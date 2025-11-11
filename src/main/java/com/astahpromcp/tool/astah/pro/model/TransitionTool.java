package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.TransitionWithActionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.TransitionWithEventDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.TransitionWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.TransitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.TransitionDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ITransition.html
@Slf4j
public class TransitionTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public TransitionTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
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
            log.error("Failed to create transition tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_trans_info",
                        "Return detailed information about the specified transition (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        TransitionDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_act_of_trans",
                        "Set the action of the specified transition (specified by ID), and return the transition information after it is set.",
                        this::setAction,
                        TransitionWithActionDTO.class,
                        TransitionDTO.class),

                ToolSupport.definition(
                        "set_event_of_trans",
                        "Set the event of the specified transition (specified by ID), and return the transition information after it is set.",
                        this::setEvent,
                        TransitionWithEventDTO.class,
                        TransitionDTO.class),

                ToolSupport.definition(
                        "set_guard_of_trans",
                        "Set the guard of the specified transition (specified by ID), and return the transition information after it is set.",
                        this::setGuard,
                        TransitionWithGuardDTO.class,
                        TransitionDTO.class)
        );
    }

    private TransitionDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get transition information: {}", param);

        ITransition astahTransition = astahProToolSupport.getTransition(param.id());

        return TransitionDTOAssembler.toDTO(astahTransition);
    }

    private TransitionDTO setAction(McpSyncServerExchange exchange, TransitionWithActionDTO param) throws Exception {
        log.debug("Set action of transition: {}", param);

        ITransition astahTransition = astahProToolSupport.getTransition(param.targetTransitionId());

        try {
            transactionManager.beginTransaction();
            astahTransition.setAction(param.action());
            transactionManager.endTransaction();

            return TransitionDTOAssembler.toDTO(astahTransition);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private TransitionDTO setEvent(McpSyncServerExchange exchange, TransitionWithEventDTO param) throws Exception {
        log.debug("Set event of transition: {}", param);

        ITransition astahTransition = astahProToolSupport.getTransition(param.targetTransitionId());

        try {
            transactionManager.beginTransaction();
            astahTransition.setEvent(param.event());
            transactionManager.endTransaction();

            return TransitionDTOAssembler.toDTO(astahTransition);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private TransitionDTO setGuard(McpSyncServerExchange exchange, TransitionWithGuardDTO param) throws Exception {
        log.debug("Set guard of transition: {}", param);

        ITransition astahTransition = astahProToolSupport.getTransition(param.targetTransitionId());

        try {
            transactionManager.beginTransaction();
            astahTransition.setGuard(param.guard());
            transactionManager.endTransaction();

            return TransitionDTOAssembler.toDTO(astahTransition);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
