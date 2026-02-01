package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ConstraintWithSpecificationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ConstraintDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ConstraintDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IConstraint.html
@Slf4j
public class ConstraintTool implements ToolProvider {
    
    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ConstraintTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create constraint tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_constraint_info",
                        "Return detailed information about the specified constraint (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        ConstraintDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.definition(
                "set_specification_of_constraint",
                "Set the specification (specified by string) of the specified constraint (specified by ID), and return the constraint information after it is set.",
                this::setSpecification,
                ConstraintWithSpecificationDTO.class,
                ConstraintDTO.class)
        );
    }

    private ConstraintDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get constraint information: {}", param);

        IConstraint astahConstraint = astahProToolSupport.getConstraint(param.id());

        return ConstraintDTOAssembler.toDTO(astahConstraint);
    }

    private ConstraintDTO setSpecification(McpSyncServerExchange exchange, ConstraintWithSpecificationDTO param) throws Exception {
        log.debug("Set specification: {}", param);

        IConstraint astahConstraint = astahProToolSupport.getConstraint(param.id());
        
        try {
            transactionManager.beginTransaction();
            astahConstraint.setSpecification(param.specificationContents());
            transactionManager.endTransaction();

            return ConstraintDTOAssembler.toDTO(astahConstraint);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
