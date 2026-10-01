package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.DiagramTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DiagramGuideTool implements ToolProvider {

    public DiagramGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "dgm_guide",
                    "MCP client (you) MUST call this tool function, specifying the diagram type, before referencing or editing a diagram of that type, to understand its usage and terminology definitions. Note that the class diagram type also serves as an object diagram and a package diagram.",
                    this::getGuide,
                    DiagramTypeDTO.class,
                    GuideDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create diagram guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(DiagramTypeDTO param) throws Exception {
        log.debug("Get diagram guide: {}", param);

        String contents = switch (param.diagramType()) {
            case CLASS -> ClassDiagramGuide.contents();
            case SEQUENCE -> SequenceDiagramGuide.contents();
            case ACTIVITY -> ActivityDiagramGuide.contents();
            case STATE_MACHINE -> StateMachineDiagramGuide.contents();
            case USE_CASE -> UseCaseDiagramGuide.contents();
            case COMMUNICATION -> CommunicationDiagramGuide.contents();
            case COMPOSITE_STRUCTURE -> CompositeStructureDiagramGuide.contents();
            case REQUIREMENT -> RequirementDiagramGuide.contents();
            case ER -> ERDiagramGuide.contents();
            case MIND_MAP -> MindMapGuide.contents();
        };

        return new GuideDTO(contents);
    }
}
