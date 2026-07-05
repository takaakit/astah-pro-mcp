package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.change_vision.jude.api.inf.model.IDiagram;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
public class ImageCaptureSupport {

    private final AstahProToolSupport astahProToolSupport;
    private final Path imageOutputDir;

    public ImageCaptureSupport(AstahProToolSupport astahProToolSupport, Path imageOutputDir) {
        this.astahProToolSupport = astahProToolSupport;
        this.imageOutputDir = imageOutputDir;
    }

    // Bundles an exported diagram image with the metadata needed to map diagram coordinates onto the image's pixel coordinates.
    private record ExportedImage(
        BufferedImage image,
        String relativeImagePath,
        Rectangle2D boundRect
    ) {
    }

    // Exports the specified diagram to a PNG file and reads it back into memory
    private ExportedImage exportDiagramImage(String diagramId) throws Exception {
        IDiagram astahDiagram = astahProToolSupport.getDiagram(diagramId);

        // Ensure the output directory exists and create it when needed
        log.debug("Create output directory: {}", imageOutputDir);
        try {
            Files.createDirectories(imageOutputDir);
        } catch (Exception e) {
            throw new Exception("Failed to create output directory: " + e.getMessage());
        }

        // Verify that the directory is writable
        if (!Files.isWritable(imageOutputDir)) {
            throw new Exception("Output directory is not writable: " + imageOutputDir);
        }

        // Export an image using the Astah API
        String relativeImagePath;
        try {
            relativeImagePath = astahDiagram.exportImage(imageOutputDir.toString(), "png", 96);
        } catch (Exception e) {
            throw new Exception("Astah API exportImage method failed: " + e.getMessage());
        }

        if (relativeImagePath == null || relativeImagePath.trim().isEmpty()) {
            throw new Exception("exportImage returned null or empty file path");
        }

        Path absoluteImagePath = Paths.get(imageOutputDir.toString(), relativeImagePath);
        if (!Files.exists(absoluteImagePath)) {
            throw new Exception("Exported image file does not exist: " + absoluteImagePath.toString());
        }

        // Read the exported image file
        BufferedImage image;
        try {
            image = ImageIO.read(absoluteImagePath.toFile());
        } catch (Exception e) {
            throw new Exception("Failed to read exported image file: " + e.getMessage());
        }
        if (image == null) {
            throw new Exception("Failed to read image file: " + absoluteImagePath.toString());
        }

        Rectangle2D boundRect = astahDiagram.getBoundRect();

        return new ExportedImage(image, relativeImagePath, boundRect);
    }

    public McpSchema.ImageContent createImageContent(String diagramId, ImageRegion region) throws Exception {
        log.debug("Capture diagram image as PNG: {}", diagramId);

        ExportedImage exported = exportDiagramImage(diagramId);
        BufferedImage image = exported.image();
        String relativeImagePath = exported.relativeImagePath();

        try {
            // Calculate the size of the cropped region
            int width = image.getWidth();
            int height = image.getHeight();
            int cropWidth, cropHeight;

            if (region == ImageRegion.FULL) {
                cropWidth = width;
                cropHeight = height;
            } else {
                int halfWidth = width / 2;
                int halfHeight = height / 2;

                switch (region) {
                    case TOP_LEFT -> { cropWidth = halfWidth; cropHeight = halfHeight; }
                    case TOP_RIGHT -> { cropWidth = width - halfWidth; cropHeight = halfHeight; }
                    case BOTTOM_LEFT -> { cropWidth = halfWidth; cropHeight = height - halfHeight; }
                    case BOTTOM_RIGHT -> { cropWidth = width - halfWidth; cropHeight = height - halfHeight; }
                    default -> { cropWidth = width; cropHeight = height; }
                }
            }

            // Calculate the scaling factor based on the cropped region size
            final long TARGET_SIZE = 500L * 1024; // Target roughly 500 KB for the PNG before Base64 encoding
            long estimatedCroppedSize = (long) cropWidth * cropHeight * 4; // Approximate using 4 bytes per pixel for RGBA
            double scale = estimatedCroppedSize <= TARGET_SIZE ? 1.0 :
                    Math.max(0.1, Math.min(1.0, Math.sqrt((double) TARGET_SIZE / (double) estimatedCroppedSize)));

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                Thumbnails.Builder<BufferedImage> thumbnailBuilder = Thumbnails.of(image);

                // Crop according to the selected region
                if (region != ImageRegion.FULL) {
                    int halfWidth = width / 2;
                    int halfHeight = height / 2;

                    switch (region) {
                        case TOP_LEFT -> thumbnailBuilder.sourceRegion(0, 0, halfWidth, halfHeight);
                        case TOP_RIGHT -> thumbnailBuilder.sourceRegion(halfWidth, 0, width - halfWidth, halfHeight);
                        case BOTTOM_LEFT -> thumbnailBuilder.sourceRegion(0, halfHeight, halfWidth, height - halfHeight);
                        case BOTTOM_RIGHT -> thumbnailBuilder.sourceRegion(halfWidth, halfHeight, width - halfWidth, height - halfHeight);
                        default -> throw new IllegalArgumentException("Unexpected image region: " + region);
                    }
                }

                thumbnailBuilder.scale(scale)
                        .outputFormat("png")
                        .toOutputStream(outputStream);

                byte[] pngBytes = outputStream.toByteArray();

                if (pngBytes.length == 0) {
                    throw new RuntimeException("PNG conversion produced empty result");
                }

                log.info("PNG conversion succeeded ({} bytes, scale={}, region={}, cropSize={}x{})",
                        pngBytes.length, scale, region, cropWidth, cropHeight);

                // Write the scaled image to disk
                try {
                    String scaledFileName = "scaled_" + relativeImagePath.replace(".png", "_scale" + String.format("%.2f", scale) + ".png");
                    if (scaledFileName.equals("scaled_" + relativeImagePath)) {
                        scaledFileName = "scaled_" + relativeImagePath + "_scale" + String.format("%.2f", scale) + ".png";
                    }
                    Path scaledImagePath = Paths.get(imageOutputDir.toString(), scaledFileName);
                    Files.write(scaledImagePath, pngBytes);
                    log.info("Scaled image saved to: {}", scaledImagePath);
                } catch (Exception e) {
                    log.warn("Failed to save scaled image file: {}", e.getMessage());
                    // Continue even if writing the file fails
                }

                String encoded = Base64.getEncoder().encodeToString(pngBytes);
                return McpSchema.ImageContent.builder(encoded, "image/png").build();

            } catch (OutOfMemoryError e) {
                throw new RuntimeException("Insufficient memory to process screenshot", e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert screenshot to PNG", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process exported image file: " + e.getMessage());
        }
    }

