package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ISlot;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
