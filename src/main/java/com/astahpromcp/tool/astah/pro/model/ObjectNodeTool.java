package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ObjectNodeWithBaseDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ObjectNodeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ObjectNodeDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IObjectNode;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ObjectNodeTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ObjectNodeTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create object node tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_obj_node_info",
                        "Return detailed information about the specified object node (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        ObjectNodeDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_base_cls_of_obj_node",
                        "Set the base class (specified by ID) of the specified object node (specified by ID), and return the object node information after it is set.",
                        this::setBase,
                        ObjectNodeWithBaseDTO.class,
                        ObjectNodeDTO.class)
        );
    }

    private ObjectNodeDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get object node information: {}", param);

        IObjectNode astahObjectNode = astahProToolSupport.getObjectNode(param.id());

        return ObjectNodeDTOAssembler.toDTO(astahObjectNode);
    }

    private ObjectNodeDTO setBase(McpSyncServerExchange exchange, ObjectNodeWithBaseDTO param) throws Exception {
        log.debug("Set base class of object node: {}", param);

        IObjectNode astahObjectNode = astahProToolSupport.getObjectNode(param.targetObjectNodeId());
        IClass astahBaseClass = astahProToolSupport.getClass(param.baseClassId());

        try {
            transactionManager.beginTransaction();
            astahObjectNode.setBase(astahBaseClass);
            transactionManager.endTransaction();

            return ObjectNodeDTOAssembler.toDTO(astahObjectNode);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
