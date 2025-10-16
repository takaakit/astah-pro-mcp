package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.change_vision.jude.api.inf.presentation.IPresentation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LabelIdTypeDTOAssembler {
    public static LabelIdTypeDTO toDTO(@NonNull IPresentation presentation) throws Exception {

        return new LabelIdTypeDTO(
            presentation.getLabel(),
            presentation.getID(),
            presentation.getType());
    }
}
