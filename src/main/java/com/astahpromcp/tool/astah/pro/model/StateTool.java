package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithDoActivityDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithEntryDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithExitDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithInternalTransitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.StateDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.model.IFinalState;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IState.html
@Slf4j
public class StateTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public StateTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_state_info",
                "Return model element information about the specified state (specified by ID).",
                this::getInfo,
                IdDTO.class,
                StateDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "add_inter_trans_of_state",
                "Add an internal transition to the specified state (specified by ID), and return the model element of the state after it is edited.",
                this::addInternalTransition,
                StateWithInternalTransitionDTO.class,
                StateDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "delete_all_inter_trans_of_state",
                "Delete all internal transitions from the specified state (specified by ID), and return the model element of the state after it is edited.",
                this::deleteAllInternalTransitions,
                IdDTO.class,
                StateDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_entry_of_state",
                "Set the entry of the specified state (specified by ID), and return the model element of the state after it is edited.",
                this::setEntry,
                StateWithEntryDTO.class,
                StateDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_do_act_of_state",
                "Set the doActivity of the specified state (specified by ID), and return the model element of the state after it is edited.",
                this::setDoActivity,
                StateWithDoActivityDTO.class,
                StateDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_exit_of_state",
                "Set the exit of the specified state (specified by ID), and return the model element of the state after it is edited.",
                this::setExit,
                StateWithExitDTO.class,
                StateDTO.class)
        );
    }

    private StateDTO getInfo(IdDTO param) throws Exception {
        IState astahState = astahProToolSupport.getState(param.id());

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO addInternalTransition(StateWithInternalTransitionDTO param) throws Exception {
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        if (astahState instanceof IFinalState) {
            throw new IllegalArgumentException(String.format(
                "The state (ID: %s) is a final state.",
                astahState.getId()));
        }

        // IState.addInternalTransition() stores null as the text "null", so a null guard or action means none
        String event = stripOrEmpty(param.event());
        String guard = stripOrEmpty(param.guard());
        String action = stripOrEmpty(param.action());

        if (event.isEmpty()) {
            throw new IllegalArgumentException("The event of an internal transition must not be empty or blank.");
        }

        // IState.addInternalTransition() accepts the same internal transition twice
        for (ITransition astahTransition : astahState.getInternalTransitions()) {
            if (event.equals(stripOrEmpty(astahTransition.getEvent()))
                && guard.equals(stripOrEmpty(astahTransition.getGuard()))
                && action.equals(stripOrEmpty(astahTransition.getAction()))) {
                throw new IllegalArgumentException(String.format(
                    "Failed to add the internal transition because the state '%s' (ID: %s) already has the internal transition '%s' (ID: %s).",
                    astahState.getName(), astahState.getId(), astahTransition.getName(), astahTransition.getId()));
            }
        }

        txnAstah.run( () -> {
            astahState.addInternalTransition(event, guard, action);
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private static String stripOrEmpty(String text) {
        return text == null ? "" : text.strip();
    }

    private StateDTO deleteAllInternalTransitions(IdDTO param) throws Exception {
        IState astahState = astahProToolSupport.getState(param.id());

        txnAstah.run( () -> {
            astahState.deleteAllInternalTransitions();
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO setEntry(StateWithEntryDTO param) throws Exception {
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        txnAstah.run( () -> {
            astahState.setEntry(param.entry());
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO setDoActivity(StateWithDoActivityDTO param) throws Exception {
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        txnAstah.run( () -> {
            astahState.setDoActivity(param.doActivity());
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO setExit(StateWithExitDTO param) throws Exception {
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        txnAstah.run( () -> {
            astahState.setExit(param.exit());
        });

        return StateDTOAssembler.toDTO(astahState);
    }
}
