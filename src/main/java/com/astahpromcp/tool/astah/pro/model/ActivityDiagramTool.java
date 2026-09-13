package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ActivityDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ActivityDiagramDTOAssembler;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IActivityDiagram.html
@Slf4j
public class ActivityDiagramTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ActivityDiagramTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_activity_dgm_info",
                "Return the model element information about the specified activity diagram (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ActivityDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_activity_of_activity_dgm",
                "Return the model element of the activity for the specified activity diagram (specified by ID).",
                this::getActivity,
                IdDTO.class,
                ActivityDTO.class)
        );
    }

    private ActivityDiagramDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get activity diagram information: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.id());

        return ActivityDiagramDTOAssembler.toDTO(astahActivityDiagram);
    }

    private ActivityDTO getActivity(IdDTO param) throws Exception {
        log.debug("Get activity of activity diagram: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.id());

        return ActivityDTOAssembler.toDTO(astahActivityDiagram.getActivity());
    }
}
