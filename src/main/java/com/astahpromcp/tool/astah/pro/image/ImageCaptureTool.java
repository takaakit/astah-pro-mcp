package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.image.inputdto.DiagramWithImageRegionDTO;
import com.astahpromcp.tool.astah.pro.image.inputdto.DiagramWithCropAreaDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageCaptureTool implements ToolProvider {

    private final ImageCaptureSupport imageCaptureSupport;

    public ImageCaptureTool(ImageCaptureSupport imageCaptureSupport) {
        this.imageCaptureSupport = imageCaptureSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.toolDefinitionReturningContents(
                "capture_dgm_img",
                "Capture a PNG image of the specified area within the specified diagram (specified by ID). The MCP client (you) can use this tool to get a diagram image and understand its contents, whether you want to review the drawing itself or check it after making edits. If the image of the full diagram has too low a resolution to recognize the drawing details, you can obtain a higher-resolution image by specifying the top left, top right, bottom left, or bottom right area.",
                this::captureDiagramImage,
                DiagramWithImageRegionDTO.class),

            ToolSupport.toolDefinitionReturningContents(
                "crop_dgm_img",
                "Crop a PNG image of the specified rectangular area within the specified diagram (specified by ID). This tool returns image data without reducing the resolution. Use this tool if diagram images obtained with other tools are too blurry to recognize. If an error occurs because the returned data size exceeds the limit, run this tool again with a smaller specified rectangular area.",
                this::cropDiagramImage,
                DiagramWithCropAreaDTO.class)
        );
    }

    private List<McpSchema.Content> captureDiagramImage(McpSyncServerExchange exchange, DiagramWithImageRegionDTO param) throws Exception {
        log.debug("Capture diagram image: {}", param);

        McpSchema.ImageContent content = imageCaptureSupport.createImageContent(param.targetDiagramId(), param.region());
        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(content);

        return contents;
    }

    private List<McpSchema.Content> cropDiagramImage(McpSyncServerExchange exchange, DiagramWithCropAreaDTO param) throws Exception {
        log.debug("Crop diagram image: {}", param);

        McpSchema.ImageContent content = imageCaptureSupport.createCroppedImageContent(
                param.targetDiagramId(), param.x(), param.y(), param.width(), param.height());
        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(content);

        return contents;
    }
}
