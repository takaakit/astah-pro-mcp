package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
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
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/ILinkPresentation.html
@Slf4j
public class LinkPresentationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public LinkPresentationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.imageCaptureSupport = imageCaptureSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create link presentation tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_link_prst_info",
                "Return information about the specified link presentation (specified by ID).",
                this::getInfo,
                IdDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_points_of_link_prst",
                "Set all points with the connection points in the rectangles of the specified link presentation (specified by ID), and return the link presentation after it is set along with the updated diagram image in low resolution. Note that it must include the connection points with the rectangle (node presentation). The connection points must be inside the node presentation rectangles, not on their borders.",
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
                throw new RuntimeException(String.format(
                    "%s The points must be ordered from the source end (%s) to the target end (%s) of the link, and the first and last points must be inside the rectangles of those ends, not on their borders.",
                    e.getMessage(),
                    linkPresentation.getSourceEnd().getLabel(),
                    linkPresentation.getTargetEnd().getLabel()), e);
            }
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(linkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(linkPresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
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
