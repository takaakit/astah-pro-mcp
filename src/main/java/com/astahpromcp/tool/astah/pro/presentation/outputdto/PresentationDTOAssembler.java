package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import lombok.NonNull;

public class PresentationDTOAssembler {
    public static PresentationDTO toDTO(@NonNull IPresentation astahPresentation) throws Exception {

        String correspondingElementId;
        if (astahPresentation.getModel() != null) {
            correspondingElementId = astahPresentation.getModel().getId();
        } else {
            correspondingElementId = "";
        }

        String fillColor;
        if (astahPresentation.getProperty(Key.FILL_COLOR) != null) {
            fillColor = astahPresentation.getProperty(Key.FILL_COLOR);
        } else {
            fillColor = "";
        }

        String lineColor;
        if (astahPresentation.getProperty(Key.LINE_COLOR) != null) {
            lineColor = astahPresentation.getProperty(Key.LINE_COLOR);
        } else {
            lineColor = "";
        }

        String fontColor;
        if (astahPresentation.getProperty(Key.FONT_COLOR) != null) {
            fontColor = astahPresentation.getProperty(Key.FONT_COLOR);
        } else {
            fontColor = "";
        }

        return  new PresentationDTO(
            astahPresentation.getID(),
            astahPresentation.getLabel(),
            astahPresentation.getDiagram().getId(),
            correspondingElementId,
            astahPresentation.getType(),
            fillColor,
            lineColor,
            fontColor);
    }
}
