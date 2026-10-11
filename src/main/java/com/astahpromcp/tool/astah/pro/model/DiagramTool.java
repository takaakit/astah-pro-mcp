package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ImageFileDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationGeometryDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationGeometryDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationGeometriesDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationGeometryDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationGeometryDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IDiagram.html
@Slf4j
public class DiagramTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;

    public DiagramTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.imageCaptureSupport = imageCaptureSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_dgm_info",
                "Return model element information about the specified diagram (specified by ID).",
                this::getInfo,
                IdDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_dgm_rectangle",
                "Return a rectangle (x, y, width, height) representing the boundary of the specified diagram (specified by ID).",
                this::getDiagramBoundRect,
                IdDTO.class,
                RectangleDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_prsts_on_dgm",
                "Return the list of presentations on the specified diagram (specified by ID).",
                this::getPresentationsOnDiagram,
                IdDTO.class,
                PresentationListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_prst_geometries_on_dgm",
                "Return the geometry of every presentation on the specified diagram (specified by ID), with node presentations and link presentations in separate lists: the drawn rectangle of each node presentation and the drawn points of each link presentation, in diagram coordinates.",
                this::getPresentationGeometriesOnDiagram,
                IdDTO.class,
                PresentationGeometriesDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "export_dgm_png_img",
                "Export a PNG image of the specified diagram (specified by ID), and return the exported image file information. For example, if you need a diagram image file while creating a document, use this tool.",
                this::exportPngImage,
                IdDTO.class,
                ImageFileDTO.class)
        );
    }

    private DiagramDTO getInfo(IdDTO param) throws Exception {
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());
        
        return DiagramDTOAssembler.toDTO(astahDiagram);
    }

    private RectangleDTO getDiagramBoundRect(IdDTO param) throws Exception {
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());

        Rectangle2D astahRectangle = astahDiagram.getBoundRect();
        return RectangleDTOAssembler.toDTO(astahRectangle);
    }

    private PresentationListDTO getPresentationsOnDiagram(IdDTO param) throws Exception {
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : AstahProToolSupport.getPresentationsOnDiagram(astahDiagram)) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }
        
        return new PresentationListDTO(presentationDTOs);
    }

    private PresentationGeometriesDTO getPresentationGeometriesOnDiagram(IdDTO param) throws Exception {
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());

        List<NodePresentationGeometryDTO> nodePrstGeoDTOs = new ArrayList<>();
        List<LinkPresentationGeometryDTO> linkPrstGeoDTOs = new ArrayList<>();
        for (IPresentation presentation : AstahProToolSupport.getPresentationsOnDiagram(astahDiagram)) {
            if (presentation instanceof INodePresentation nodePresentation) {
                nodePrstGeoDTOs.add(NodePresentationGeometryDTOAssembler.toDTO(nodePresentation));
            } else if (presentation instanceof ILinkPresentation linkPresentation) {
                linkPrstGeoDTOs.add(LinkPresentationGeometryDTOAssembler.toDTO(linkPresentation));
            }
        }

        return new PresentationGeometriesDTO(
            NameIdTypeDTOAssembler.toDTO(astahDiagram),
            RectangleDTOAssembler.toDTO(astahDiagram.getBoundRect()),
            nodePrstGeoDTOs,
            linkPrstGeoDTOs);
    }

    private ImageFileDTO exportPngImage(IdDTO param) throws Exception {
        if (projectAccessor.getTransactionManager().isInTransaction()) {
            throw new Exception("A diagram cannot be exported while an Astah transaction is open, which in an mcp tool script means after the run's first edit. Remove this call, run the edits again, and export in a separate run that makes no edits once they have been committed.");
        }

        ImageCaptureSupport.ExportedImageFile exported = imageCaptureSupport.exportDiagramImageFile(param.id());

        return new ImageFileDTO(exported.path().toString(), "png", exported.width(), exported.height());
    }
}
