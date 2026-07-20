package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.ImageConvertSupport;
import com.astahpromcp.tool.common.ImageConvertSupport.RasterizedSvgImage;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeleteDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeletePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewJpgImageWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewPngImageWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewRectDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewSvgImageWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewTextWithPointDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.DiagramEditor;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/DiagramEditor.html
@Slf4j
public class DiagramEditorTool implements ToolProvider {

    private static final double SVG_IMAGE_RASTERIZATION_SCALE = 4.0;

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;
    private final ImageConvertSupport imageConvertSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public DiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport, ImageConvertSupport imageConvertSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.diagramEditorSupport = diagramEditorSupport;
        this.imageConvertSupport = imageConvertSupport;
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
            log.error("Failed to create diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_svg_img_on_dgm",
                "Insert an SVG image (specified by SVG code) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image rectangle along with the updated diagram image in low resolution.",
                this::insertSvgImage,
                NewSvgImageWithPointDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_png_img_on_dgm",
                "Insert a PNG image (specified by image URL) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image rectangle along with the updated diagram image in low resolution. When specifying a local image file, use the 'file:///' protocol.",
                this::insertPngImage,
                NewPngImageWithPointDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_jpg_img_on_dgm",
                "Insert a JPG image (specified by image URL) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image rectangle along with the updated diagram image in low resolution. When specifying a local image file, use the 'file:///' protocol.",
                this::insertJpgImage,
                NewJpgImageWithPointDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_rect_on_dgm",
                "Insert a rectangle on the specified diagram (specified by ID), and return the newly created rectangle presentation along with the updated diagram image in low resolution. For example, use this tool when you want to draw a system boundary on a use case diagram.",
                this::insertRect,
                NewRectDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_txt_on_dgm",
                "Insert a text at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the newly created node presentation of the text along with the updated diagram image in low resolution.",
                this::insertText,
                NewTextWithPointDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "delete_dgm",
                "Delete the specified diagram (specified by ID), and return the deleted diagram.",
                this::deleteDiagram,
                DeleteDiagramDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "delete_prst",
                "Delete the specified presentation (specified by ID) on the specified diagram (specified by ID), and return the deleted presentation along with the updated diagram image in low resolution. Note that deleting a presentation does not delete the corresponding element.",
                this::deletePresentation,
                DeletePresentationDTO.class,
                PresentationDTO.class)
        );
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> insertSvgImage(McpSyncServerExchange exchange, NewSvgImageWithPointDTO param) throws Exception {
        log.debug("Insert SVG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.", e);
        }

        diagramEditor.setDiagram(astahDiagram);

        RasterizedSvgImage svgImage = imageConvertSupport.svgToRasterizedImage(
                param.imageSvgCode(),
                SVG_IMAGE_RASTERIZATION_SCALE);
        Image image = svgImage.image();
        INodePresentation astahImagePresentation = createImagePresentation(
                astahDiagram,
                diagramEditor,
                image,
                param.locationX(),
                param.locationY(),
                svgImage.displayWidth(),
                svgImage.displayHeight());

        RectangleDTO dto = RectangleDTOAssembler.toDTO(astahImagePresentation.getRectangle());

        McpSchema.ImageContent diagramImage = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(diagramImage));
    }

    private INodePresentation createImagePresentation(IDiagram astahDiagram, DiagramEditor diagramEditor, Image image, double locationX, double locationY, int displayWidth, int displayHeight) throws Exception {

        Set<String> beforePresentationIds = presentationIds(astahDiagram);

        return txnAstah.call( () -> {
            // Note: The return value of createImage() is null, so the newly created presentation is identified by diffing the diagram's presentation IDs.
            INodePresentation createdPresentation = diagramEditor.createImage(
                    image,
                    new Point2D.Double(locationX, locationY));

            INodePresentation imagePresentation = createdPresentation != null ? createdPresentation : findNewNodePresentation(astahDiagram, beforePresentationIds);
            if (imagePresentation == null) {
                throw new RuntimeException("Failed to identify the created image presentation.");
            }

            imagePresentation.setWidth(displayWidth);
            imagePresentation.setHeight(displayHeight);

            return imagePresentation;
        });
    }

    private Set<String> presentationIds(IDiagram astahDiagram) throws Exception {
        Set<String> ids = new HashSet<>();
        for (IPresentation presentation : astahDiagram.getPresentations()) {
            ids.add(presentation.getID());
        }

        return ids;
    }

    private INodePresentation findNewNodePresentation(IDiagram astahDiagram, Set<String> beforePresentationIds) throws Exception {

        for (IPresentation presentation : astahDiagram.getPresentations()) {
            if (beforePresentationIds.contains(presentation.getID())) {
                continue;
            }
            if (presentation instanceof INodePresentation nodePresentation) {
                return nodePresentation;
            }
        }

        return null;
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> insertPngImage(McpSyncServerExchange exchange, NewPngImageWithPointDTO param) throws Exception {
        log.debug("Insert PNG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.", e);
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        txnAstah.run( () -> {
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
        });

        RectangleDTO dto = new RectangleDTO(
            param.locationX(),
            param.locationY(),
            image.getWidth(null),
            image.getHeight(null));

        McpSchema.ImageContent diagramImage = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(diagramImage));
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> insertJpgImage(McpSyncServerExchange exchange, NewJpgImageWithPointDTO param) throws Exception {
        log.debug("Insert JPG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.", e);
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        txnAstah.run( () -> {
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
        });

        RectangleDTO dto = new RectangleDTO(
            param.locationX(),
            param.locationY(),
            image.getWidth(null),
            image.getHeight(null));

        McpSchema.ImageContent diagramImage = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(diagramImage));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> insertRect(McpSyncServerExchange exchange, NewRectDTO param) throws Exception {
        log.debug("Insert rectangle: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.", e);
        }

        diagramEditor.setDiagram(astahDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return diagramEditor.createRect(
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()),
                param.width(),
                param.height());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> insertText(McpSyncServerExchange exchange, NewTextWithPointDTO param) throws Exception {
        log.debug("Insert text: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.", e);
        }

        diagramEditor.setDiagram(astahDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return diagramEditor.createText(
                param.textContent(),
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private DiagramDTO deleteDiagram(McpSyncServerExchange exchange, DeleteDiagramDTO param) throws Exception {
        log.debug("Deleting diagram: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        // Delete target diagram
        DiagramDTO diagramDTO = DiagramDTOAssembler.toDTO(astahDiagram);

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.", e);
        }

        diagramEditor.setDiagram(astahDiagram);

        txnAstah.run( () -> {
            diagramEditor.deleteDiagram();
        });

        return diagramDTO;
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> deletePresentation(McpSyncServerExchange exchange, DeletePresentationDTO param) throws Exception {
        log.debug("Deleting presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.targetPresentationId());

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        // Delete target presentation
        PresentationDTO presentationDTO = PresentationDTOAssembler.toDTO(astahPresentation);

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.", e);
        }

        diagramEditor.setDiagram(astahDiagram);

        txnAstah.run( () -> {
            diagramEditor.deletePresentation(astahPresentation);
        });

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(presentationDTO, List.of(image));
    }

}
