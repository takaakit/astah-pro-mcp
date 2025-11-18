package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.RequirementWithIdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.RequirementWithTextDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.RequirementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.RequirementDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IRequirement.html
@Slf4j
public class RequirementTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public RequirementTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create requirement tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_req_info",
                        "Return detailed information about the specified requirement (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        RequirementDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_req_id",
                        "Set the requirement identifier (specified by string) of the specified requirement (specified by ID), and return the requirement information after it is set.",
                        this::setRequirementId,
                        RequirementWithIdDTO.class,
                        RequirementDTO.class),

                ToolSupport.definition(
                        "set_req_text",
                        "Set the requirement text (specified by string) of the specified requirement (specified by ID), and return the requirement information after it is set.",
                        this::setRequirementText,
                        RequirementWithTextDTO.class,
                        RequirementDTO.class)
        );
    }

    private RequirementDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get requirement information: {}", param);

        IRequirement astahRequirement = astahProToolSupport.getRequirement(param.id());

        return RequirementDTOAssembler.toDTO(astahRequirement);
    }

    private RequirementDTO setRequirementId(McpSyncServerExchange exchange, RequirementWithIdDTO param) throws Exception {
        log.debug("Set requirement identifier: {}", param);

        IRequirement astahRequirement = astahProToolSupport.getRequirement(param.id());

        try {
            transactionManager.beginTransaction();
            astahRequirement.setRequirementID(param.requirementId());
            transactionManager.endTransaction();

            return RequirementDTOAssembler.toDTO(astahRequirement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private RequirementDTO setRequirementText(McpSyncServerExchange exchange, RequirementWithTextDTO param) throws Exception {
        log.debug("Set requirement text: {}", param);

        IRequirement astahRequirement = astahProToolSupport.getRequirement(param.id());

        try {
            transactionManager.beginTransaction();
            astahRequirement.setRequirementText(param.requirementText());
            transactionManager.endTransaction();

            return RequirementDTOAssembler.toDTO(astahRequirement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
