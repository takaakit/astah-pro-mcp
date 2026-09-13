package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityNodeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ActivityNodeDTOAssembler;
import com.change_vision.jude.api.inf.model.IActivityNode;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IActivityNode.html
@Slf4j
public class ActivityNodeTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ActivityNodeTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_activity_node_info",
                "Return the model element information about the specified activity node (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ActivityNodeDTO.class)
        );
    }

    private ActivityNodeDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get activity node information: {}", param);

        IActivityNode astahActivityNode = astahProToolSupport.getActivityNode(param.id());

        return ActivityNodeDTOAssembler.toDTO(astahActivityNode);
    }
}
