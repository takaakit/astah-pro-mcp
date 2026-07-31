package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ConstraintWithSpecificationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ConstraintDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ConstraintDTOAssembler;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IConstraint.html
@Slf4j
public class ConstraintTool implements ToolProvider {
    
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ConstraintTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create constraint tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_constraint_info",
                "Return model element information about the specified constraint (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ConstraintDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
            "set_specification_of_constraint",
            "Set the specification (specified by string) of the specified constraint (specified by ID), and return the model element of the constraint after it is set.",
            this::setSpecification,
            ConstraintWithSpecificationDTO.class,
            ConstraintDTO.class)
        );
    }

    private ConstraintDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get constraint information: {}", param);

        IConstraint astahConstraint = astahProToolSupport.getConstraint(param.id());

        return ConstraintDTOAssembler.toDTO(astahConstraint);
    }

    private ConstraintDTO setSpecification(ConstraintWithSpecificationDTO param) throws Exception {
        log.debug("Set specification: {}", param);

        IConstraint astahConstraint = astahProToolSupport.getConstraint(param.id());
        
        txnAstah.run( () -> {
            astahConstraint.setSpecification(param.specificationContents());
        });

        return ConstraintDTOAssembler.toDTO(astahConstraint);
    }
}
