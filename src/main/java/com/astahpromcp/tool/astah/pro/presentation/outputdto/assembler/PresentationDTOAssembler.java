package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IHyperlink;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import lombok.NonNull;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.change_vision.jude.api.inf.model.INamedElement;

public class PresentationDTOAssembler {
    public static PresentationDTO toDTO(@NonNull IPresentation astahPresentation) throws Exception {

        NameIdTypeDTO renderedInDiagram;
        if (astahPresentation.getDiagram() != null && astahPresentation.getDiagram() instanceof INamedElement) {
            renderedInDiagram = NameIdTypeDTOAssembler.toDTO((INamedElement)astahPresentation.getDiagram());
        } else {
            renderedInDiagram = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO correspondingModelElement;
        if (astahPresentation.getModel() != null && astahPresentation.getModel() instanceof INamedElement) {
            correspondingModelElement = NameIdTypeDTOAssembler.toDTO((INamedElement)astahPresentation.getModel());
        } else {
            correspondingModelElement = NameIdTypeDTO.empty();
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

        // If the presentation has a null label (such as an image, rectangle, or line), use an empty string.
        String label = astahPresentation.getLabel() != null ? astahPresentation.getLabel() : "";

        return  new PresentationDTO(
            astahPresentation.getID(),
            label,
            renderedInDiagram,
            correspondingModelElement,
            astahPresentation.getType(),
            fillColor,
            lineColor,
            fontColor);
    }
}
