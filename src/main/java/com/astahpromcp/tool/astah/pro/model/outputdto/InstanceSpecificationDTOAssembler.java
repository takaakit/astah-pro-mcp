package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.model.ISlot;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InstanceSpecificationDTOAssembler {
    public static InstanceSpecificationDTO toDTO(@NonNull IInstanceSpecification astahInstanceSpecification) throws Exception {

        NameIdTypeDTO classifier;
        if (astahInstanceSpecification.getClassifier() != null) {
            classifier = NameIdTypeDTOAssembler.toDTO(astahInstanceSpecification.getClassifier());
        } else {
            classifier = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO parentInstanceSpecification;
        if (astahInstanceSpecification.getContainer() != null) {
            parentInstanceSpecification = NameIdTypeDTOAssembler.toDTO(astahInstanceSpecification.getContainer());
        } else {
            parentInstanceSpecification = NameIdTypeDTO.empty();
        }

        List<NameIdTypeDTO> linkEnds = new ArrayList<>();
        for (ILinkEnd linkEnd : astahInstanceSpecification.getLinkEnds()) {
            linkEnds.add(NameIdTypeDTOAssembler.toDTO(linkEnd));
        }

        List<NameIdTypeDTO> slotDTOs = new ArrayList<>();
        for (ISlot slot : astahInstanceSpecification.getAllSlots()) {
            slotDTOs.add(NameIdTypeDTOAssembler.toDTO(slot));
        }

        return new InstanceSpecificationDTO(
            NamedElementDTOAssembler.toDTO(astahInstanceSpecification),
            classifier,
            parentInstanceSpecification,
            linkEnds,
            slotDTOs);
    }
}
