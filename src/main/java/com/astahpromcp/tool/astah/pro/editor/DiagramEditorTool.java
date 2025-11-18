package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeleteDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeletePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewJpgImageWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewPngImageWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewSvgImageWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewTextWithPointDTO;
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
import lombok.extern.slf4j.Slf4j;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/DiagramEditor.html
@Slf4j
public class DiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;
    private final boolean includeEditTools;

    public DiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.diagramEditorSupport = diagramEditorSupport;
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
                ToolSupport.definition(
                        "insert_svg_img_on_dgm",
                        "Insert an SVG image (specified by SVG code) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image.",
                        this::insertSvgImage,
                        NewSvgImageWithPointDTO.class,
                        RectangleDTO.class),

                ToolSupport.definition(
                        "insert_png_img_on_dgm",
                        "Insert a PNG image (specified by image URL) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image. When specifying a local image file, use the 'file:///' protocol.",
                        this::insertPngImage,
                        NewPngImageWithPointDTO.class,
                        RectangleDTO.class),

                ToolSupport.definition(
                        "insert_jpg_img_on_dgm",
                        "Insert a JPG image (specified by image URL) at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the rectangle (x, y, width, height) representing the boundary of the newly created image. When specifying a local image file, use the 'file:///' protocol.",
                        this::insertJpgImage,
                        NewJpgImageWithPointDTO.class,
                        RectangleDTO.class),

                ToolSupport.definition(
                        "insert_txt_on_dgm",
                        "Insert a text at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the node presentation information of the newly created text.",
                        this::insertText,
                        NewTextWithPointDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "delete_dgm",
                        "Delete the specified diagram (specified by ID), and return the deleted diagram information.",
                        this::deleteDiagram,
                        DeleteDiagramDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "delete_prst",
                        "Delete the specified presentation (specified by ID) on the specified diagram (specified by ID), and return the deleted presentation information.",
                        this::deletePresentation,
                        DeletePresentationDTO.class,
                        PresentationDTO.class)
        );
    }

    private RectangleDTO insertSvgImage(McpSyncServerExchange exchange, NewSvgImageWithPointDTO param) throws Exception {
        log.debug("Insert SVG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = svgToImage(param.imageSvgCode());

        try {
            transactionManager.beginTransaction();
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            return new RectangleDTO(
                param.locationX(),
                param.locationY(),
                image.getWidth(null),
                image.getHeight(null));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Image svgToImage(String svgCode) {
        if (svgCode == null || svgCode.isBlank()) {
            throw new IllegalArgumentException("SVG code must not be null or blank");
        }

        // Validate SVG code
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        try (StringReader reader = new StringReader(svgCode)) {
            Document document = factory.createDocument("internal:svg", reader);
            if (document == null || document.getDocumentElement() == null
                    || !"svg".equals(document.getDocumentElement().getLocalName())) {
                throw new IllegalArgumentException("SVG code must have an <svg> root element");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SVG markup", e);
        }

        // Create ImageTranscoder
        final BufferedImage[] imageHolder = new BufferedImage[1];
        ImageTranscoder transcoder = new ImageTranscoder() {

            @Override
            public BufferedImage createImage(int width, int height) {
                return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }

            @Override
            public void writeImage(BufferedImage img, TranscoderOutput out) throws TranscoderException {
                imageHolder[0] = img;
            }
        };

        // Convert SVG to Image
        try (StringReader reader = new StringReader(svgCode)) {
            TranscoderInput input = new TranscoderInput(reader);
            transcoder.transcode(input, null);
            BufferedImage result = imageHolder[0];
            if (result == null) {
                throw new IllegalStateException("ImageTranscoder did not produce an image");
            }
            return result;

        } catch (TranscoderException e) {
            throw new IllegalArgumentException("Failed to convert SVG to Image", e);
        }
    }

    private RectangleDTO insertPngImage(McpSyncServerExchange exchange, NewPngImageWithPointDTO param) throws Exception {
        log.debug("Insert PNG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            return new RectangleDTO(
                param.locationX(),
                param.locationY(),
                image.getWidth(null),
                image.getHeight(null));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Image urlToImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL must not be null or blank");
        }

        try {
            URI uri = URI.create(imageUrl);
            try (InputStream inputStream = uri.toURL().openStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new IllegalArgumentException("Failed to read image from URL: " + imageUrl);
                }
                return image;
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to load image from URL: " + imageUrl, e);
        }
    }

    private RectangleDTO insertJpgImage(McpSyncServerExchange exchange, NewJpgImageWithPointDTO param) throws Exception {
        log.debug("Insert JPG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        DiagramEditor diagramEditor;
        try {
            diagramEditor = diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get diagram editor.");
        }

        diagramEditor.setDiagram(astahDiagram);

        Image image = urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            // Note: The return value of createImage() is null (likely due to an API bug), so the return value cannot be used.
            diagramEditor.createImage(
                image,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            return new RectangleDTO(
                param.locationX(),
                param.locationY(),
                image.getWidth(null),
                image.getHeight(null));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO insertText(McpSyncServerExchange exchange, NewTextWithPointDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahNodePresentation);

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

    private PresentationDTO deletePresentation(McpSyncServerExchange exchange, DeletePresentationDTO param) throws Exception {
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

            return presentationDTO;

        } catch (InvalidEditingException e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

}