    public McpSchema.ImageContent createCroppedImageContent(String diagramId, int x, int y, int width, int height) throws Exception {
        log.debug("Crop diagram image as PNG: {} (x={}, y={}, width={}, height={})", diagramId, x, y, width, height);

        if (width <= 0 || height <= 0) {
            throw new Exception("Crop width and height must be positive: width=" + width + ", height=" + height);
        }

        ExportedImage exported = exportDiagramImage(diagramId);
        BufferedImage image = exported.image();
        Rectangle2D boundRect = exported.boundRect();

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double boundX = boundRect.getX();
        double boundY = boundRect.getY();
        double boundWidth = boundRect.getWidth();
        double boundHeight = boundRect.getHeight();

        // The crop area is given in the diagram coordinate system, i.e. the same coordinates returned by get_dgm_rectangle and get_prsts_on_dgm.
        // Reject any crop area that extends outside the diagram, even partially.
        // The check is performed in diagram coordinates (not pixel coordinates) so that rounding during the pixel conversion below cannot cause a false rejection at the edges.
        if (x < boundX || y < boundY
                || x + (double) width > boundX + boundWidth
                || y + (double) height > boundY + boundHeight) {
            throw new Exception(String.format(
                "The specified crop area extends outside the diagram bounds. "
                + "The diagram bound rectangle is x=%.1f, y=%.1f, width=%.1f, height=%.1f, "
                + "but the requested crop area (x=%d, y=%d, width=%d, height=%d) does not fit within it. "
                + "Specify a crop area that lies inside the diagram bound rectangle "
                + "(obtainable via the get_dgm_rectangle tool).",
                boundX, boundY, boundWidth, boundHeight,
                x, y, width, height));
        }

        // The exported image spans the diagram bound rectangle, so the image's top-left pixel (0,0) in pixel coordinates corresponds to the top-left
        // corner of the bound rectangle (diagram coordinates boundX, boundY, which may be negative).
        // Translate the crop edges into the image's pixel coordinates.
        // The right and bottom edges are mapped from the absolute coordinates (x + width, y + height) and then clamped to the image, so independent rounding can never push the region past the image bounds.
        double scaleX = imageWidth / boundWidth;
        double scaleY = imageHeight / boundHeight;

        int pixelLeft = (int) Math.round((x - boundX) * scaleX);
        int pixelTop = (int) Math.round((y - boundY) * scaleY);
        int pixelRight = (int) Math.round((x + (double) width - boundX) * scaleX);
        int pixelBottom = (int) Math.round((y + (double) height - boundY) * scaleY);

        pixelLeft = Math.max(0, Math.min(pixelLeft, imageWidth));
        pixelTop = Math.max(0, Math.min(pixelTop, imageHeight));
        pixelRight = Math.max(pixelLeft, Math.min(pixelRight, imageWidth));
        pixelBottom = Math.max(pixelTop, Math.min(pixelBottom, imageHeight));

        int pixelX = pixelLeft;
        int pixelY = pixelTop;
        int pixelWidth = pixelRight - pixelLeft;
        int pixelHeight = pixelBottom - pixelTop;

        if (pixelWidth <= 0 || pixelHeight <= 0) {
            throw new Exception(String.format(
                "The specified crop area is too small to produce an image. The requested crop area (x=%d, y=%d, width=%d, height=%d) maps to an empty pixel region.", x, y, width, height));
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .sourceRegion(pixelX, pixelY, pixelWidth, pixelHeight)
                    .scale(1.0)               // Keep full resolution; do not downscale.
                    .outputFormat("png")
                    .toOutputStream(outputStream);

            byte[] pngBytes = outputStream.toByteArray();

            if (pngBytes.length == 0) {
                throw new RuntimeException("PNG conversion produced empty result");
            }

            log.info("PNG crop succeeded ({} bytes, pixel area x={}, y={}, {}x{})", pngBytes.length, pixelX, pixelY, pixelWidth, pixelHeight);

            String encoded = Base64.getEncoder().encodeToString(pngBytes);
            return McpSchema.ImageContent.builder(encoded, "image/png").build();

        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Insufficient memory to process cropped image", e);
        }
    }
}
