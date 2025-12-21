package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ActionWithCallingActivityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ActionDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAction;
import com.change_vision.jude.api.inf.model.IActivity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IAction.html
@Slf4j
public class ActionTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ActionTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create action tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_action_info",
                        "Return detailed information about the specified action (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        ActionDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_calling_activity_of_action",
                        "Set the calling activity (specified by ID) of the specified action (specified by ID), and return the action information after it is set.",
                        this::setCallingActivity,
                        ActionWithCallingActivityDTO.class,
                        ActionDTO.class)
        );
    }

    private ActionDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get action information: {}", param);

        IAction astahAction = astahProToolSupport.getAction(param.id());

        return ActionDTOAssembler.toDTO(astahAction);
    }

    private ActionDTO setCallingActivity(McpSyncServerExchange exchange, ActionWithCallingActivityDTO param) throws Exception {
        log.debug("Set calling activity of action: {}", param);

        IAction astahAction = astahProToolSupport.getAction(param.targetActionId());
        IActivity astahCallingActivity = astahProToolSupport.getActivity(param.callingActivityId());

        try {
            transactionManager.beginTransaction();
            astahAction.setCallingActivity(astahCallingActivity);
            transactionManager.endTransaction();

            return ActionDTOAssembler.toDTO(astahAction);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
