package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionOperandWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.InteractionOperandDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IInteractionOperand.html
@Slf4j
public class InteractionOperandTool extends AstahToolProvider {

    private final BasicModelEditor basicModelEditor;
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public InteractionOperandTool(BasicModelEditor basicModelEditor, ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.basicModelEditor = basicModelEditor;
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_interaction_operand_info",
                "Return model element information about the specified interaction operand (specified by ID). The lifelines and messages Astah's model records for the operand can differ from what the diagram draws inside it; drawnLifelines and drawnMessages report what the diagram shows, and matchesDiagram tells whether the two agree.",
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
        IInteractionOperand astahInteractionOperand = astahProToolSupport.getInteractionOperand(param.id());

        return InteractionOperandDTOAssembler.toDTO(
            astahInteractionOperand,
            SequenceDiagramLayout.drawnContentsOf(projectAccessor, astahInteractionOperand));
    }

    private InteractionOperandDTO setGuard(InteractionOperandWithGuardDTO param) throws Exception {
        IInteractionOperand astahInteractionOperand = astahProToolSupport.getInteractionOperand(param.targetInteractionOperandId());

        // A null or blank guard means no guard. Surrounding whitespace is removed up front.
        String guard = param.guard() == null ? "" : param.guard().strip();

        txnAstah.run( () -> {
            // Workaround: IInteractionOperand.setGuard() adds another guard constraint instead of replacing the current one, and leaves the old guard in the model. So rewrite, or delete, the constraint that holds the current guard rather than calling setGuard() on an operand that already has one.
            // An empty current guard counts as no guard.
            String currentGuard = astahInteractionOperand.getGuard();
            IConstraint currentGuardConstraint = null;
            if (currentGuard != null && !currentGuard.isEmpty()) {
                for (IConstraint constraint : astahInteractionOperand.getConstraints()) {
                    if (constraint.getSpecification().equals(currentGuard)) {
                        currentGuardConstraint = constraint;  // The last match is the current guard, since setGuard() appends
                    }
                }
            }

            if (guard.isEmpty()) {
                // An empty guard means no guard. Astah refuses an empty specification, and setGuard("") adds an empty guard beside the current one, so delete the constraint that holds the current guard instead.
                if (currentGuardConstraint != null) {
                    basicModelEditor.delete(currentGuardConstraint);
                }
            } else if (currentGuardConstraint != null) {
                currentGuardConstraint.setSpecification(guard);
            } else {
                astahInteractionOperand.setGuard(guard);
            }
        });

        return InteractionOperandDTOAssembler.toDTO(
            astahInteractionOperand,
            SequenceDiagramLayout.drawnContentsOf(projectAccessor, astahInteractionOperand));
    }
}
