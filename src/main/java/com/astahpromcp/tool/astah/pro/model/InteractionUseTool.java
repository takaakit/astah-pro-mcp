package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionUseWithArgumentDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionUseWithSequenceDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionUseDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.InteractionUseDTOAssembler;
import com.change_vision.jude.api.inf.model.IInteractionUse;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IInteractionUse.html
@Slf4j
public class InteractionUseTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public InteractionUseTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_interaction_use_info",
                "Return model element information about the specified interaction use (specified by ID).",
                this::getInfo,
                IdDTO.class,
                InteractionUseDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_arg_of_interaction_use",
                "Set the argument of the specified interaction use (specified by ID), and return the model element of the interaction use after it is set.",
                this::setArgument,
                InteractionUseWithArgumentDTO.class,
                InteractionUseDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_seq_dgm_to_interaction_use",
                "Set the sequence diagram (specified by ID) to the specified interaction use (specified by ID), and return the model element of the interaction use after it is set.",
                this::setSequenceDiagram,
                InteractionUseWithSequenceDiagramDTO.class,
                InteractionUseDTO.class)
        );
    }

    private InteractionUseDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get interaction use information: {}", param);

        IInteractionUse astahInteractionUse = astahProToolSupport.getInteractionUse(param.id());

        return InteractionUseDTOAssembler.toDTO(astahInteractionUse);
    }

    private InteractionUseDTO setArgument(McpSyncServerExchange exchange, InteractionUseWithArgumentDTO param) throws Exception {
        log.debug("Set argument of interaction use: {}", param);

        IInteractionUse astahInteractionUse = astahProToolSupport.getInteractionUse(param.targetInteractionUseId());

        txnAstah.run( () -> {
            astahInteractionUse.setArgment(param.argument());
        });

        return InteractionUseDTOAssembler.toDTO(astahInteractionUse);
    }

    private InteractionUseDTO setSequenceDiagram(McpSyncServerExchange exchange, InteractionUseWithSequenceDiagramDTO param) throws Exception {
        log.debug("Set sequence diagram to interaction use: {}", param);

        IInteractionUse astahInteractionUse = astahProToolSupport.getInteractionUse(param.targetInteractionUseId());
        ISequenceDiagram astahSequenceDiagram = astahProToolSupport.getSequenceDiagram(param.targetSequenceDiagramId());

        txnAstah.run( () -> {
            astahInteractionUse.setSequenceDiagram(astahSequenceDiagram);
        });

        return InteractionUseDTOAssembler.toDTO(astahInteractionUse);
    }
}
