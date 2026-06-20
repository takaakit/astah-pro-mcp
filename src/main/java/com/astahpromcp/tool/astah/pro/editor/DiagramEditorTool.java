package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
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
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
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
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/DiagramEditor.html
@Slf4j
public class DiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;
    private final ImageConvertSupport imageConvertSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public DiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport, ImageConvertSupport imageConvertSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
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
                "Insert an SVG image (specified by SVG code) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image rectangle along with the updated diagram image.",
                this::insertSvgImage,
                NewSvgImageWithPointDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_png_img_on_dgm",
                "Insert a PNG image (specified by image URL) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image rectangle along with the updated diagram image. When specifying a local image file, use the 'file:///' protocol.",
                this::insertPngImage,
                NewPngImageWithPointDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_jpg_img_on_dgm",
                "Insert a JPG image (specified by image URL) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image rectangle along with the updated diagram image. When specifying a local image file, use the 'file:///' protocol.",
                this::insertJpgImage,
                NewJpgImageWithPointDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_rect_on_dgm",
                "Insert a rectangle on the specified diagram (specified by ID), and return the newly created rectangle presentation along with the updated diagram image. For example, use this tool when you want to draw a system boundary on a use case diagram.",
                this::insertRect,
                NewRectDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_txt_on_dgm",
                "Insert a text at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the newly created node presentation of the text along with the updated diagram image.",
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
                "Delete the specified presentation (specified by ID) on the specified diagram (specified by ID), and return the deleted presentation along with the updated diagram image. Note that deleting a presentation does not delete the corresponding element.",
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
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = imageConvertSupport.svgToImage(param.imageSvgCode());

        try {
            transactionManager.beginTransaction();
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            RectangleDTO dto = new RectangleDTO(
                param.locationX(),
                param.locationY(),
                image.getWidth(null),
                image.getHeight(null));

            McpSchema.ImageContent diagramImage = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(diagramImage));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> insertPngImage(McpSyncServerExchange exchange, NewPngImageWithPointDTO param) throws Exception {
        log.debug("Insert PNG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            RectangleDTO dto = new RectangleDTO(
                param.locationX(),
                param.locationY(),
                image.getWidth(null),
                image.getHeight(null));

            McpSchema.ImageContent diagramImage = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(diagramImage));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<RectangleDTO, List<McpSchema.Content>> insertJpgImage(McpSyncServerExchange exchange, NewJpgImageWithPointDTO param) throws Exception {
        log.debug("Insert JPG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            RectangleDTO dto = new RectangleDTO(
                param.locationX(),
                param.locationY(),
                image.getWidth(null),
                image.getHeight(null));

            McpSchema.ImageContent diagramImage = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(diagramImage));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> insertRect(McpSyncServerExchange exchange, NewRectDTO param) throws Exception {
        log.debug("Insert rectangle: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = diagramEditor.createRect(
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()),
                param.width(),
                param.height());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> insertText(McpSyncServerExchange exchange, NewTextWithPointDTO param) throws Exception {
        log.debug("Insert text: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = diagramEditor.createText(
                param.textContent(),
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
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
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        try {
            transactionManager.beginTransaction();
            diagramEditor.deleteDiagram();
            transactionManager.endTransaction();

            return diagramDTO;

        } catch (InvalidEditingException e) {
            transactionManager.abortTransaction();
            throw e;
        }
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
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        try {
            transactionManager.beginTransaction();
            diagramEditor.deletePresentation(astahPresentation);
            transactionManager.endTransaction();

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(presentationDTO, List.of(image));

        } catch (InvalidEditingException e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

}
