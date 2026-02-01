package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithTypeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EREntityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.EREntityDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IEREntity.html
@Slf4j
public class EREntityTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public EREntityTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER entity tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_er_entity_info",
                        "Return detailed information about the specified ER entity (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        EREntityDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_logical_name_of_er_entity",
                        "Set the logical name (specified by string) of the specified ER entity (specified by ID), and return the ER entity information after it is set.",
                        this::setLogicalName,
                        EREntityWithLogicalNameDTO.class,
                        EREntityDTO.class),

                ToolSupport.definition(
                        "set_physical_name_of_er_entity",
                        "Set the physical name (specified by string) of the specified ER entity (specified by ID), and return the ER entity information after it is set.",
                        this::setPhysicalName,
                        EREntityWithPhysicalNameDTO.class,
                        EREntityDTO.class),

                ToolSupport.definition(
                        "set_type_of_er_entity",
                        "Set the type (specified by string) of the specified ER entity (specified by ID), and return the ER entity information after it is set.",
                        this::setType,
                        EREntityWithTypeDTO.class,
                        EREntityDTO.class)
        );
    }

    private EREntityDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get ER entity information: {}", param);

        IEREntity astahEREntity = astahProToolSupport.getEREntity(param.id());

        return EREntityDTOAssembler.toDTO(astahEREntity);
    }

    private EREntityDTO setLogicalName(McpSyncServerExchange exchange, EREntityWithLogicalNameDTO param) throws Exception {
        log.debug("Set logical name of ER entity: {}", param);

        IEREntity astahEREntity = astahProToolSupport.getEREntity(param.targetEREntityId());

        try {
            transactionManager.beginTransaction();
            astahEREntity.setLogicalName(param.logicalName());
            transactionManager.endTransaction();

            return EREntityDTOAssembler.toDTO(astahEREntity);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private EREntityDTO setPhysicalName(McpSyncServerExchange exchange, EREntityWithPhysicalNameDTO param) throws Exception {
        log.debug("Set physical name of ER entity: {}", param);

        IEREntity astahEREntity = astahProToolSupport.getEREntity(param.targetEREntityId());

        try {
            transactionManager.beginTransaction();
            astahEREntity.setPhysicalName(param.physicalName());
            transactionManager.endTransaction();

            return EREntityDTOAssembler.toDTO(astahEREntity);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private EREntityDTO setType(McpSyncServerExchange exchange, EREntityWithTypeDTO param) throws Exception {
        log.debug("Set type of ER entity: {}", param);

        IEREntity astahEREntity = astahProToolSupport.getEREntity(param.targetEREntityId());

        try {
            transactionManager.beginTransaction();
            astahEREntity.setType(param.type());
            transactionManager.endTransaction();

            return EREntityDTOAssembler.toDTO(astahEREntity);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
