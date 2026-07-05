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
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.StateDTOAssembler;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IState.html
@Slf4j
public class StateTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public StateTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create state tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_state_info",
                "Return model element information about the specified state (specified by ID).",
                this::getInfo,
                IdDTO.class,
                StateDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
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

    private StateDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get state information: {}", param);

        IState astahState = astahProToolSupport.getState(param.id());

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO addInternalTransition(McpSyncServerExchange exchange, StateWithInternalTransitionDTO param) throws Exception {
        log.debug("Add internal transition to state: {}", param);

        IState astahState = astahProToolSupport.getState(param.targetStateId());

        txnAstah.run( () -> {
            astahState.addInternalTransition(
                param.event(),
                param.guard(),
                param.action());
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO deleteAllInternalTransitions(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Delete all internal transitions from state: {}", param);

        IState astahState = astahProToolSupport.getState(param.id());

        txnAstah.run( () -> {
            astahState.deleteAllInternalTransitions();
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO setEntry(McpSyncServerExchange exchange, StateWithEntryDTO param) throws Exception {
        log.debug("Set entry of state: {}", param);

        IState astahState = astahProToolSupport.getState(param.targetStateId());

        txnAstah.run( () -> {
            astahState.setEntry(param.entry());
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO setDoActivity(McpSyncServerExchange exchange, StateWithDoActivityDTO param) throws Exception {
        log.debug("Set doActivity of state: {}", param);

        IState astahState = astahProToolSupport.getState(param.targetStateId());

        txnAstah.run( () -> {
            astahState.setDoActivity(param.doActivity());
        });

        return StateDTOAssembler.toDTO(astahState);
    }

    private StateDTO setExit(McpSyncServerExchange exchange, StateWithExitDTO param) throws Exception {
        log.debug("Set exit of state: {}", param);

        IState astahState = astahProToolSupport.getState(param.targetStateId());

        txnAstah.run( () -> {
            astahState.setExit(param.exit());
        });

        return StateDTOAssembler.toDTO(astahState);
    }
}
