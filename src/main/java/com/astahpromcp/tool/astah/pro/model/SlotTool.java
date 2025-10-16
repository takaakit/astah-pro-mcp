package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.SlotWithValueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.SlotDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.SlotDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ISlot;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ISlot.html
@Slf4j
public class SlotTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public SlotTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.definition(
                "get_slot_info",
                "Return detailed information about the specified slot (specified by ID).",
                this::getInfo,
                IdDTO.class,
                SlotDTO.class),

            ToolSupport.definition(
                "set_val_of_slot",
                "Set the value of the specified slot (specified by ID), and return the slot information after it is set.",
                this::setValue,
                SlotWithValueDTO.class,
                SlotDTO.class)
        );
    }

    private SlotDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get slot information: {}", param);

        ISlot astahSlot = astahProToolSupport.getSlot(param.id());

        return SlotDTOAssembler.toDTO(astahSlot);
    }

    private SlotDTO setValue(McpSyncServerExchange exchange, SlotWithValueDTO param) throws Exception {
        log.debug("Set value of slot: {}", param);

        ISlot astahSlot = astahProToolSupport.getSlot(param.targetSlotId());

        try {
            transactionManager.beginTransaction();
            astahSlot.setValue(param.value());
            transactionManager.endTransaction();

            return SlotDTOAssembler.toDTO(astahSlot);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
