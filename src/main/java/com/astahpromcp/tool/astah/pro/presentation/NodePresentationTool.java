package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithHeightDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithLocationDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithWidthDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/INodePresentation.html
@Slf4j
public class NodePresentationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public NodePresentationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
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
            log.error("Failed to create node presentation tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_node_info",
                "Return information about the specified node presentation (specified by ID).",
                this::getInfo,
                IdDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_node_prst_rectangle",
                "Return the rectangle of the specified node presentation (specified by ID).",
                this::getNodePresentationRectangle,
                IdDTO.class,
                RectangleDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_node_prst_location",
                "Set the location (specified by x and y coordinates) of the specified node presentation (specified by ID), and return its rectangle after setting along with the updated diagram image in low resolution.",
                this::setNodePresentationLocation,
                NodePresentationWithLocationDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_node_prst_width",
                "Set the width of the specified node presentation (specified by ID), and return its rectangle after setting along with the updated diagram image in low resolution.",
                this::setNodePresentationWidth,
                NodePresentationWithWidthDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_node_prst_height",
                "Set the height of the specified node presentation (specified by ID), and return its rectangle after setting along with the updated diagram image in low resolution.",
                this::setNodePresentationHeight,
                NodePresentationWithHeightDTO.class,
                RectangleDTO.class)
        );
    }

    private NodePresentationDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get node presentation information: {}", param);

        INodePresentation nodePresentation = astahProToolSupport.getNodePresentation(param.id());

        return NodePresentationDTOAssembler.toDTO(nodePresentation);
    }

    private RectangleDTO getNodePresentationRectangle(IdDTO param) throws Exception {
        log.debug("Get node presentation rectangle: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());

        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        return RectangleDTOAssembler.toDTO(rectangle2D);
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> setNodePresentationLocation(NodePresentationWithLocationDTO param) throws Exception {
        log.debug("Set node presentation location: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.nodePresentationId());

        txnAstah.run( () -> {
            astahNodePresentation.setLocation(new Point2D.Double(param.locationX(), param.locationY()));
        });

        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        RectangleDTO dto = RectangleDTOAssembler.toDTO(rectangle2D);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(astahNodePresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> setNodePresentationWidth(NodePresentationWithWidthDTO param) throws Exception {
        log.debug("Set node presentation width: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.nodePresentationId());

        txnAstah.run( () -> {
            astahNodePresentation.setWidth(param.width());
        });

        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        RectangleDTO dto = RectangleDTOAssembler.toDTO(rectangle2D);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(astahNodePresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> setNodePresentationHeight(NodePresentationWithHeightDTO param) throws Exception {
        log.debug("Set node presentation height: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.nodePresentationId());

        txnAstah.run( () -> {
            astahNodePresentation.setHeight(param.height());
        });

        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        RectangleDTO dto = RectangleDTOAssembler.toDTO(rectangle2D);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(astahNodePresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }
}
