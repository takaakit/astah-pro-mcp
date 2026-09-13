package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.RequirementWithIdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.RequirementWithTextDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.RequirementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.RequirementDTOAssembler;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IRequirement.html
@Slf4j
public class RequirementTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public RequirementTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_req_info",
                "Return model element information about the specified requirement (specified by ID).",
                this::getInfo,
                IdDTO.class,
                RequirementDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_req_id",
                "Set the requirement identifier (specified by string) of the specified requirement (specified by ID), and return the model element of the requirement after it is set.",
                this::setRequirementId,
                RequirementWithIdDTO.class,
                RequirementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_req_text",
                "Set the requirement text (specified by string) of the specified requirement (specified by ID), and return the model element of the requirement after it is set.",
                this::setRequirementText,
                RequirementWithTextDTO.class,
                RequirementDTO.class)
        );
    }

    private RequirementDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get requirement information: {}", param);

        IRequirement astahRequirement = astahProToolSupport.getRequirement(param.id());

        return RequirementDTOAssembler.toDTO(astahRequirement);
    }

    private RequirementDTO setRequirementId(RequirementWithIdDTO param) throws Exception {
        log.debug("Set requirement identifier: {}", param);

        IRequirement astahRequirement = astahProToolSupport.getRequirement(param.id());

        txnAstah.run( () -> {
            astahRequirement.setRequirementID(param.requirementId());
        });

        return RequirementDTOAssembler.toDTO(astahRequirement);
    }

    private RequirementDTO setRequirementText(RequirementWithTextDTO param) throws Exception {
        log.debug("Set requirement text: {}", param);

        IRequirement astahRequirement = astahProToolSupport.getRequirement(param.id());

        txnAstah.run( () -> {
            astahRequirement.setRequirementText(param.requirementText());
        });

        return RequirementDTOAssembler.toDTO(astahRequirement);
    }
}
