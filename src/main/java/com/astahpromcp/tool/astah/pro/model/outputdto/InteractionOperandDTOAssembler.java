package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.IMessage;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class InteractionOperandDTOAssembler {
    public static InteractionOperandDTO toDTO(@NonNull IInteractionOperand astahInteractionOperand) throws Exception {

        List<NameIdTypeDTO> lifelineDTOs = new ArrayList<>();
        for (ILifeline astahLifeline : astahInteractionOperand.getLifelines()) {
            lifelineDTOs.add(NameIdTypeDTOAssembler.toDTO(astahLifeline));
        }

        List<NameIdTypeDTO> messageDTOs = new ArrayList<>();
        for (IMessage astahMessage : astahInteractionOperand.getMessages()) {
            messageDTOs.add(NameIdTypeDTOAssembler.toDTO(astahMessage));
        }

        return new InteractionOperandDTO(
            NamedElementDTOAssembler.toDTO(astahInteractionOperand),
            astahInteractionOperand.getGuard(),
            lifelineDTOs,
            messageDTOs);
    }
}
