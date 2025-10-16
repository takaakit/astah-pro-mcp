package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.*;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.astahpromcp.tool.astah.pro.image.inputdto.DiagramWithImageRegionDTO;
import com.change_vision.jude.api.inf.model.IDiagram;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
public class ImageCaptureTool implements ToolProvider {

    private final AstahProToolSupport astahProToolSupport;
    private final Path imageOutputDir;

    public ImageCaptureTool(AstahProToolSupport astahProToolSupport, Path imageOutputDir) {
        this.astahProToolSupport = astahProToolSupport;
        this.imageOutputDir = imageOutputDir;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            captureDiagramImageTool()
        );
    }

    private ToolDefinition captureDiagramImageTool() {
        McpSchema.Tool schema = McpSchema.Tool.builder()
                .name("capture_dgm_img")
                .description("Capture a PNG image of the specified area within the specified diagram (specified by ID). The MCP client (you) can use this tool to get a diagram image and understand its contents, whether you want to review the drawing itself or check it after making edits. If the image of the full diagram has too low a resolution to recognize the drawing details, you can obtain a higher-resolution image by specifying the top left, top right, bottom left, or bottom right area.")
                .inputSchema(JsonSupport.MCP_JSON_MAPPER, SchemaSupport.generateSchema(DiagramWithImageRegionDTO.class))
                .build();

        return new ToolDefinition(schema, this::captureDiagramImage);
    }

    private McpSchema.CallToolResult captureDiagramImage(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {

        ValidationSupport.ValidationResult<DiagramWithImageRegionDTO> parsed = ValidationSupport.parse(request.arguments(), DiagramWithImageRegionDTO.class);
        if (parsed.error() != null) return parsed.error();

        try {
            DiagramWithImageRegionDTO param = parsed.dto();
            McpSchema.ImageContent content = createImageContent(param.targetDiagramId(), param.region());
            List<McpSchema.Content> contents = new ArrayList<>();
            contents.add(content);

            return McpSchema.CallToolResult.builder()
                    .content(contents)
                    .isError(false)
                    .build();

        } catch (Exception e) {
            String message = "Failed to capture diagram image: " + e.getMessage();
            log.error(message, e);
            return ResponseSupport.error(message);
        }
    }

    private McpSchema.ImageContent createImageContent(String diagramId, ImageRegion region) throws Exception {
        log.debug("Capture diagram image as PNG: {}", diagramId);

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
        
        // Inspect the exported image file
        try {
            BufferedImage image = ImageIO.read(absoluteImagePath.toFile());
            if (image == null) {
                throw new Exception("Failed to read image file: " + absoluteImagePath.toString());
            }

            // Calculate the size of the cropped region
            int width = image.getWidth();
            int height = image.getHeight();
            int cropWidth, cropHeight;
            
            if (region == ImageRegion.full) {
                cropWidth = width;
                cropHeight = height;
            } else {
                int halfWidth = width / 2;
                int halfHeight = height / 2;
                
                switch (region) {
                    case top_left -> { cropWidth = halfWidth; cropHeight = halfHeight; }
                    case top_right -> { cropWidth = width - halfWidth; cropHeight = halfHeight; }
                    case bottom_left -> { cropWidth = halfWidth; cropHeight = height - halfHeight; }
                    case bottom_right -> { cropWidth = width - halfWidth; cropHeight = height - halfHeight; }
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
                if (region != ImageRegion.full) {
                    int halfWidth = width / 2;
                    int halfHeight = height / 2;
                    
                    switch (region) {
                        case top_left -> thumbnailBuilder.sourceRegion(0, 0, halfWidth, halfHeight);
                        case top_right -> thumbnailBuilder.sourceRegion(halfWidth, 0, width - halfWidth, halfHeight);
                        case bottom_left -> thumbnailBuilder.sourceRegion(0, halfHeight, halfWidth, height - halfHeight);
                        case bottom_right -> thumbnailBuilder.sourceRegion(halfWidth, halfHeight, width - halfWidth, height - halfHeight);
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
