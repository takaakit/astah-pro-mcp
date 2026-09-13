package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionOperandWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.InteractionOperandDTOAssembler;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IInteractionOperand.html
@Slf4j
public class InteractionOperandTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public InteractionOperandTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_interaction_operand_info",
                "Return model element information about the specified interaction operand (specified by ID).",
                this::getInfo,
                IdDTO.class,
                InteractionOperandDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_guard_of_interaction_operand",
                "Set the guard of the specified interaction operand (specified by ID), and return the model element of the interaction operand after it is set.",
                this::setGuard,
                InteractionOperandWithGuardDTO.class,
                InteractionOperandDTO.class)
        );
    }

    private InteractionOperandDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get interaction operand information: {}", param);

        IInteractionOperand astahInteractionOperand = astahProToolSupport.getInteractionOperand(param.id());

        return InteractionOperandDTOAssembler.toDTO(astahInteractionOperand);
    }

    private InteractionOperandDTO setGuard(InteractionOperandWithGuardDTO param) throws Exception {
        log.debug("Set guard of interaction operand: {}", param);

        IInteractionOperand astahInteractionOperand = astahProToolSupport.getInteractionOperand(param.targetInteractionOperandId());

        txnAstah.run( () -> {
            astahInteractionOperand.setGuard(param.guard());
        });

        return InteractionOperandDTOAssembler.toDTO(astahInteractionOperand);
    }
}
