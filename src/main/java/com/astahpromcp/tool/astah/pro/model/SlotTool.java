package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.SlotWithValueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.SlotDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.SlotDTOAssembler;
import com.change_vision.jude.api.inf.model.ISlot;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ISlot.html
@Slf4j
public class SlotTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public SlotTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_slot_info",
                "Return model element information about the specified slot (specified by ID).",
                this::getInfo,
                IdDTO.class,
                SlotDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_val_of_slot",
                "Set the value of the specified slot (specified by ID), and return the model element of the slot after it is set.",
                this::setValue,
                SlotWithValueDTO.class,
                SlotDTO.class)
        );
    }

    private SlotDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get slot information: {}", param);

        ISlot astahSlot = astahProToolSupport.getSlot(param.id());

        return SlotDTOAssembler.toDTO(astahSlot);
    }

    private SlotDTO setValue(SlotWithValueDTO param) throws Exception {
        log.debug("Set value of slot: {}", param);

        ISlot astahSlot = astahProToolSupport.getSlot(param.targetSlotId());

        txnAstah.run( () -> {
            astahSlot.setValue(param.value());
        });

        return SlotDTOAssembler.toDTO(astahSlot);
    }
}
