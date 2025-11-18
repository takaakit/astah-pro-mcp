package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IGate;
import com.change_vision.jude.api.inf.model.IInteraction;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.IMessage;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionDTO;

import java.util.ArrayList;
import java.util.List;

public class InteractionDTOAssembler {
    public static InteractionDTO toDTO(@NonNull IInteraction astahInteraction) throws Exception {

        List<NameIdTypeDTO> lifelines = new ArrayList<>();
        for (ILifeline astahLifeline : astahInteraction.getLifelines()) {
            lifelines.add(NameIdTypeDTOAssembler.toDTO(astahLifeline));
        }

        List<NameIdTypeDTO> messages = new ArrayList<>();
        for (IMessage astahMessage : astahInteraction.getMessages()) {
            messages.add(NameIdTypeDTOAssembler.toDTO(astahMessage));
        }

        List<NameIdTypeDTO> gates = new ArrayList<>();
        for (IGate astahGate : astahInteraction.getGates()) {
            gates.add(NameIdTypeDTOAssembler.toDTO(astahGate));
        }

        return new InteractionDTO(
            NamedElementDTOAssembler.toDTO(astahInteraction),
            lifelines,
            messages,
            gates);
    }
}
