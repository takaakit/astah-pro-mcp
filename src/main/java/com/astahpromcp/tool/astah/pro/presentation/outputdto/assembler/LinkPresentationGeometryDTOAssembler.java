package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationGeometryDTO;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import lombok.NonNull;

public class LinkPresentationGeometryDTOAssembler {
    public static LinkPresentationGeometryDTO toDTO(@NonNull ILinkPresentation astahLinkPresentation) throws Exception {

        // For a return message, () is appended to the end, so return the model name.
        String label;
        if (astahLinkPresentation.getModel() instanceof IMessage astahMessage && astahMessage.isReturnMessage()) {
            label = astahMessage.getName() != null ? astahMessage.getName() : "";
        } else {
            label = astahLinkPresentation.getLabel() != null ? astahLinkPresentation.getLabel() : "";
        }

        return new LinkPresentationGeometryDTO(
            astahLinkPresentation.getID(),
            label,
            astahLinkPresentation.getType(),
            PresentationDTOAssembler.correspondingModelElementOf(astahLinkPresentation),
            LinkPresentationDTOAssembler.endOf(LinkPresentationDTOAssembler.sourceEndOf(astahLinkPresentation)),
            LinkPresentationDTOAssembler.endOf(LinkPresentationDTOAssembler.targetEndOf(astahLinkPresentation)),
            LinkPresentationDTOAssembler.drawnPointsOf(astahLinkPresentation),
            LineStyleKind.getCorrespondingType(astahLinkPresentation.getProperty(Key.LINE_SHAPE)));
    }
}
