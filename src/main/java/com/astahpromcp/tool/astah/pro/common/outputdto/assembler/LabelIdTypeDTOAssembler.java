package com.astahpromcp.tool.astah.pro.common.outputdto.assembler;

import com.change_vision.jude.api.inf.presentation.IPresentation;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;

public class LabelIdTypeDTOAssembler {
    public static LabelIdTypeDTO toDTO(@NonNull IPresentation astahPresentation) throws Exception {
        return new LabelIdTypeDTO(
            astahPresentation.getLabel(),
            astahPresentation.getID(),
            astahPresentation.getType());
    }
}
