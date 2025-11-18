package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IGeneralization;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.GeneralizationDTO;

public class GeneralizationDTOAssembler {
    public static GeneralizationDTO toDTO(@NonNull IGeneralization astahGeneralization) throws Exception {

        return new GeneralizationDTO(
            NamedElementDTOAssembler.toDTO(astahGeneralization),
            NameIdTypeDTOAssembler.toDTO(astahGeneralization.getSuperType()),
            NameIdTypeDTOAssembler.toDTO(astahGeneralization.getSubType()));
    }
}
