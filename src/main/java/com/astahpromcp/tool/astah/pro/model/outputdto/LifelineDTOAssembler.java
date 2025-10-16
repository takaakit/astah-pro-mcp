package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.ILifelineLink;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LifelineDTOAssembler {
    public static LifelineDTO toDTO(@NonNull ILifeline astahLifeline) throws Exception {

        NameIdTypeDTO baseClass;
        if (astahLifeline.getBase() != null) {
            baseClass = NameIdTypeDTOAssembler.toDTO(astahLifeline.getBase());
        } else {
            baseClass = NameIdTypeDTO.empty();
        }

        List<NameIdTypeDTO> fragments = new ArrayList<>();
        for (INamedElement astahFragment : astahLifeline.getFragments()) {
            fragments.add(NameIdTypeDTOAssembler.toDTO(astahFragment));
        }

        List<NameIdTypeDTO> lifelineLinks = new ArrayList<>();
        for (ILifelineLink astahLifelineLink : astahLifeline.getLifelineLinks()) {
            lifelineLinks.add(NameIdTypeDTOAssembler.toDTO(astahLifelineLink));
        }

        return new LifelineDTO(
            NamedElementDTOAssembler.toDTO(astahLifeline),
            baseClass,
            fragments,
            lifelineLinks);
    }
}
