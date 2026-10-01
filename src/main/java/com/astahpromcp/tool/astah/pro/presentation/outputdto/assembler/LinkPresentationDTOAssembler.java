package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.LabelIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LinkPresentationDTOAssembler {
    public static LinkPresentationDTO toDTO(@NonNull ILinkPresentation astahLinkPresentation) throws Exception {

        return new LinkPresentationDTO(
            PresentationDTOAssembler.toDTO(astahLinkPresentation),
            endOf(astahLinkPresentation.getSourceEnd()),
            endOf(astahLinkPresentation.getTargetEnd()),
            drawnPointsOf(astahLinkPresentation),
            LineStyleKind.getCorrespondingType(astahLinkPresentation.getProperty(Key.LINE_SHAPE)));
    }

    public static LabelIdTypeDTO endOf(IPresentation astahEnd) throws Exception {
        if (astahEnd != null) {
            return LabelIdTypeDTOAssembler.toDTO(astahEnd);
        } else {
            return LabelIdTypeDTO.empty();
        }
    }

    public static List<PointDoubleDTO> drawnPointsOf(ILinkPresentation astahLinkPresentation) throws Exception {
        List<PointDoubleDTO> drawnPoints = new ArrayList<>();

        // getPoints() may return null
        Point2D[] astahPoints = astahLinkPresentation.getPoints();
        if (astahPoints == null) {
            return drawnPoints;
        }

        for (Point2D point : astahPoints) {
            drawnPoints.add(new PointDoubleDTO(point.getX(), point.getY()));
        }
        return drawnPoints;
    }
}
