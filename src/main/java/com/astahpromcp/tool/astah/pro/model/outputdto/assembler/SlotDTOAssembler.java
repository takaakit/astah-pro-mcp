package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ISlot;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.SlotDTO;

public class SlotDTOAssembler {
    public static SlotDTO toDTO(@NonNull ISlot astahSlot) throws Exception {

        NameIdTypeDTO definingAttribute;
        if (astahSlot.getDefiningAttribute() != null) {
            definingAttribute = NameIdTypeDTOAssembler.toDTO(astahSlot.getDefiningAttribute());
        } else {
            definingAttribute = NameIdTypeDTO.empty();
        }

        return new SlotDTO(
            NamedElementDTOAssembler.toDTO(astahSlot),
            definingAttribute,
            astahSlot.getValue());
    }
}
