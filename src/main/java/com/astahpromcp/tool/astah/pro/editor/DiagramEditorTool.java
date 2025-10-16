package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeleteDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeletePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewTextWithPointDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.DiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/DiagramEditor.html
@Slf4j
public class DiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;

    public DiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.diagramEditorSupport = diagramEditorSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.definition(
                        "create_txt_on_dgm",
                        "Create a new text at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the newly created node presentation information.",
                        this::createText,
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
        } catch (Exception e) {
            log.error("Failed to create diagram editor tools", e);
            return List.of();
        }
    }

    private NodePresentationDTO createText(McpSyncServerExchange exchange, NewTextWithPointDTO param) throws Exception {
        log.debug("Create text: {}", param);

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
