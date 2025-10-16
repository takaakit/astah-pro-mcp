package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ControlNodeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ControlNodeDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IControlNode;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ControlNodeTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public ControlNodeTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.definition(
                    "get_ctrl_node_info",
                    "Return detailed information about the specified control node (specified by ID).",
                    this::getInfo,
                    IdDTO.class,
                    ControlNodeDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create control node tools", e);
            return List.of();
        }
    }

    private ControlNodeDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get control node information: {}", param);

        IControlNode astahControlNode = astahProToolSupport.getControlNode(param.id());

        return ControlNodeDTOAssembler.toDTO(astahControlNode);
    }
}
