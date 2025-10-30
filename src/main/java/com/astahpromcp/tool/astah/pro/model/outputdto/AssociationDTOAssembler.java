package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import lombok.NonNull;

public class AssociationDTOAssembler {
    public static AssociationDTO toDTO(@NonNull IAssociation astahAssociation) throws Exception {

        IAttribute[] memberEnds = astahAssociation.getMemberEnds();
        IAttribute associationEndA = memberEnds[0];
        IAttribute associationEndB = memberEnds[1];

        return new AssociationDTO(
            NamedElementDTOAssembler.toDTO(astahAssociation),
            NameIdTypeDTOAssembler.toDTO(associationEndA),
            NameIdTypeDTOAssembler.toDTO(associationEndB));
    }
}
