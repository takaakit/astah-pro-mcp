package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationGeometryDTO;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import lombok.NonNull;

public class NodePresentationGeometryDTOAssembler {
    public static NodePresentationGeometryDTO toDTO(@NonNull INodePresentation astahNodePresentation) throws Exception {

        // If the presentation has a null label (such as an image or rectangle), use an empty string.
        String label = astahNodePresentation.getLabel() != null ? astahNodePresentation.getLabel() : "";

        return new NodePresentationGeometryDTO(
            astahNodePresentation.getID(),
            label,
            astahNodePresentation.getType(),
            PresentationDTOAssembler.correspondingModelElementOf(astahNodePresentation),
            RectangleDTOAssembler.toDTO(astahNodePresentation.getRectangle()));
    }
}
