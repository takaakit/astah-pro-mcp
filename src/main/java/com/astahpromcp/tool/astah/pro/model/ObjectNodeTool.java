package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ObjectNodeWithBaseDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ObjectNodeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ObjectNodeDTOAssembler;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IObjectNode;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

@Slf4j
public class ObjectNodeTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ObjectNodeTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_obj_node_info",
                "Return model element information about the specified object node (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ObjectNodeDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_base_class_of_obj_node",
                "Set the base class (specified by ID) of the specified object node (specified by ID), and return the model element of the object node after it is set.",
                this::setBase,
                ObjectNodeWithBaseDTO.class,
                ObjectNodeDTO.class)
        );
    }

    private ObjectNodeDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get object node information: {}", param);

        IObjectNode astahObjectNode = astahProToolSupport.getObjectNode(param.id());

        return ObjectNodeDTOAssembler.toDTO(astahObjectNode);
    }

    private ObjectNodeDTO setBase(ObjectNodeWithBaseDTO param) throws Exception {
        log.debug("Set base class of object node: {}", param);

        IObjectNode astahObjectNode = astahProToolSupport.getObjectNode(param.targetObjectNodeId());
        IClass astahBaseClass = astahProToolSupport.getClassOrPrimitiveType(param.baseClassId());

        txnAstah.run( () -> {
            astahObjectNode.setBase(astahBaseClass);
        });

        return ObjectNodeDTOAssembler.toDTO(astahObjectNode);
    }
}
