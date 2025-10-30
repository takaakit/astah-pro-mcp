package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IGate;
import lombok.NonNull;

public class GateDTOAssembler {
    public static GateDTO toDTO(@NonNull IGate astahGate) throws Exception {

        NameIdTypeDTO interactionUse;
        if (astahGate.getInteractionUse() != null) {
            interactionUse = NameIdTypeDTOAssembler.toDTO(astahGate.getInteractionUse());
        } else {
            interactionUse = NameIdTypeDTO.empty();
        }
        
        NameIdTypeDTO message;
        if (astahGate.getMessage() != null) {
            message = NameIdTypeDTOAssembler.toDTO(astahGate.getMessage());
        } else {
            message = NameIdTypeDTO.empty();
        }

        return new GateDTO(
            NamedElementDTOAssembler.toDTO(astahGate),
            interactionUse,
            message);
    }
}
