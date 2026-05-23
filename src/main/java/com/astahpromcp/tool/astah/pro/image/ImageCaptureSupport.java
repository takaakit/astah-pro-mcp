package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.change_vision.jude.api.inf.model.IDiagram;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ImageCaptureSupport {

    private final AstahProToolSupport astahProToolSupport;
    private final Path imageOutputDir;

    // Serializes Astah API access and reading of the exported image file.
    private final ReentrantLock exportLock = new ReentrantLock();

    public ImageCaptureSupport(AstahProToolSupport astahProToolSupport, Path imageOutputDir) {
        this.astahProToolSupport = astahProToolSupport;
        this.imageOutputDir = imageOutputDir;
    }

    public McpSchema.ImageContent createImageContent(String diagramId, ImageRegion region) throws Exception {
        log.debug("Capture diagram image as PNG: {}", diagramId);

        BufferedImage image;
        String relativeImagePath;
        Path absoluteImagePath;

        // Lock-protected section
        exportLock.lock();
        try {
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
            try {
                relativeImagePath = astahDiagram.exportImage(imageOutputDir.toString(), "png", 96);
            } catch (Exception e) {
                throw new Exception("Astah API exportImage method failed: " + e.getMessage());
            }

            if (relativeImagePath == null || relativeImagePath.trim().isEmpty()) {
                throw new Exception("exportImage returned null or empty file path");
            }

            absoluteImagePath = Paths.get(imageOutputDir.toString(), relativeImagePath);
            if (!Files.exists(absoluteImagePath)) {
                throw new Exception("Exported image file does not exist: " + absoluteImagePath.toString());
            }

            // Read the exported image file
            try {
                image = ImageIO.read(absoluteImagePath.toFile());
            } catch (Exception e) {
                throw new Exception("Failed to read exported image file: " + e.getMessage());
            }
            if (image == null) {
                throw new Exception("Failed to read image file: " + absoluteImagePath.toString());
            }
        } finally {
            exportLock.unlock();
        }

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
                return new McpSchema.ImageContent(null, encoded, "image/png");

            } catch (OutOfMemoryError e) {
                throw new RuntimeException("Insufficient memory to process screenshot", e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert screenshot to PNG", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process exported image file: " + e.getMessage());
        }
    }

}
