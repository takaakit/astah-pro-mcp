package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithDoActivityDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithEntryDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithExitDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithInternalTransitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IState.html
@Slf4j
public class StateTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public StateTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.definition(
                "get_state_info",
                "Return detailed information about the specified state (specified by ID).",
                this::getInfo,
                IdDTO.class,
                StateDTO.class),

            ToolSupport.definition(
                "add_inter_trans_of_state",
                "Add an internal transition to the specified state (specified by ID), and return the state information after it is edited.",
                this::addInternalTransition,
                StateWithInternalTransitionDTO.class,
                StateDTO.class),

            ToolSupport.definition(
                "delete_all_inter_trans_of_state",
                "Delete all internal transitions from the specified state (specified by ID), and return the state information after it is edited.",
                this::deleteAllInternalTransitions,
                IdDTO.class,
                StateDTO.class),

            ToolSupport.definition(
                "set_entry_of_state",
                "Set the entry of the specified state (specified by ID), and return the state information after it is edited.",
                this::setEntry,
                StateWithEntryDTO.class,
                StateDTO.class),

            ToolSupport.definition(
                "set_do_act_of_state",
                "Set the doActivity of the specified state (specified by ID), and return the state information after it is edited.",
                this::setDoActivity,
                StateWithDoActivityDTO.class,
                StateDTO.class),

            ToolSupport.definition(
                "set_exit_of_state",
                "Set the exit of the specified state (specified by ID), and return the state information after it is edited.",
                this::setExit,
                StateWithExitDTO.class,
                StateDTO.class)
        );
    }

    private StateDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get state information: {}", param);

        IState astahState = astahProToolSupport.getState(param.id());

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO addInternalTransition(McpSyncServerExchange exchange, StateWithInternalTransitionDTO param) throws Exception {
        log.debug("Add internal transition to state: {}", param);
        
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        try {
            transactionManager.beginTransaction();
            astahState.addInternalTransition(
                param.event(),
                param.guard(),
                param.action());
            transactionManager.endTransaction();

            return StateDTOAssembler.toDTO(astahState);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private StateDTO deleteAllInternalTransitions(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Delete all internal transitions from state: {}", param);

        IState astahState = astahProToolSupport.getState(param.id());

        try {
            transactionManager.beginTransaction();
            astahState.deleteAllInternalTransitions();
            transactionManager.endTransaction();

            return StateDTOAssembler.toDTO(astahState);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private StateDTO setEntry(McpSyncServerExchange exchange, StateWithEntryDTO param) throws Exception {
        log.debug("Set entry of state: {}", param);
        
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        try {
            transactionManager.beginTransaction();
            astahState.setEntry(param.entry());
            transactionManager.endTransaction();

            return StateDTOAssembler.toDTO(astahState);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private StateDTO setDoActivity(McpSyncServerExchange exchange, StateWithDoActivityDTO param) throws Exception {
        log.debug("Set doActivity of state: {}", param);
        
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        try {
            transactionManager.beginTransaction();
            astahState.setDoActivity(param.doActivity());
            transactionManager.endTransaction();

            return StateDTOAssembler.toDTO(astahState);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private StateDTO setExit(McpSyncServerExchange exchange, StateWithExitDTO param) throws Exception {
        log.debug("Set exit of state: {}", param);
        
        IState astahState = astahProToolSupport.getState(param.targetStateId());

        try {
            transactionManager.beginTransaction();
            astahState.setExit(param.exit());
            transactionManager.endTransaction();

            return StateDTOAssembler.toDTO(astahState);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
