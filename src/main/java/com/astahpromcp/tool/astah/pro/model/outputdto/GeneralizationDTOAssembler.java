package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IGeneralization;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneralizationDTOAssembler {
    public static GeneralizationDTO toDTO(@NonNull IGeneralization astahGeneralization) throws Exception {

        return new GeneralizationDTO(
            NamedElementDTOAssembler.toDTO(astahGeneralization),
            NameIdTypeDTOAssembler.toDTO(astahGeneralization.getSuperType()),
            NameIdTypeDTOAssembler.toDTO(astahGeneralization.getSubType()));
    }
}
