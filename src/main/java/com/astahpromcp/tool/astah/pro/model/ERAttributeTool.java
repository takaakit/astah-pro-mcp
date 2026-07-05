package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERAttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERAttributeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERDatatype;
import com.change_vision.jude.api.inf.model.IERDomain;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERAttribute.html
@Slf4j
public class ERAttributeTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERAttributeTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER attribute tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_er_attr_info",
                "Return model element information about the specified ER attribute (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ERAttributeDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_er_datatype_of_er_attr",
                "Set the ER datatype (specified by ID) of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set.",
                this::setDatatype,
                ERAttributeWithERDatatypeDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_er_domain_of_er_attr",
                "Set the ER domain (specified by ID) of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set.",
                this::setDomain,
                ERAttributeWithERDomainDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_default_value_of_er_attr",
                "Set the default value (specified by string) of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set.",
                this::setDefaultValue,
                ERAttributeWithDefaultValueDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_length_precision_of_er_attr",
                "Set the length/precision (specified by string) of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set. Whether you can specify length and precision depends on the data type. For example, when the data type is CHAR, only the length can be specified; if both length and precision are specified, an error will occur.",
                this::setLengthPrecision,
                ERAttributeWithLengthPrecisionDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_logical_name_of_er_attr",
                "Set the logical name (specified by string) of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set.",
                this::setLogicalName,
                ERAttributeWithLogicalNameDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_physical_name_of_er_attr",
                "Set the physical name (specified by string) of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set.",
                this::setPhysicalName,
                ERAttributeWithPhysicalNameDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_primary_key_of_er_attr",
                "Set the primary key of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set.",
                this::setPrimaryKey,
                ERAttributeWithPrimaryKeyDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_not_null_of_er_attr",
                "Set the NOT NULL of the specified ER attribute (specified by ID), and return the model element of the ER attribute after it is set.",
                this::setNotNull,
                ERAttributeWithNotNullDTO.class,
                ERAttributeDTO.class)
        );
    }

    private ERAttributeDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get ER attribute information: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.id());

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setDatatype(McpSyncServerExchange exchange, ERAttributeWithERDatatypeDTO param) throws Exception {
        log.debug("Set ER datatype of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());
        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        txnAstah.run( () -> {
            astahERAttribute.setDatatype(astahERDatatype);
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setDomain(McpSyncServerExchange exchange, ERAttributeWithERDomainDTO param) throws Exception {
        log.debug("Set ER domain of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());
        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.erDomainId());

        txnAstah.run( () -> {
            astahERAttribute.setDomain(astahERDomain);
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setDefaultValue(McpSyncServerExchange exchange, ERAttributeWithDefaultValueDTO param) throws Exception {
        log.debug("Set default value of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());

        txnAstah.run( () -> {
            astahERAttribute.setDefaultValue(param.defaultValue());
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setLengthPrecision(McpSyncServerExchange exchange, ERAttributeWithLengthPrecisionDTO param) throws Exception {
        log.debug("Set length/precision of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());

        txnAstah.run( () -> {
            astahERAttribute.setLengthPrecision(param.lengthPrecision());
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setLogicalName(McpSyncServerExchange exchange, ERAttributeWithLogicalNameDTO param) throws Exception {
        log.debug("Set logical name of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());

        txnAstah.run( () -> {
            astahERAttribute.setLogicalName(param.logicalName());
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setPhysicalName(McpSyncServerExchange exchange, ERAttributeWithPhysicalNameDTO param) throws Exception {
        log.debug("Set physical name of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());

        txnAstah.run( () -> {
            astahERAttribute.setPhysicalName(param.physicalName());
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setPrimaryKey(McpSyncServerExchange exchange, ERAttributeWithPrimaryKeyDTO param) throws Exception {
        log.debug("Set primary key of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());

        txnAstah.run( () -> {
            astahERAttribute.setPrimaryKey(param.isPrimaryKey());
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }

    private ERAttributeDTO setNotNull(McpSyncServerExchange exchange, ERAttributeWithNotNullDTO param) throws Exception {
        log.debug("Set not null of ER attribute: {}", param);

        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.targetERAttributeId());

        txnAstah.run( () -> {
            astahERAttribute.setNotNull(param.isNotNull());
        });

        return ERAttributeDTOAssembler.toDTO(astahERAttribute);
    }
}
