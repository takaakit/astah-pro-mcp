package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ControlNodeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ControlNodeDTOAssembler;
import com.change_vision.jude.api.inf.model.IControlNode;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

@Slf4j
public class ControlNodeTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ControlNodeTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_control_node_info",
                "Return model element information about the specified control node (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ControlNodeDTO.class)
        );
    }

    private ControlNodeDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get control node information: {}", param);

        IControlNode astahControlNode = astahProToolSupport.getControlNode(param.id());

        return ControlNodeDTOAssembler.toDTO(astahControlNode);
    }
}
