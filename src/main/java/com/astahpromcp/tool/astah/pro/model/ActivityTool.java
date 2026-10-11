package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ActivityDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.model.IActivity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IActivity.html
@Slf4j
public class ActivityTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ActivityTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_activity_info",
                "Return the model element information about the specified activity (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ActivityDTO.class)
        );
    }

    private ActivityDTO getInfo(IdDTO param) throws Exception {
        IActivity astahActivity = astahProToolSupport.getActivity(param.id());

        try {
            astahActivity.getActivityDiagram();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                "The activity '%s' (ID: %s) is the one Astah keeps behind a data flow diagram, not the activity of an activity diagram, so its activity nodes and flows cannot be read. Use get_named_element_info for its basic information.",
                astahActivity.getName(), astahActivity.getId()));
        }

        return ActivityDTOAssembler.toDTO(astahActivity);
    }
}
