package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LinkPresentationDTOAssembler {
    public static LinkPresentationDTO toDTO(@NonNull ILinkPresentation astahLinkPresentation) throws Exception {

        String sourceNodeEndId;
        if (astahLinkPresentation.getSourceEnd() != null) {
            sourceNodeEndId = astahLinkPresentation.getSourceEnd().getID();
        } else {
            sourceNodeEndId = "";
        }

        String targetNodeEndId;
        if (astahLinkPresentation.getTargetEnd() != null) {
            targetNodeEndId = astahLinkPresentation.getTargetEnd().getID();
        } else {
            targetNodeEndId = "";
        }

        List<PointDoubleDTO> drawPoints = new ArrayList<>();
        for (Point2D point : astahLinkPresentation.getPoints()) {
            drawPoints.add(new PointDoubleDTO(point.getX(), point.getY()));
        }

        return new LinkPresentationDTO(
            PresentationDTOAssembler.toDTO(astahLinkPresentation),
            sourceNodeEndId,
            targetNodeEndId,
            drawPoints);
    }
}
