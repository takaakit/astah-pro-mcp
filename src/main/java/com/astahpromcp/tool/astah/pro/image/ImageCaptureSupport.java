package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.change_vision.jude.api.inf.model.IDiagram;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Slf4j
public class ImageCaptureSupport {

    // Target size for the PNG payload before Base64 encoding.
    public enum PngSizeTarget {
        LARGE(50L * 1024),    // 50 KB
        SMALL(5L * 1024);     // 5 KB

        private final long targetBytes;

        PngSizeTarget(long targetBytes) {
            this.targetBytes = targetBytes;
        }

        public long targetBytes() {
            return targetBytes;
        }
    }

    private final AstahProToolSupport astahProToolSupport;
    private final SystemPropertySupport systemPropertySupport;
    private final Path imageOutputDir;

    public ImageCaptureSupport(AstahProToolSupport astahProToolSupport, SystemPropertySupport systemPropertySupport, Path imageOutputDir) {
        this.astahProToolSupport = astahProToolSupport;
        this.systemPropertySupport = systemPropertySupport;
        this.imageOutputDir = imageOutputDir;
    }

    // Bundles an exported diagram image with the metadata needed to map diagram coordinates onto the image's pixel coordinates.
    record ExportedImage(
        BufferedImage image,
        String relativeImagePath,
        Rectangle2D boundRect
    ) {
    }

    // Exports the specified diagram to a PNG file and reads it back into memory
    ExportedImage exportDiagramImage(String diagramId) throws Exception {
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
            relativeImagePath = astahDiagram.exportImage(imageOutputDir.toString(), "png", systemPropertySupport.imageExportDpi());
        } catch (Exception e) {
            throw new Exception("Astah API exportImage method failed: " + e.getMessage());
        }

        if (relativeImagePath == null || relativeImagePath.trim().isEmpty()) {
            throw new Exception("exportImage returned null or empty file path");
        }

        Path absoluteImagePath = imageOutputDir.resolve(relativeImagePath);
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

