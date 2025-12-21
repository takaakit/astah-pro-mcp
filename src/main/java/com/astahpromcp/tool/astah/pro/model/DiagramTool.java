package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ImageFileDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IDiagram.html
@Slf4j
public class DiagramTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final Path imageOutputDir;
    private final boolean includeEditTools;

    public DiagramTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, Path imageOutputDir, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.imageOutputDir = imageOutputDir;
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
            log.error("Failed to create diagram tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_dgm_info",
                        "Return detailed information about the specified diagram (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "get_dgm_rectangle",
                        "Return a rectangle (x, y, width, height) representing the boundary of the specified diagram (specified by ID).",
                        this::getDiagramBoundRect,
                        IdDTO.class,
                        RectangleDTO.class),

                ToolSupport.definition(
                        "get_prsts_on_dgm",
                        "Return the list of presentations on the specified diagram (specified by ID).",
                        this::getPresentationsOnDiagram,
                        IdDTO.class,
                        PresentationListDTO.class),

                ToolSupport.definition(
                        "export_dgm_png_img",
                        "Export a PNG image of the specified diagram (specified by ID), and return the exported image file information. For example, if you need a diagram image file while creating a document, use this tool.",
                        this::exportPngImage,
                        IdDTO.class,
                        ImageFileDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of();
    }

    private DiagramDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get diagram information: {}", param);
        
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());
        
        return DiagramDTOAssembler.toDTO(astahDiagram);
    }

    private RectangleDTO getDiagramBoundRect(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get diagram bound rect: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());

        Rectangle2D astahRectangle = astahDiagram.getBoundRect();
        return RectangleDTOAssembler.toDTO(astahRectangle);
    }

    private PresentationListDTO getPresentationsOnDiagram(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get presentations on diagram: {}", param);
        
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : astahDiagram.getPresentations()) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }
        
        return new PresentationListDTO(presentationDTOs);
    }

    private ImageFileDTO exportPngImage(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Export PNG image: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());

        // Ensure the output directory exists and create it when needed
        log.debug("Create output directory: {}", imageOutputDir);
        try {
            Files.createDirectories(imageOutputDir);
        } catch (Exception e) {
            throw new Exception("Failed to create output directory: " + e.getMessage());
        }

        // Verify that the directory is writable
        if (!Files.isWritable(imageOutputDir)) {
            throw new Exception("Output directory is not writable: " + imageOutputDir);
        }

        // Export an image using the Astah API
        String relativeImagePath;
        try {
            relativeImagePath = astahDiagram.exportImage(imageOutputDir.toString(), "png", 96);
        } catch (Exception e) {
            throw new Exception("Astah API exportImage method failed: " + e.getMessage());
        }
        
        if (relativeImagePath == null || relativeImagePath.trim().isEmpty()) {
            throw new Exception("exportImage returned null or empty file path");
        }
        
        Path absoluteImagePath = Paths.get(imageOutputDir.toString(), relativeImagePath);
        if (!Files.exists(absoluteImagePath)) {
            throw new Exception("Exported image file does not exist: " + absoluteImagePath.toString());
        }
        
        // Get the image width and height
        BufferedImage image = ImageIO.read(absoluteImagePath.toFile());
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        return new ImageFileDTO(absoluteImagePath.toString(), "png", imageWidth, imageHeight);
    }
}
