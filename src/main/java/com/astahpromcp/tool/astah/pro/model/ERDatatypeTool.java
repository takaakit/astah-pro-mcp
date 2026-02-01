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
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERDatatype;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import java.util.List;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERDatatype.html
@Slf4j
public class ERDatatypeTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERDatatypeTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER datatype tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_er_datatype_info",
                        "Return detailed information about the specified ER datatype (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        ERDatatypeDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_length_constraint_of_er_datatype",
                        "Set the length constraint (specified by string) of the specified ER datatype (specified by ID), and return the ER datatype information after it is set.",
                        this::setLengthConstraint,
                        ERDatatypeWithLengthConstraintDTO.class,
                        ERDatatypeDTO.class),

                ToolSupport.definition(
                        "set_precision_constraint_of_er_datatype",
                        "Set the precision constraint (specified by string) of the specified ER datatype (specified by ID), and return the ER datatype information after it is set.",
                        this::setPrecisionConstraint,
                        ERDatatypeWithPrecisionConstraintDTO.class,
                        ERDatatypeDTO.class),

                ToolSupport.definition(
                        "set_default_length_precision_of_er_datatype",
                        "Set the default length/precision (specified by string) of the specified ER datatype (specified by ID), and return the ER datatype information after it is set.",
                        this::setDefaultLengthPrecision,
                        ERDatatypeWithDefaultLengthPrecisionDTO.class,
                        ERDatatypeDTO.class),

                ToolSupport.definition(
                        "set_description_of_er_datatype",
                        "Set the description (specified by string) of the specified ER datatype (specified by ID), and return the ER datatype information after it is set.",
                        this::setDescription,
                        ERDatatypeWithDescriptionDTO.class,
                        ERDatatypeDTO.class)
        );
    }

    private ERDatatypeDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get ER datatype information: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.id());

        return ERDatatypeDTOAssembler.toDTO(astahERDatatype);
    }

    private ERDatatypeDTO setLengthConstraint(McpSyncServerExchange exchange, ERDatatypeWithLengthConstraintDTO param) throws Exception {
        log.debug("Set length constraint of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        try {
            transactionManager.beginTransaction();
            astahERDatatype.setLengthConstraint(param.lengthConstraint());
            transactionManager.endTransaction();

            return ERDatatypeDTOAssembler.toDTO(astahERDatatype);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDatatypeDTO setPrecisionConstraint(McpSyncServerExchange exchange, ERDatatypeWithPrecisionConstraintDTO param) throws Exception {
        log.debug("Set precision constraint of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        try {
            transactionManager.beginTransaction();
            astahERDatatype.setPrecisionConstraint(param.precisionConstraint());
            transactionManager.endTransaction();

            return ERDatatypeDTOAssembler.toDTO(astahERDatatype);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDatatypeDTO setDefaultLengthPrecision(McpSyncServerExchange exchange, ERDatatypeWithDefaultLengthPrecisionDTO param) throws Exception {
        log.debug("Set default length/precision of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        try {
            transactionManager.beginTransaction();
            astahERDatatype.setDefaultLengthPrecision(param.defaultLengthPrecision());
            transactionManager.endTransaction();

            return ERDatatypeDTOAssembler.toDTO(astahERDatatype);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDatatypeDTO setDescription(McpSyncServerExchange exchange, ERDatatypeWithDescriptionDTO param) throws Exception {
        log.debug("Set description of ER datatype: {}", param);

        IERDatatype astahERDatatype = astahProToolSupport.getERDatatype(param.targetERDatatypeId());

        try {
            transactionManager.beginTransaction();
            astahERDatatype.setDefinition(param.description());
            transactionManager.endTransaction();

            return ERDatatypeDTOAssembler.toDTO(astahERDatatype);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
