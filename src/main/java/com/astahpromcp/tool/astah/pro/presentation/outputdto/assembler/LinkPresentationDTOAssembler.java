package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.LabelIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IExtend;
import com.change_vision.jude.api.inf.model.IInclude;
import com.change_vision.jude.api.inf.model.IRealization;
import com.change_vision.jude.api.inf.model.IUsage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import lombok.NonNull;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LinkPresentationDTOAssembler {
    public static LinkPresentationDTO toDTO(@NonNull ILinkPresentation astahLinkPresentation) throws Exception {

        return new LinkPresentationDTO(
            PresentationDTOAssembler.toDTO(astahLinkPresentation),
            endOf(sourceEndOf(astahLinkPresentation)),
            endOf(targetEndOf(astahLinkPresentation)),
            drawnPointsOf(astahLinkPresentation),
            LineStyleKind.getCorrespondingType(astahLinkPresentation.getProperty(Key.LINE_SHAPE)));
    }

    // Astah reports the supplier side of these links as the source end, the reverse of the model and of the arrow, so the ends are swapped back.
    private static boolean hasReversedEnds(ILinkPresentation astahLinkPresentation) {
        IElement astahModel = astahLinkPresentation.getModel();
        return astahModel instanceof IDependency || astahModel instanceof IUsage || astahModel instanceof IRealization
            || astahModel instanceof IInclude || astahModel instanceof IExtend;
    }

    public static IPresentation sourceEndOf(ILinkPresentation astahLinkPresentation) {
        return hasReversedEnds(astahLinkPresentation) ? astahLinkPresentation.getTargetEnd() : astahLinkPresentation.getSourceEnd();
    }

    public static IPresentation targetEndOf(ILinkPresentation astahLinkPresentation) {
        return hasReversedEnds(astahLinkPresentation) ? astahLinkPresentation.getSourceEnd() : astahLinkPresentation.getTargetEnd();
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
