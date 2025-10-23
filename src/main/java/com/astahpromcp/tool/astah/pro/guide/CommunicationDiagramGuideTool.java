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
public class CommunicationDiagramGuideTool implements ToolProvider {

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "comm_dgm_guide",
                            "MCP client (you) MUST call this tool function before referencing or editing a communication diagram to understand its usage and terminology definitions.",
                            this::getGuide,
                            NoInputDTO.class,
                            GuideDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get communication diagram guide: {}", param);
        
        String content = """
IMPORTANT POINTS to Keep in Mind:
* For communication-diagram-related tool functions, only read-only (viewing) operations are provided; editing operations are not available.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* Communication Diagrams focus on the interaction between Lifelines where the architecture of the internal structure and how this corresponds with the message passing is central.
* Communication Diagrams correspond to simple Sequence Diagrams that use none of the structuring mechanisms such as InteractionUses and CombinedFragments.
* The sequencing of Messages is given through a sequence numbering scheme.
        """;
        
        return new GuideDTO(content);
    }
}
