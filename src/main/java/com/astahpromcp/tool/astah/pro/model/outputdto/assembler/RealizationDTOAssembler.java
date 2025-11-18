package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IRealization;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.RealizationDTO;

public class RealizationDTOAssembler {
    public static RealizationDTO toDTO(@NonNull IRealization astahRealization) throws Exception {

        return new RealizationDTO(
            NamedElementDTOAssembler.toDTO(astahRealization),
            NameIdTypeDTOAssembler.toDTO(astahRealization.getClient()),
            NameIdTypeDTOAssembler.toDTO(astahRealization.getSupplier()));
    }
}
