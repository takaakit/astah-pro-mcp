package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteAnchorDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.BasicDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/BasicDiagramEditor.html
@Slf4j
public class BasicDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;
    private final boolean includeEditTools;

    public BasicDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport, boolean includeEditTools) {
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
            log.error("Failed to create basic diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_note",
                        "Create a new note at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the newly created note information (node presentation information).",
                        this::createNote,
                        NewNoteDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_note_anc",
                        "Create a new note anchor between the specified note (specified by ID) and the specified target presentation (specified by ID) on the specified diagram (specified by ID), and return the newly created note anchor information (link presentation information).",
                        this::createNoteAnchor,
                        NewNoteAnchorDTO.class,
                        LinkPresentationDTO.class)
        );
    }

    private NodePresentationDTO createNote(McpSyncServerExchange exchange, NewNoteDTO param) throws Exception {
        log.debug("Create note: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        BasicDiagramEditor basicDiagramEditor;
        try {
            basicDiagramEditor = (BasicDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get basic diagram editor.");
        }

        basicDiagramEditor.setDiagram(astahDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = basicDiagramEditor.createNote(
                param.noteContent(),
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

    private LinkPresentationDTO createNoteAnchor(McpSyncServerExchange exchange, NewNoteAnchorDTO param) throws Exception {
        log.debug("Create note anchor: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());
        INodePresentation astahNote = astahProToolSupport.getNodePresentation(param.targetNoteId());
        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.targetPresentationId());

        BasicDiagramEditor basicDiagramEditor;
        try {
            basicDiagramEditor = (BasicDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get basic diagram editor.");
        }

        basicDiagramEditor.setDiagram(astahDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLinkPresentation = basicDiagramEditor.createNoteAnchor(
                astahNote,
                astahPresentation);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
