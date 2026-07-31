package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithDefaultValueDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithERDatatypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithLengthPrecisionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithNotNullDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithParentERDomainDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDomainDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERDomainDTOAssembler;
import com.change_vision.jude.api.inf.model.IERDatatype;
import com.change_vision.jude.api.inf.model.IERDomain;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERDomain.html
@Slf4j
public class ERDomainTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERDomainTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER domain tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_er_domain_info",
                "Return model element information about the specified ER domain (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ERDomainDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_er_datatype_of_er_domain",
                "Set the ER datatype (specified by ID) of the specified ER domain (specified by ID), and return the model element of the ER domain after it is set.",
                this::setDatatype,
                ERDomainWithERDatatypeDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_default_value_of_er_domain",
                "Set the default value (specified by string) of the specified ER domain (specified by ID), and return the model element of the ER domain after it is set.",
                this::setDefaultValue,
                ERDomainWithDefaultValueDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_length_precision_of_er_domain",
                "Set the length/precision (specified by string) of the specified ER domain (specified by ID), and return the model element of the ER domain after it is set. Whether you can specify length and precision depends on the data type. For example, when the data type is CHAR, only the length can be specified; if both length and precision are specified, an error will occur.",
                this::setLengthPrecision,
                ERDomainWithLengthPrecisionDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_logical_name_of_er_domain",
                "Set the logical name (specified by string) of the specified ER domain (specified by ID), and return the model element of the ER domain after it is set.",
                this::setLogicalName,
                ERDomainWithLogicalNameDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_physical_name_of_er_domain",
                "Set the physical name (specified by string) of the specified ER domain (specified by ID), and return the model element of the ER domain after it is set.",
                this::setPhysicalName,
                ERDomainWithPhysicalNameDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_not_null_of_er_domain",
                "Set the NOT NULL of the specified ER domain (specified by ID), and return the model element of the ER domain after it is set.",
                this::setNotNull,
                ERDomainWithNotNullDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_parent_er_domain_of_er_domain",
                "Set the parent ER domain (specified by ID) of the specified ER domain (specified by ID), and return the model element of the ER domain after it is set.",
                this::setParentERDomain,
                ERDomainWithParentERDomainDTO.class,
                ERDomainDTO.class)
        );
    }

    private ERDomainDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get ER domain information: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.id());

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }

    private ERDomainDTO setDatatype(ERDomainWithERDatatypeDTO param) throws Exception {
        log.debug("Set ER datatype of ER domain: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.targetERDomainId());
        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        txnAstah.run( () -> {
            astahERDomain.setDatatype(astahERDatatype);
        });

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }

    private ERDomainDTO setDefaultValue(ERDomainWithDefaultValueDTO param) throws Exception {
        log.debug("Set default value of ER domain: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.targetERDomainId());

        txnAstah.run( () -> {
            astahERDomain.setDefaultValue(param.defaultValue());
        });

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }

    private ERDomainDTO setLengthPrecision(ERDomainWithLengthPrecisionDTO param) throws Exception {
        log.debug("Set length/precision of ER domain: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.targetERDomainId());

        txnAstah.run( () -> {
            astahERDomain.setLengthPrecision(param.lengthPrecision());
        });

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }

    private ERDomainDTO setLogicalName(ERDomainWithLogicalNameDTO param) throws Exception {
        log.debug("Set logical name of ER domain: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.targetERDomainId());

        txnAstah.run( () -> {
            astahERDomain.setLogicalName(param.logicalName());
        });

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }

    private ERDomainDTO setPhysicalName(ERDomainWithPhysicalNameDTO param) throws Exception {
        log.debug("Set physical name of ER domain: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.targetERDomainId());

        txnAstah.run( () -> {
            astahERDomain.setPhysicalName(param.physicalName());
        });

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }

    private ERDomainDTO setNotNull(ERDomainWithNotNullDTO param) throws Exception {
        log.debug("Set not null of ER domain: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.targetERDomainId());

        txnAstah.run( () -> {
            astahERDomain.setNotNull(param.isNotNull());
        });

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }

    private ERDomainDTO setParentERDomain(ERDomainWithParentERDomainDTO param) throws Exception {
        log.debug("Set parent ER domain of ER domain: {}", param);

        IERDomain astahERDomain = astahProToolSupport.getERDomain(param.targetERDomainId());
        IERDomain astahParentERDomain = astahProToolSupport.getERDomain(param.parentERDomainId());

        txnAstah.run( () -> {
            astahERDomain.setParentDomain(astahParentERDomain);
        });

        return ERDomainDTOAssembler.toDTO(astahERDomain);
    }
}
