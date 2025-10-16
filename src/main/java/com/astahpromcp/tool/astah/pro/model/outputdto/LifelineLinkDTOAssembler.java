package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ILifelineLink;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LifelineLinkDTOAssembler {
    public static LifelineLinkDTO toDTO(@NonNull ILifelineLink astahLifelineLink) throws Exception {

        List<NameIdTypeDTO> messages = new ArrayList<>();
        for (INamedElement astahMessage : astahLifelineLink.getMessages()) {
            messages.add(NameIdTypeDTOAssembler.toDTO(astahMessage));
        }

        NameIdTypeDTO sourceLifelineDTO;
        if (astahLifelineLink.getSource() != null) {
            sourceLifelineDTO = NameIdTypeDTOAssembler.toDTO(astahLifelineLink.getSource());
        } else {
            sourceLifelineDTO = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO targetLifelineDTO;
        if (astahLifelineLink.getTarget() != null) {
            targetLifelineDTO = NameIdTypeDTOAssembler.toDTO(astahLifelineLink.getTarget());
        } else {
            targetLifelineDTO = NameIdTypeDTO.empty();
        }

        return new LifelineLinkDTO(
            NamedElementDTOAssembler.toDTO(astahLifelineLink),
            messages,
            sourceLifelineDTO,
            targetLifelineDTO);
    }
}
