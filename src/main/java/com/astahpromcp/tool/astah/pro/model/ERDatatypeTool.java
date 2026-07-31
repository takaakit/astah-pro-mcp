package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithDefaultLengthPrecisionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithDescriptionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithLengthConstraintDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithPrecisionConstraintDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDatatypeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERDatatypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERDatatype;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import java.util.List;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERDatatype.html
@Slf4j
public class ERDatatypeTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERDatatypeTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER datatype tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_er_datatype_info",
                "Return model element information about the specified ER datatype (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ERDatatypeDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_length_constraint_of_er_datatype",
                "Set the length constraint (specified by string) of the specified ER datatype (specified by ID), and return the model element of the ER datatype after it is set.",
                this::setLengthConstraint,
                ERDatatypeWithLengthConstraintDTO.class,
                ERDatatypeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_precision_constraint_of_er_datatype",
                "Set the precision constraint (specified by string) of the specified ER datatype (specified by ID), and return the model element of the ER datatype after it is set.",
                this::setPrecisionConstraint,
                ERDatatypeWithPrecisionConstraintDTO.class,
                ERDatatypeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_default_length_precision_of_er_datatype",
                "Set the default length/precision (specified by string) of the specified ER datatype (specified by ID), and return the model element of the ER datatype after it is set.",
                this::setDefaultLengthPrecision,
                ERDatatypeWithDefaultLengthPrecisionDTO.class,
                ERDatatypeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_description_of_er_datatype",
                "Set the description (specified by string) of the specified ER datatype (specified by ID), and return the model element of the ER datatype after it is set.",
                this::setDescription,
                ERDatatypeWithDescriptionDTO.class,
                ERDatatypeDTO.class)
        );
    }

    private ERDatatypeDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get ER datatype information: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.id());

        return ERDatatypeDTOAssembler.toDTO(astahERDatatype);
    }

    private ERDatatypeDTO setLengthConstraint(ERDatatypeWithLengthConstraintDTO param) throws Exception {
        log.debug("Set length constraint of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        txnAstah.run( () -> {
            astahERDatatype.setLengthConstraint(param.lengthConstraint());
        });

        return ERDatatypeDTOAssembler.toDTO(astahERDatatype);
    }

    private ERDatatypeDTO setPrecisionConstraint(ERDatatypeWithPrecisionConstraintDTO param) throws Exception {
        log.debug("Set precision constraint of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        txnAstah.run( () -> {
            astahERDatatype.setPrecisionConstraint(param.precisionConstraint());
        });

        return ERDatatypeDTOAssembler.toDTO(astahERDatatype);
    }

    private ERDatatypeDTO setDefaultLengthPrecision(ERDatatypeWithDefaultLengthPrecisionDTO param) throws Exception {
        log.debug("Set default length/precision of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        txnAstah.run( () -> {
            astahERDatatype.setDefaultLengthPrecision(param.defaultLengthPrecision());
        });

        return ERDatatypeDTOAssembler.toDTO(astahERDatatype);
    }

    private ERDatatypeDTO setDescription(ERDatatypeWithDescriptionDTO param) throws Exception {
        log.debug("Set description of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        txnAstah.run( () -> {
            astahERDatatype.setDefinition(param.description());
        });

        return ERDatatypeDTOAssembler.toDTO(astahERDatatype);
    }
}
