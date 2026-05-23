package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.image.inputdto.DiagramWithImageRegionDTO;
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
                DiagramWithImageRegionDTO.class)
        );
    }

    private List<McpSchema.Content> captureDiagramImage(McpSyncServerExchange exchange, DiagramWithImageRegionDTO param) throws Exception {
        log.debug("Capture diagram image: {}", param);

        McpSchema.ImageContent content = imageCaptureSupport.createImageContent(param.targetDiagramId(), param.region());
        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(content);

        return contents;
    }
}
