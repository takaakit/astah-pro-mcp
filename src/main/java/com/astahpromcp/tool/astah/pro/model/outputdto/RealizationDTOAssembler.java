package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IRealization;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RealizationDTOAssembler {
    public static RealizationDTO toDTO(@NonNull IRealization astahRealization) throws Exception {

        return new RealizationDTO(
            NamedElementDTOAssembler.toDTO(astahRealization),
            NameIdTypeDTOAssembler.toDTO(astahRealization.getClient()),
            NameIdTypeDTOAssembler.toDTO(astahRealization.getSupplier()));
    }
}