    public McpSchema.ImageContent createImageContent(String diagramId, ImageRegion region, PngSizeTarget sizeTarget) throws Exception {
        log.debug("Capture diagram image as PNG: {}", diagramId);

        ExportedImage exported = exportDiagramImage(diagramId);
        BufferedImage image = exported.image();
        String relativeImagePath = exported.relativeImagePath();

        // Calculate the cropped region
        Rectangle crop = cropRectangleOf(region, image.getWidth(), image.getHeight());

        try {
            EncodedPng encoded = encodeDownscaledPng(image, crop, sizeTarget.targetBytes());
            byte[] pngBytes = encoded.bytes();

            log.info("PNG conversion succeeded ({} bytes, scale={}, region={}, cropSize={}x{})",
                    pngBytes.length, encoded.scale(), region, crop.width, crop.height);

            saveScaledImage(relativeImagePath, encoded.scale(), pngBytes);

            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return McpSchema.ImageContent.builder(base64, "image/png").build();

        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Insufficient memory to process screenshot", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert screenshot to PNG", e);
        }
    }

    public McpSchema.ImageContent createLargeImageContent(String diagramId, ImageRegion region) throws Exception {
        return createImageContent(diagramId, region, PngSizeTarget.LARGE);
    }

    public McpSchema.ImageContent createSmallImageContent(String diagramId) throws Exception {
        return createImageContent(diagramId, ImageRegion.FULL, PngSizeTarget.SMALL);
    }

    // Saves the encoded PNG next to the exported image for troubleshooting; failures are non-fatal.
    private void saveScaledImage(String relativeImagePath, double scale, byte[] pngBytes) {
        try {
            String baseName = relativeImagePath.endsWith(".png")
                    ? relativeImagePath.substring(0, relativeImagePath.length() - ".png".length())
                    : relativeImagePath;
            String scaledFileName = "scaled_" + baseName + "_scale" + String.format("%.2f", scale) + ".png";
            Path scaledImagePath = imageOutputDir.resolve(scaledFileName);
            Files.write(scaledImagePath, pngBytes);
            log.info("Scaled image saved to: {}", scaledImagePath);
            
        } catch (Exception e) {
            log.warn("Failed to save scaled image file: {}", e.getMessage());
            // Continue even if writing the file fails
        }
    }

    // Maps an image region onto the crop rectangle in the image's pixel coordinates (FULL covers the whole image).
    static Rectangle cropRectangleOf(ImageRegion region, int width, int height) {
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        return switch (region) {
            case FULL -> new Rectangle(0, 0, width, height);
            case TOP_LEFT -> new Rectangle(0, 0, halfWidth, halfHeight);
            case TOP_RIGHT -> new Rectangle(halfWidth, 0, width - halfWidth, halfHeight);
            case BOTTOM_LEFT -> new Rectangle(0, halfHeight, halfWidth, height - halfHeight);
            case BOTTOM_RIGHT -> new Rectangle(halfWidth, halfHeight, width - halfWidth, height - halfHeight);
        };
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
                "The specified crop area extends outside the diagram bounds. The diagram bound rectangle is x=%.1f, y=%.1f, width=%.1f, height=%.1f, but the requested crop area (x=%d, y=%d, width=%d, height=%d) does not fit within it. Specify a crop area that lies inside the diagram bound rectangle (obtainable via the get_dgm_rectangle tool).",
                boundX, boundY, boundWidth, boundHeight, x, y, width, height));
        }

        // The exported image spans the diagram bound rectangle, so the image's top-left pixel (0,0) in pixel coordinates corresponds to the top-left corner of the bound rectangle (diagram coordinates boundX, boundY, which may be negative).
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

    public McpSchema.ImageContent createWindowImageContent(JFrame frame) throws Exception {
        log.debug("Capture Astah main window as PNG");

        if (frame == null) {
            throw new Exception("The Astah main window is not available");
        }

        BufferedImage image = captureWindowImage(frame);

        int width = image.getWidth();
        int height = image.getHeight();

        try {
            EncodedPng encoded = encodeDownscaledPng(image, null, PngSizeTarget.LARGE.targetBytes());
            byte[] pngBytes = encoded.bytes();

            log.info("PNG conversion succeeded ({} bytes, scale={}, size={}x{})", pngBytes.length, encoded.scale(), width, height);

            // Write the scaled image to disk
            try {
                Files.createDirectories(imageOutputDir);
                String scaledFileName = "scaled_astah_window_scale" + String.format("%.2f", encoded.scale()) + ".png";
                Path scaledImagePath = imageOutputDir.resolve(scaledFileName);
                Files.write(scaledImagePath, pngBytes);
                log.info("Scaled image saved to: {}", scaledImagePath);
            } catch (Exception e) {
                log.warn("Failed to save scaled image file: {}", e.getMessage());
                // Continue even if writing the file fails
            }

            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return McpSchema.ImageContent.builder(base64, "image/png").build();

        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Insufficient memory to process screenshot", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert screenshot to PNG", e);
        }
    }

    // PNG bytes plus the linear scale factor actually applied (1.0 when no downscaling was needed).
    record EncodedPng(byte[] bytes, double scale) {
    }

    // Encodes (a region of) the image to PNG, downscaling only when the full-resolution PNG actually exceeds the target size.
    static EncodedPng encodeDownscaledPng(BufferedImage image, Rectangle sourceRegion, long targetPngSizeBytes) throws Exception {
        // Encode at full resolution first and measure the real compressed size.
        byte[] fullBytes = encodePng(image, sourceRegion, 1.0);
        if (fullBytes.length <= targetPngSizeBytes) {
            return new EncodedPng(fullBytes, 1.0);  // common case: figures compress well, no downscale needed
        }

        // Too large: derive the scale from the measured PNG size (bytes ~ pixel area ~ scale^2) and re-encode once.
        double scale = Math.max(0.1, Math.min(1.0,
                Math.sqrt((double) targetPngSizeBytes / (double) fullBytes.length)));
        byte[] scaledBytes = encodePng(image, sourceRegion, scale);
        return new EncodedPng(scaledBytes, scale);
    }

    // Encodes (a region of) the image to PNG at the given linear scale.
    private static byte[] encodePng(BufferedImage image, Rectangle sourceRegion, double scale) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(image);
            if (sourceRegion != null) {
                builder.sourceRegion(sourceRegion.x, sourceRegion.y, sourceRegion.width, sourceRegion.height);
            }
            builder.scale(scale)
                    .outputFormat("png")
                    .toOutputStream(outputStream);

            byte[] pngBytes = outputStream.toByteArray();
            if (pngBytes.length == 0) {
                throw new RuntimeException("PNG conversion produced empty result");
            }
            return pngBytes;
        }
    }

    private BufferedImage captureWindowImage(JFrame frame) throws Exception {
        final BufferedImage[] captured = new BufferedImage[1];
        final Exception[] error = new Exception[1];

        // Swing components must be accessed on the Event Dispatch Thread.
        Runnable paintTask = () -> {
            try {
                int width = frame.getWidth();
                int height = frame.getHeight();
                if (width <= 0 || height <= 0) {
                    throw new Exception("The Astah main window has no visible area (width=" + width + ", height=" + height + "). Make sure Astah is running and not minimized.");
                }

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    // printAll paints the whole component tree, honoring Swing double buffering (unlike paint).
                    frame.printAll(graphics);
                } finally {
                    graphics.dispose();
                }
                captured[0] = image;
            
            } catch (Exception e) {
                error[0] = e;
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            paintTask.run();
        } else {
            SwingUtilities.invokeAndWait(paintTask);
        }

        if (error[0] != null) {
            throw error[0];
        }
        if (captured[0] == null) {
            throw new Exception("Failed to render the Astah main window image");
        }
        
        return captured[0];
    }
}
