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
public class UseCaseDiagramGuideTool implements ToolProvider {

    public UseCaseDiagramGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
	        return List.of(
	                ToolSupport.definition(
	                        "usecase_dgm_guide",
	                        "MCP client (you) MUST call this tool function before referencing or editing a usecase diagram to understand its usage and terminology definitions.",
	                        this::getGuide,
	                        NoInputDTO.class,
	                        GuideDTO.class)
	        );

        } catch (Exception e) {
            log.error("Failed to create usecase diagram guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get usecase diagram guide: {}", param);
        
        String content = """
IMPORTANT POINTS to Keep in Mind:
* After editing a diagram, retrieve its drawing content and verify that the edits and layout are as intended.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* UseCases are a means to capture the requirements of systems, i.e., what systems are supposed to do. The key concepts specified in this clause are Actors, UseCases, and subjects. Each UseCase’s subject represents a system under consideration to which the UseCase applies. Users and any other systems that may interact with a subject are represented as Actors.
* Actors may represent roles played by human users, external hardware, or other systems.
* An Extend is a relationship from an extending UseCase (the extension) to an extended UseCase (the extendedCase) that specifies how and when the behavior defined in the extending UseCase can be inserted into the behavior defined in the extended UseCase.
* Extend is intended to be used when there is some additional behavior that should be added, possibly conditionally, to the behavior defined in one or more UseCases.
* Include is a DirectedRelationship between two UseCases, indicating that the behavior of the included UseCase (the addition) is inserted into the behavior of the including UseCase (the includingCase).
* The Include relationship is intended to be used when there are common parts of the behavior of two or more UseCases. This common part is then extracted to a separate UseCase, to be included by all the base UseCases having this part in common. As the primary use of the Include relationship is for reuse of common parts, what is left in a base UseCase is usually not complete in itself but dependent on the included parts to be meaningful.
* The Include relationship allows hierarchical composition of UseCases as well as reuse of UseCases.
* An Extend relationship between UseCases is shown by a dashed arrow with an open arrowhead pointing from the extending UseCase towards the extended UseCase. The arrow is labeled with the keyword «extend».
* An Include relationship between UseCases is shown by a dashed arrow with an open arrowhead pointing from the base UseCase to the included UseCase. The arrow is labeled with the keyword «include».
        """;
        
        return new GuideDTO(content);
    }
}
