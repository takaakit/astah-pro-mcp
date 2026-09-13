package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.OperationDTOAssembler;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IOperation.html
@Slf4j
public class OperationTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public OperationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_ope_info",
                "Return model element information about the specified operation (specified by ID).",
                this::getInfo,
                IdDTO.class,
                OperationDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_abstract_of_ope",
                "Set the Abstract of the specified operation (specified by ID), and return the model element of the operation after it is set.",
                this::setAbstract,
                OperationWithAbstractDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_leaf_of_ope",
                "Set the Leaf of the specified operation (specified by ID), and return the model element of the operation after it is set.",
                this::setLeaf,
                OperationWithLeafDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_static_of_ope",
                "Set the Static of the specified operation (specified by ID), and return the model element of the operation after it is set.",
                this::setStatic,
                OperationWithStaticDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_return_type_of_ope",
                "Set the return type (specified by ID) of the specified operation (specified by ID), and return the model element of the operation after it is set. Before using this tool function, obtain or create the type to assign to the return type.",
                this::setReturnType,
                OperationWithReturnTypeDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_return_type_expression_of_ope",
                "Set the return type expression (specified by string) of the specified operation (specified by ID), and return the model element of the operation after it is set. Use this tool function to set a primitive type for an operation only when you want to set a Java or C++ primitive type. If it is not a primitive type, obtain or create the type and then set it to the operation return type. For example, 'int' and 'string' are primitive types, whereas 'Integer', 'String' and 'List<int>' require creating a type with that exact name before they can be used.",
                this::setReturnTypeExpression,
                OperationWithReturnTypeExpressionDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_ope_precond",
                "Add a precondition to the specified operation (specified by ID), and return the model element of the operation after it is added.",
                this::addPrecondition,
                OperationWithPreconditionDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_ope_postcond",
                "Add a postcondition to the specified operation (specified by ID), and return the model element of the operation after it is added.",
                this::addPostcondition,
                OperationWithPostconditionDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_ope_bodycond",
                "Set the body condition of the specified operation (specified by ID), and return the model element of the operation after it is set. An operation can have only one body condition.",
                this::setBodyCondition,
                OperationWithBodyConditionDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_ope_precond",
                "Remove the specified precondition from the specified operation (specified by ID), and return the model element of the operation after it is removed.",
                this::removePrecondition,
                OperationWithPreconditionDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_ope_postcond",
                "Remove the specified postcondition from the specified operation (specified by ID), and return the model element of the operation after it is removed.",
                this::removePostcondition,
                OperationWithPostconditionDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_ope_bodycond",
                "Remove the body condition from the specified operation (specified by ID), and return the model element of the operation after it is removed.",
                this::removeBodyCondition,
                IdDTO.class,
                OperationDTO.class)
        );
    }

    private OperationDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get operation information: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.id());

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO setAbstract(OperationWithAbstractDTO param) throws Exception {
        log.debug("Set abstract of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.setAbstract(param.isAbstract());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO setLeaf(OperationWithLeafDTO param) throws Exception {
        log.debug("Set leaf of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.setLeaf(param.isLeaf());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO setStatic(OperationWithStaticDTO param) throws Exception {
        log.debug("Set static of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.setStatic(param.isStatic());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO setReturnType(OperationWithReturnTypeDTO param) throws Exception {
        log.debug("Set return type of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());
        IClass astahReturnType = astahProToolSupport.getClassOrPrimitiveType(param.returnTypeId());

        txnAstah.run( () -> {
            astahOperation.setReturnType(astahReturnType);
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO setReturnTypeExpression(OperationWithReturnTypeExpressionDTO param) throws Exception {
        log.debug("Set return type expression of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        try {
            txnAstah.run( () -> {
                astahOperation.setReturnTypeExpression(param.returnTypeExpression());
            });

        } catch (InvalidEditingException e) {
            throw new InvalidEditingException(
                e.getKey(),
                e.getMessage() + " If the type is not a primitive type, create a model element with that exact name before setting it (for example, 'String' and 'List<int>').");
        }

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO addPrecondition(OperationWithPreconditionDTO param) throws Exception {
        log.debug("Add precondition to operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.addPreCondition(param.precondition());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO addPostcondition(OperationWithPostconditionDTO param) throws Exception {
        log.debug("Add postcondition to operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.addPostCondition(param.postcondition());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO setBodyCondition(OperationWithBodyConditionDTO param) throws Exception {
        log.debug("Set body condition of operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.setBodyCondition(param.bodyCondition());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO removePrecondition(OperationWithPreconditionDTO param) throws Exception {
        log.debug("Remove precondition from operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.removePreCondition(param.precondition());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO removePostcondition(OperationWithPostconditionDTO param) throws Exception {
        log.debug("Remove postcondition from operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.targetOperationId());

        txnAstah.run( () -> {
            astahOperation.removePostCondition(param.postcondition());
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }

    private OperationDTO removeBodyCondition(IdDTO param) throws Exception {
        log.debug("Remove body condition from operation: {}", param);

        IOperation astahOperation = astahProToolSupport.getOperation(param.id());

        txnAstah.run( () -> {
            astahOperation.setBodyCondition("");
        });

        return OperationDTOAssembler.toDTO(astahOperation);
    }
}
