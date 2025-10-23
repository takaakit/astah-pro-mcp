package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionOperandWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionOperandDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IInteractionOperand.html
@Slf4j
public class InteractionOperandTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public InteractionOperandTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
                ToolSupport.definition(
                        "get_intr_oprd_info",
                        "Return detailed information about the specified interaction operand (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        InteractionOperandDTO.class),

                ToolSupport.definition(
                        "set_guard_of_intr_oprd",
                        "Set the guard of the specified interaction operand (specified by ID), and return the interaction operand information after it is set.",
                        this::setGuard,
                        InteractionOperandWithGuardDTO.class,
                        InteractionOperandDTO.class)
        );
    }

    private InteractionOperandDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get interaction operand information: {}", param);

        IInteractionOperand astahInteractionOperand = astahProToolSupport.getInteractionOperand(param.id());

        return InteractionOperandDTOAssembler.toDTO(astahInteractionOperand);
    }

    private InteractionOperandDTO setGuard(McpSyncServerExchange exchange, InteractionOperandWithGuardDTO param) throws Exception {
        log.debug("Set guard of interaction operand: {}", param);
        
        IInteractionOperand astahInteractionOperand = astahProToolSupport.getInteractionOperand(param.targetInteractionOperandId());
        
        try {
            transactionManager.beginTransaction();
            astahInteractionOperand.setGuard(param.guard());
            transactionManager.endTransaction();

            return InteractionOperandDTOAssembler.toDTO(astahInteractionOperand);
            
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
