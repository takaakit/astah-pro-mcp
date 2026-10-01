package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationGeometryDTO;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import lombok.NonNull;

public class LinkPresentationGeometryDTOAssembler {
    public static LinkPresentationGeometryDTO toDTO(@NonNull ILinkPresentation astahLinkPresentation) throws Exception {

        // If the presentation has a null label, use an empty string.
        String label = astahLinkPresentation.getLabel() != null ? astahLinkPresentation.getLabel() : "";

        return new LinkPresentationGeometryDTO(
            astahLinkPresentation.getID(),
            label,
            astahLinkPresentation.getType(),
            PresentationDTOAssembler.correspondingModelElementOf(astahLinkPresentation),
            LinkPresentationDTOAssembler.endOf(astahLinkPresentation.getSourceEnd()),
            LinkPresentationDTOAssembler.endOf(astahLinkPresentation.getTargetEnd()),
            LinkPresentationDTOAssembler.drawnPointsOf(astahLinkPresentation),
            LineStyleKind.getCorrespondingType(astahLinkPresentation.getProperty(Key.LINE_SHAPE)));
    }
}
