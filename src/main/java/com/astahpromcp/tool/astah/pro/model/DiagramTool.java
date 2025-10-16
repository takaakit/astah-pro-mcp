package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Rectangle2D;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IDiagram.html
@Slf4j
public class DiagramTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final Path imageOutputDir;

    public DiagramTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, Path imageOutputDir) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.imageOutputDir = imageOutputDir;
   }

   @Override
   public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_dgm_info",
                            "Return detailed information about the specified diagram (specified by ID).",
                            this::getInfo,
                            IdDTO.class,
                            DiagramDTO.class),

                    ToolSupport.definition(
                            "get_dgm_rect",
                            "Return a rectangle (x, y, width, height) representing the boundary of the specified diagram (specified by ID).",
                            this::getDiagramBoundRect,
                            IdDTO.class,
                            RectangleDTO.class),
                            
                    ToolSupport.definition(
                            "get_prst_on_dgm",
                            "Return the list of presentations on the specified diagram (specified by ID).",
                            this::getPresentationsOnDiagram,
                            IdDTO.class,
                            PresentationListDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create diagram tools", e);
            return List.of();
        }
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
}
