package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ActionWithCallingActivityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ActionDTOAssembler;
import com.change_vision.jude.api.inf.model.IAction;
import com.change_vision.jude.api.inf.model.IActivity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IAction.html
@Slf4j
public class ActionTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ActionTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_action_info",
                "Return the model element information about the specified action (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ActionDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_calling_activity_of_action",
                "Set the calling activity (specified by ID) of the specified action (specified by ID), and return the model element of the action after it is set.",
                this::setCallingActivity,
                ActionWithCallingActivityDTO.class,
                ActionDTO.class)
        );
    }

    private ActionDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get action information: {}", param);

        IAction astahAction = astahProToolSupport.getAction(param.id());

        return ActionDTOAssembler.toDTO(astahAction);
    }

    private ActionDTO setCallingActivity(ActionWithCallingActivityDTO param) throws Exception {
        log.debug("Set calling activity of action: {}", param);

        IAction astahAction = astahProToolSupport.getAction(param.targetActionId());
        IActivity astahCallingActivity = astahProToolSupport.getActivity(param.callingActivityId());

        txnAstah.run( () -> {
            astahAction.setCallingActivity(astahCallingActivity);
        });

        return ActionDTOAssembler.toDTO(astahAction);
    }
}
