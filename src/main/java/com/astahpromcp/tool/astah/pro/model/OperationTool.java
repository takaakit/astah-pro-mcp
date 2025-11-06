package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IOperation.html
@Slf4j
public class OperationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public OperationTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create operation tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_ope_info",
                        "Return detailed information about the specified operation (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        OperationDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_abs_of_ope",
                        "Set the Abstract of the specified operation (specified by ID), and return the operation information after it is set.",
                        this::setAbstract,
                        OperationWithAbstractDTO.class,
                        OperationDTO.class),

                ToolSupport.definition(
                        "set_leaf_of_ope",
                        "Set the Leaf of the specified operation (specified by ID), and return the operation information after it is set.",
                        this::setLeaf,
                        OperationWithLeafDTO.class,
                        OperationDTO.class),

                ToolSupport.definition(
                        "set_static_of_ope",
                        "Set the Static of the specified operation (specified by ID), and return the operation information after it is set.",
                        this::setStatic,
                        OperationWithStaticDTO.class,
                        OperationDTO.class),

                ToolSupport.definition(
                        "set_type_of_ope",
                        "Set the return type (specified by ID) of the specified operation (specified by ID), and return the operation information after it is set. Before using this tool function, obtain or create the type to assign to the return type. If you want to set a primitive type, use a different tool function.",
                        this::setReturnType,
                        OperationWithReturnTypeDTO.class,
                        OperationDTO.class),

                ToolSupport.definition(
                        "set_type_exp_of_ope",
                        "Set the return type expression (specified by string) of the specified operation (specified by ID), and return the operation information after it is set. Use this tool function to set a primitive type for an operation only when you want to set a Java or C++ primitive type. If it is not a primitive type, obtain or create the type and then set it to the operation return type. For example, 'int' and 'string' are primitive types, whereas 'Integer' and 'String' require creating a type before they can be used.",
                        this::setReturnTypeExpression,
                        OperationWithReturnTypeExpressionDTO.class,
                        OperationDTO.class)
        );
    }

    private OperationDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get operation information: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.id());

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO setAbstract(McpSyncServerExchange exchange, OperationWithAbstractDTO param) throws Exception {
        log.debug("Set abstract of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        try {
            transactionManager.beginTransaction();
            astahOperation.setAbstract(param.isAbstract());
            transactionManager.endTransaction();

            return OperationDTOAssembler.toDTO(astahOperation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private OperationDTO setLeaf(McpSyncServerExchange exchange, OperationWithLeafDTO param) throws Exception {
        log.debug("Set leaf of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        try {
            transactionManager.beginTransaction();
            astahOperation.setLeaf(param.isLeaf());
            transactionManager.endTransaction();

            return OperationDTOAssembler.toDTO(astahOperation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private OperationDTO setStatic(McpSyncServerExchange exchange, OperationWithStaticDTO param) throws Exception {
        log.debug("Set static of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        try {
            transactionManager.beginTransaction();
            astahOperation.setStatic(param.isStatic());
            transactionManager.endTransaction();

            return OperationDTOAssembler.toDTO(astahOperation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private OperationDTO setReturnType(McpSyncServerExchange exchange, OperationWithReturnTypeDTO param) throws Exception {
        log.debug("Set return type of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());
        IClass astahReturnType = astahProToolSupport.getClass(param.returnTypeId());

        try {
            transactionManager.beginTransaction();
            astahOperation.setReturnType(astahReturnType);
            transactionManager.endTransaction();

            return OperationDTOAssembler.toDTO(astahOperation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private OperationDTO setReturnTypeExpression(McpSyncServerExchange exchange, OperationWithReturnTypeExpressionDTO param) throws Exception {
        log.debug("Set return type expression of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        try {
            transactionManager.beginTransaction();
            astahOperation.setReturnTypeExpression(param.returnTypeExpression());
            transactionManager.endTransaction();

            return OperationDTOAssembler.toDTO(astahOperation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
