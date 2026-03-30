package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.LabelIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LinkPresentationDTOAssembler {
    public static LinkPresentationDTO toDTO(@NonNull ILinkPresentation astahLinkPresentation) throws Exception {

        LabelIdTypeDTO sourceNodeEnd;
        if (astahLinkPresentation.getSourceEnd() != null) {
            sourceNodeEnd = LabelIdTypeDTOAssembler.toDTO(astahLinkPresentation.getSourceEnd());
        } else {
            sourceNodeEnd = LabelIdTypeDTO.empty();
        }

        LabelIdTypeDTO targetNodeEnd;
        if (astahLinkPresentation.getTargetEnd() != null) {
            targetNodeEnd = LabelIdTypeDTOAssembler.toDTO(astahLinkPresentation.getTargetEnd());
        } else {
            targetNodeEnd = LabelIdTypeDTO.empty();
        }

        List<PointDoubleDTO> drawPoints = new ArrayList<>();
        for (Point2D point : astahLinkPresentation.getPoints()) {
            drawPoints.add(new PointDoubleDTO(point.getX(), point.getY()));
        }

        return new LinkPresentationDTO(
            PresentationDTOAssembler.toDTO(astahLinkPresentation),
            sourceNodeEnd,
            targetNodeEnd,
            drawPoints,
            LineStyleKind.getCorrespondingType(astahLinkPresentation.getProperty(Key.LINE_SHAPE)));
    }
}
