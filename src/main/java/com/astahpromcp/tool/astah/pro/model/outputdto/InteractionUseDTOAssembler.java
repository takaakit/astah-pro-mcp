package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IGate;
import com.change_vision.jude.api.inf.model.IInteractionUse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InteractionUseDTOAssembler {
    public static InteractionUseDTO toDTO(@NonNull IInteractionUse astahInteractionUse) throws Exception {

        NameIdTypeDTO sequenceDiagramDTO;
        if (astahInteractionUse.getSequenceDiagram() != null) {
            sequenceDiagramDTO = NameIdTypeDTOAssembler.toDTO(astahInteractionUse.getSequenceDiagram());
        } else {
            sequenceDiagramDTO = NameIdTypeDTO.empty();
        }

        List<NameIdTypeDTO> gateDTOs = new ArrayList<>();
        for (IGate astahGate : astahInteractionUse.getGates()) {
            gateDTOs.add(NameIdTypeDTOAssembler.toDTO(astahGate));
        }

        return new InteractionUseDTO(
            NamedElementDTOAssembler.toDTO(astahInteractionUse),
            astahInteractionUse.getArgument(),
            sequenceDiagramDTO,
            gateDTOs);
    }
}
