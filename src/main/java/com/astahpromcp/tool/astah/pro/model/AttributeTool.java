package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.AttributeDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IAttribute.html
@Slf4j
public class AttributeTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public AttributeTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create attribute tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_attr_info",
                        "Return detailed information about the specified attribute (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        AttributeDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_init_val_of_attr",
                        "Set the initial value of the specified attribute (specified by ID), and return the attribute information after it is set.",
                        this::setInitialValue,
                        AttributeWithInitialValueDTO.class,
                        AttributeDTO.class),

                ToolSupport.definition(
                        "set_static_of_attr",
                        "Set the Static of the specified attribute (specified by ID), and return the attribute information after it is set.",
                        this::setStatic,
                        AttributeWithStaticDTO.class,
                        AttributeDTO.class),

                ToolSupport.definition(
                        "set_type_of_attr",
                        "Set the type (specified by ID) of the specified attribute (specified by ID), and return the attribute information after it is set. Before using this tool function, obtain or create the type to assign to the attribute type. If you want to set a primitive type, use a different tool function.",
                        this::setType,
                        AttributeWithTypeDTO.class,
                        AttributeDTO.class),

                ToolSupport.definition(
                        "set_type_expression_of_attr",
                        "Set the type expression (specified by string) of the specified attribute (specified by ID), and return the attribute information after it is set. Use this tool function to set a primitive type for an attribute only when you want to set a Java or C++ primitive type. If it is not a primitive type, obtain or create the type and then set it to the attribute type. For example, 'int' and 'string' are primitive types, whereas 'Integer' and 'String' require creating a type before they can be used.",
                        this::setTypeExpression,
                        AttributeWithTypeExpressionDTO.class,
                        AttributeDTO.class),

                ToolSupport.definition(
                        "set_multiplicity_of_attr",
                        "Set the upper and lower multiplicity (specified by string) of the specified attribute (specified by ID), and return the attribute information after it is set. If there is only one multiplicity, set either the upper or the lower multiplicity, and set the other to an empty string.",
                        this::setMultiplicity,
                        AttributeWithMultiplicityDTO.class,
                        AttributeDTO.class)
        );
    }

    private AttributeDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get info of attribute: {}", param);

        IAttribute astahAttribute = astahProToolSupport.getAttribute(param.id());

        return AttributeDTOAssembler.toDTO(astahAttribute);
    }

    private AttributeDTO setInitialValue(McpSyncServerExchange exchange, AttributeWithInitialValueDTO param) throws Exception {
        log.debug("Set initial value of attribute: {}", param);

        IAttribute astahAttribute = astahProToolSupport.getAttribute(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAttribute.setInitialValue(param.initialValue());
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAttribute);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO setStatic(McpSyncServerExchange exchange, AttributeWithStaticDTO param) throws Exception {
        log.debug("Set static of attribute: {}", param);

        IAttribute astahAttribute = astahProToolSupport.getAttribute(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAttribute.setStatic(param.isStatic());
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAttribute);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO setType(McpSyncServerExchange exchange, AttributeWithTypeDTO param) throws Exception {
        log.debug("Set type of attribute: {}", param);

        IAttribute astahAttribute = astahProToolSupport.getAttribute(param.targetAttributeId());
        IClass astahType = astahProToolSupport.getClass(param.attributeTypeId());

        try {
            transactionManager.beginTransaction();
            astahAttribute.setType(astahType);
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAttribute);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO setTypeExpression(McpSyncServerExchange exchange, AttributeWithTypeExpressionDTO param) throws Exception {
        log.debug("Set type expression of attribute: {}", param);

        IAttribute astahAttribute = astahProToolSupport.getAttribute(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAttribute.setTypeExpression(param.typeExpression());
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAttribute);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO setMultiplicity(McpSyncServerExchange exchange, AttributeWithMultiplicityDTO param) throws Exception {
        log.debug("Set multiplicity of attribute: {}", param);

        IAttribute astahAttribute = astahProToolSupport.getAttribute(param.targetAttributeId());

        String lowerStringMultiplicity = param.lowerMultiplicity();
        String upperStringMultiplicity = param.upperMultiplicity();

        if (!lowerStringMultiplicity.isEmpty() && !upperStringMultiplicity.isEmpty()) {
            try {
                transactionManager.beginTransaction();
                astahAttribute.setMultiplicityStrings(new String[][]{{lowerStringMultiplicity, upperStringMultiplicity}});
                transactionManager.endTransaction();
            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
        } else if (!lowerStringMultiplicity.isEmpty() && upperStringMultiplicity.isEmpty()) {
            try {
                transactionManager.beginTransaction();
                astahAttribute.setMultiplicityString(lowerStringMultiplicity);
                transactionManager.endTransaction();
            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
        } else if (lowerStringMultiplicity.isEmpty() && !upperStringMultiplicity.isEmpty()) {
            try {
                transactionManager.beginTransaction();
                astahAttribute.setMultiplicityString(upperStringMultiplicity);
                transactionManager.endTransaction();
            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
        } else {
            throw new IllegalArgumentException("Lower multiplicity and upper multiplicity are both empty.");
        }

        return AttributeDTOAssembler.toDTO(astahAttribute);
    }
}
