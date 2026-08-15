package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.ParameterWithTypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ParameterWithTypeExpressionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ParameterDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ParameterDTOAssembler;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IParameter;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IParameter.html
@Slf4j
public class ParameterTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ParameterTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
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
            log.error("Failed to create parameter tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_type_of_param",
                "Set the type (specified by ID) of the specified parameter (specified by ID), and return the model element of the parameter after it is set. Before using this tool function, obtain or create the type to assign to the parameter type. If you want to set a primitive type, use a different tool function.",
                this::setType,
                ParameterWithTypeDTO.class,
                ParameterDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_type_expression_of_param",
                "Set the type expression (specified by string) of the specified parameter (specified by ID), and return the model element of the parameter after it is set. If it is not a primitive type, obtain or create the type and then set it to the parameter type. For example, 'int' and 'string' are primitive types, whereas 'Integer' and 'String' require creating a type before they can be used.",
                this::setTypeExpression,
                ParameterWithTypeExpressionDTO.class,
                ParameterDTO.class)
        );
    }

    private ParameterDTO setType(ParameterWithTypeDTO param) throws Exception {
        log.debug("Set type of parameter: {}", param);

        IParameter astahParameter = astahProToolSupport.getParameter(param.targetParameterId());
        IClass astahType = astahProToolSupport.getClass(param.parameterTypeId());

        txnAstah.run( () -> {
            astahParameter.setType(astahType);
        });

        return ParameterDTOAssembler.toDTO(astahParameter);
    }

    private ParameterDTO setTypeExpression(ParameterWithTypeExpressionDTO param) throws Exception {
        log.debug("Set type expression of parameter: {}", param);

        IParameter astahParameter = astahProToolSupport.getParameter(param.targetParameterId());

        try {
            txnAstah.run( () -> {
                astahParameter.setTypeExpression(param.typeExpression());
            });

        } catch (InvalidEditingException e) {
            throw new InvalidEditingException(
                e.getKey(),
                e.getMessage() + " If the type is not a primitive type, create a model element for that type before setting it.");
        }

        return ParameterDTOAssembler.toDTO(astahParameter);
    }
}
