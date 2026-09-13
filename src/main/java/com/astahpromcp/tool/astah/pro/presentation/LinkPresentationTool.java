package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.PointIntDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.LinkPresentationWithLineStyleDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.LinkPresentationWithPointsDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/ILinkPresentation.html
@Slf4j
public class LinkPresentationTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;

    public LinkPresentationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.imageCaptureSupport = imageCaptureSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_link_prst_info",
                "Return information about the specified link presentation (specified by ID). Note that the source end and the target end are the ends of the link as the model defines them, and the order of the drawn points does not necessarily follow them.",
                this::getInfo,
                IdDTO.class,
                LinkPresentationDTO.class),


            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_points_of_link_prst",
                "Set all points with the connection points in the rectangles of the specified link presentation (specified by ID), and return the link presentation after it is set along with the updated diagram image in low resolution. Note that it must include the connection points with the rectangle (node presentation). The connection points must be inside the node presentation rectangles, not on their borders. The points may be given in either direction; the returned points may therefore be in the reverse order of the given ones. Note that a node presentation is resized to fit its label when the transaction that created it is committed, so connection points derived from its rectangle while that transaction is still open may fall outside the final rectangle.",
                this::setAllPoints,
                LinkPresentationWithPointsDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_line_style_of_link_prst",
                "Set the line style of the specified link presentation (specified by ID), and return the link presentation after it is set along with the updated diagram image in low resolution.",
                this::setLineStyle,
                LinkPresentationWithLineStyleDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private LinkPresentationDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get link presentation information: {}", param);

        ILinkPresentation linkPresentation = astahProToolSupport.getLinkPresentation(param.id());

        return LinkPresentationDTOAssembler.toDTO(linkPresentation);
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> setAllPoints(LinkPresentationWithPointsDTO param) throws Exception {
        log.debug("Set points of link presentation: {}", param);

        ILinkPresentation linkPresentation = astahProToolSupport.getLinkPresentation(param.targetLinkPresentationId());

		Point2D.Double[] pointArray = new Point2D.Double[param.drawPoints().size()];
		for (int i = 0; i < pointArray.length; i++) {
			PointIntDTO point = param.drawPoints().get(i);
			pointArray[i] = new Point2D.Double(point.x(), point.y());
		}

        txnAstah.run( () -> {
            try {
                linkPresentation.setAllPoints(pointArray);
                
            } catch (Exception e) {
                // Astah requires the points in the order it holds them internally, and for some link types (a generalization, for one) that order is the reverse of the source end to target end order reported for the link.
                // Reversing a polyline draws the very same line, so the reversed array is retried rather than making the caller work out which order to send.
                Point2D.Double[] reversedPointArray = new Point2D.Double[pointArray.length];
                for (int i = 0; i < pointArray.length; i++) {
                    reversedPointArray[i] = pointArray[pointArray.length - 1 - i];
                }

                try {
                    linkPresentation.setAllPoints(reversedPointArray);
                    
                } catch (Exception reversedException) {
                    // Both orders were tried, so the order is not what is wrong here.
                    throw new RuntimeException(String.format(
                        "%s The first and last points must be inside the rectangles of the two ends (%s and %s) of the link, not on their borders.",
                        e.getMessage(),
                        endLabelOf(linkPresentation.getSourceEnd()),
                        endLabelOf(linkPresentation.getTargetEnd())), e);
                }
            }
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(linkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(linkPresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }

    private static String endLabelOf(IPresentation astahEnd) {
        return astahEnd == null ? "unknown" : astahEnd.getLabel();
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> setLineStyle(LinkPresentationWithLineStyleDTO param) throws Exception {
        log.debug("Set line style of link presentation: {}", param);

        ILinkPresentation linkPresentation = astahProToolSupport.getLinkPresentation(param.targetLinkPresentationId());

        txnAstah.run( () -> {
            linkPresentation.setProperty(Key.LINE_SHAPE, param.lineStyle().astahValue);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(linkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(linkPresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }
}
