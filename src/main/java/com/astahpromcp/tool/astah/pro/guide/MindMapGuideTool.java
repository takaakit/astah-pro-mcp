package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.guide.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MindMapGuideTool implements ToolProvider {
    
    public MindMapGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
	        return List.of(
	            ToolSupport.definition(
	                "mind_map_guide",
	                "MCP client (you) MUST call this tool function before referencing or editing a mind map to understand its usage and terminology definitions.",
	                this::getGuide,
	                NoInputDTO.class,
	                GuideDTO.class)
	        );

        } catch (Exception e) {
            log.error("Failed to create mind map guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get mind map guide: {}", param);
        
        String content = """
IMPORTANT POINTS to Keep in Mind:
* For newly created topics, create and insert a 32px x 32px SVG icon that visually represents the topic.
* The topic label may contain not only a title but also a description.
        """;

        return new GuideDTO(content);
    }
}
