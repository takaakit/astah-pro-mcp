package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LifelineWithBaseClassDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LifelineWithLengthDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.LifelineDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.LifelineDTOAssembler;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ILifeline.html
@Slf4j
public class LifelineTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public LifelineTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create lifeline tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_lifeline_info",
                "Return model element information about the specified lifeline (specified by ID).",
                this::getInfo,
                IdDTO.class,
                LifelineDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_base_class_of_lifeline",
                "Set the base class of the specified lifeline (specified by ID), and return the model element of the lifeline after it is set.",
                this::setBaseClass,
                LifelineWithBaseClassDTO.class,
                LifelineDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_length_of_lifeline",
                "Set the length of the specified lifeline (specified by ID), and return the node presentation of the lifeline after it is set.",
                this::setLength,
                LifelineWithLengthDTO.class,
                NodePresentationDTO.class)
        );
    }

    private LifelineDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get lifeline information: {}", param);

        ILifeline astahLifeline = astahProToolSupport.getLifeline(param.id());

        return LifelineDTOAssembler.toDTO(astahLifeline);
    }

    private LifelineDTO setBaseClass(LifelineWithBaseClassDTO param) throws Exception {
        log.debug("Set base class of lifeline: {}", param);

        ILifeline astahLifeline = astahProToolSupport.getLifeline(param.targetLifelineId());
        IClass astahBaseClass = astahProToolSupport.getClass(param.baseClassId());

        txnAstah.run( () -> {
            astahLifeline.setBase(astahBaseClass);
        });

        return LifelineDTOAssembler.toDTO(astahLifeline);
    }

    private NodePresentationDTO setLength(LifelineWithLengthDTO param) throws Exception {
        log.debug("Set length of lifeline: {}", param);

        ILifeline astahLifeline = astahProToolSupport.getLifeline(param.targetLifelineId());

        // Get the presentation of the target lifeline
        IPresentation[] astahLifelinePresentations = astahLifeline.getPresentations();
        if (astahLifelinePresentations.length != 1) {
            throw new RuntimeException("The lifeline does not have exactly one presentation: count = " + astahLifelinePresentations.length);
        }
        INodePresentation astahLifelinePresentation = (INodePresentation) astahLifelinePresentations[0];

        txnAstah.run( () -> {
            astahLifelinePresentation.setProperty(Key.LIFELINE_LENGTH, String.valueOf(param.length()));
        });

        return NodePresentationDTOAssembler.toDTO(astahLifelinePresentation);
    }
}
