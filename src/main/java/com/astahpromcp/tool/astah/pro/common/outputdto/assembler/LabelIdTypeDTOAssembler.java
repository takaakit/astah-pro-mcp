package com.astahpromcp.tool.astah.pro.common.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;

public class LabelIdTypeDTOAssembler {
    public static LabelIdTypeDTO toDTO(@NonNull IPresentation astahPresentation) throws Exception {
        // For a return message, () is appended to the end, so return the model name.
        String label;
        if (astahPresentation.getModel() instanceof IMessage astahMessage && astahMessage.isReturnMessage()) {
            label = astahMessage.getName() != null ? astahMessage.getName() : "";
        } else {
            label = astahPresentation.getLabel() != null ? astahPresentation.getLabel() : "";
        }

        return new LabelIdTypeDTO(
            label,
            astahPresentation.getID() != null ? astahPresentation.getID() : "",
            astahPresentation.getType());
    }
}
