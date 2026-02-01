package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithERAttributeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithKeyDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithUniqueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERIndexDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERIndexDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERIndex.html
@Slf4j
public class ERIndexTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERIndexTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER index tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_er_index_info",
                        "Return detailed information about the specified ER index (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        ERIndexDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "add_er_attr_to_er_index",
                        "Add the ER attribute (specified by ID) to the specified ER index (specified by ID), and return the ER index information after it is set.",
                        this::addERAttribute,
                        ERIndexWithERAttributeDTO.class,
                        ERIndexDTO.class),

                ToolSupport.definition(
                        "remove_er_attr_from_er_index",
                        "Remove the ER attribute (specified by ID) from the specified ER index (specified by ID), and return the ER index information after it is set.",
                        this::removeERAttribute,
                        ERIndexWithERAttributeDTO.class,
                        ERIndexDTO.class),

                ToolSupport.definition(
                        "set_key_of_er_index",
                        "Set the key of the specified ER index (specified by ID), and return the ER index information after it is set.",
                        this::setKey,
                        ERIndexWithKeyDTO.class,
                        ERIndexDTO.class),

                ToolSupport.definition(
                        "set_unique_of_er_index",
                        "Set the unique of the specified ER index (specified by ID), and return the ER index information after it is set.",
                        this::setUnique,
                        ERIndexWithUniqueDTO.class,
                        ERIndexDTO.class)
        );
    }

    private ERIndexDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get ER index information: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.id());

        return ERIndexDTOAssembler.toDTO(astahERIndex);
    }

    private ERIndexDTO addERAttribute(McpSyncServerExchange exchange, ERIndexWithERAttributeDTO param) throws Exception {
        log.debug("Add ER attribute to ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());
        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.erAttributeId());

        try {
            transactionManager.beginTransaction();
            astahERIndex.addERAttribute(astahERAttribute);
            transactionManager.endTransaction();

            return ERIndexDTOAssembler.toDTO(astahERIndex);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERIndexDTO removeERAttribute(McpSyncServerExchange exchange, ERIndexWithERAttributeDTO param) throws Exception {
        log.debug("Remove ER attribute from ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());
        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.erAttributeId());

        try {
            transactionManager.beginTransaction();
            astahERIndex.removeERAttribute(astahERAttribute);
            transactionManager.endTransaction();

            return ERIndexDTOAssembler.toDTO(astahERIndex);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERIndexDTO setKey(McpSyncServerExchange exchange, ERIndexWithKeyDTO param) throws Exception {
        log.debug("Set key of ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());

        try {
            transactionManager.beginTransaction();
            astahERIndex.setKey(param.isKey());
            transactionManager.endTransaction();

            return ERIndexDTOAssembler.toDTO(astahERIndex);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERIndexDTO setUnique(McpSyncServerExchange exchange, ERIndexWithUniqueDTO param) throws Exception {
        log.debug("Set unique of ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());

        try {
            transactionManager.beginTransaction();
            astahERIndex.setUnique(param.isUnique());
            transactionManager.endTransaction();

            return ERIndexDTOAssembler.toDTO(astahERIndex);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
