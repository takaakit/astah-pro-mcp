package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
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
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO.Type;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Value;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Locale;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/INodePresentation.html
@Slf4j
public class NodePresentationTool extends AstahToolProvider {
    
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    
    public NodePresentationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.imageCaptureSupport = imageCaptureSupport;
    }
    
    @Override
    protected List<ToolDefinition> createTools() {
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
                RectangleDTO.class),
            
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_node_prst_location",
                "Set the location (specified by x and y coordinates) of the specified node presentation (specified by ID), and return its rectangle after setting along with the updated diagram image in low resolution. For a lifeline, the specified Y coordinate is ignored.",
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
        INodePresentation nodePresentation = astahProToolSupport.getNodePresentation(param.id());
        
        return NodePresentationDTOAssembler.toDTO(nodePresentation);
    }
    
    private RectangleDTO getNodePresentationRectangle(IdDTO param) throws Exception {
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());
        
        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        return RectangleDTOAssembler.toDTO(rectangle2D);
    }
    
    private Pair<RectangleDTO, List<McpSchema.Content>> setNodePresentationLocation(NodePresentationWithLocationDTO param) throws Exception {
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.nodePresentationId());
        
        try {
            txnAstah.run(() -> {
                astahNodePresentation.setLocation(new Point2D.Double(param.locationX(), param.locationY()));
            });
            
        } catch (InvalidEditingException e) {
            if (Type.COMBINED_FRAGMENT.matches(astahNodePresentation.getType())) {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " A combined fragment cannot be moved or resized. Change its height with set_height_of_interaction_operand, and its location or width as dgm_guide describes.");
            } else {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " If this presentation cannot be moved or resized on this diagram, delete it and create it again at the location and size you want.");
            }
        }
        
        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        RectangleDTO dto = RectangleDTOAssembler.toDTO(rectangle2D);
        
        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(astahNodePresentation.getDiagram().getId());
        
        return Pair.of(dto, List.of(thumbnailContent));
    }
    
    private Pair<RectangleDTO, List<McpSchema.Content>> setNodePresentationWidth(NodePresentationWithWidthDTO param) throws Exception {
        if (param.width() <= 0) {
            throw new IllegalArgumentException("The width of a node presentation must be greater than 0: " + param.width());
        }
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.nodePresentationId());
        rejectResizingIconLifeline(astahNodePresentation);

        boolean icon = isDrawnAsIcon(astahNodePresentation);
        double newWidth = param.width();
        double newHeight = icon
            ? astahNodePresentation.getHeight() * newWidth / astahNodePresentation.getWidth()
            : astahNodePresentation.getHeight();

        try {
            txnAstah.run(() -> {
                astahNodePresentation.setWidth(newWidth);
                if (icon) {
                    astahNodePresentation.setHeight(newHeight);
                }
            });

        } catch (InvalidEditingException e) {
            if (Type.COMBINED_FRAGMENT.matches(astahNodePresentation.getType())) {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " A combined fragment cannot be moved or resized. Change its height with set_height_of_interaction_operand, and its location or width as dgm_guide describes.");
            } else if (InvalidEditingException.UNSUPPORTED_ELEMENT_ERROR_KEY.equals(e.getKey())) {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " If this presentation cannot be moved or resized on this diagram, delete it and create it again at the location and size you want.");
            } else if (icon) {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + iconSizeRejectedHint(newWidth, newHeight));
            } else {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " The width may be below the minimum this presentation allows. Try a larger width; deleting the presentation does not get around this.");
            }
        }
        
        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        RectangleDTO dto = RectangleDTOAssembler.toDTO(rectangle2D);
        
        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(astahNodePresentation.getDiagram().getId());
        
        return Pair.of(dto, List.of(thumbnailContent));
    }
    
    private Pair<RectangleDTO, List<McpSchema.Content>> setNodePresentationHeight(NodePresentationWithHeightDTO param) throws Exception {
        if (param.height() <= 0) {
            throw new IllegalArgumentException("The height of a node presentation must be greater than 0: " + param.height());
        }
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.nodePresentationId());
        rejectResizingIconLifeline(astahNodePresentation);

        boolean icon = isDrawnAsIcon(astahNodePresentation);
        double newHeight = param.height();
        double newWidth = icon
            ? astahNodePresentation.getWidth() * newHeight / astahNodePresentation.getHeight()
            : astahNodePresentation.getWidth();

        try {
            txnAstah.run(() -> {
                if (icon) {
                    astahNodePresentation.setWidth(newWidth);
                }
                astahNodePresentation.setHeight(newHeight);
            });

        } catch (InvalidEditingException e) {
            if (Type.COMBINED_FRAGMENT.matches(astahNodePresentation.getType())) {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " A combined fragment cannot be moved or resized. Change its height with set_height_of_interaction_operand, and its location or width as dgm_guide describes.");
            } else if (InvalidEditingException.UNSUPPORTED_ELEMENT_ERROR_KEY.equals(e.getKey())) {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " If this presentation cannot be moved or resized on this diagram, delete it and create it again at the location and size you want.");
            } else if (icon) {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + iconSizeRejectedHint(newWidth, newHeight));
            } else {
                throw new InvalidEditingException(
                    e.getKey(),
                    e.getMessage() + " The height may be below the minimum this presentation allows, or fixed, as for a lifeline on a sequence diagram. Deleting the presentation does not get around this.");
            }
        }
        
        Rectangle2D rectangle2D = astahNodePresentation.getRectangle();
        RectangleDTO dto = RectangleDTOAssembler.toDTO(rectangle2D);
        
        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(astahNodePresentation.getDiagram().getId());

        return Pair.of(dto, List.of(thumbnailContent));
    }

    // Astah stretches an icon (an actor, the circle of an interface, a robustness icon) to fill whatever rectangle it is given, so an icon is resized by one ratio in both directions to keep its proportions.
    private static boolean isDrawnAsIcon(INodePresentation nodePresentation) {
        return Value.NOTATION_TYPE_ICON.equals(nodePresentation.getProperty(Key.NOTATION_TYPE))
            && nodePresentation.getWidth() > 0
            && nodePresentation.getHeight() > 0;
    }

    // Astah fixes the height of a lifeline on a sequence diagram, so an icon lifeline there cannot keep its proportions: a new width would only stretch the icon sideways, and an agent widens it to make room for its name, which is drawn outside the icon anyway.
    private static void rejectResizingIconLifeline(INodePresentation nodePresentation) {
        if (Type.LIFELINE.matches(nodePresentation.getType())
            && nodePresentation.getDiagram() instanceof ISequenceDiagram
            && isDrawnAsIcon(nodePresentation)) {
            throw new IllegalArgumentException(String.format(
                Locale.ROOT,
                "The lifeline '%s' is drawn as an icon and cannot be resized: Astah fixes the height of a lifeline on a sequence diagram, so a new width would only stretch the icon sideways. Its name is drawn below the icon, outside its rectangle, so the icon does not need to be wider to fit it. Leave its size as it is, and keep the neighboring lifelines far enough apart for the name.",
                nodePresentation.getLabel()));
        }
    }

    private static String iconSizeRejectedHint(double width, double height) {
        return String.format(
            Locale.ROOT,
            " This presentation is drawn as an icon, so its width and height are changed by the same ratio, to %.1f x %.1f, to keep the icon from being distorted. That size may be below the minimum this icon allows. Try a larger size; deleting the presentation does not get around this.",
            width,
            height);
    }
}
