package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteAnchorDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.BasicDiagramEditor;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/BasicDiagramEditor.html
@Slf4j
public class BasicDiagramEditorTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private static final double NOTE_MAX_WIDTH = 800.0;

    public BasicDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport, ImageCaptureSupport imageCaptureSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.diagramEditorSupport = diagramEditorSupport;
        this.imageCaptureSupport = imageCaptureSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_note",
                "Create a new note at the specified point (specified by x and y coordinates) on the specified diagram (specified by ID), and return the newly created node presentation of the note along with the updated diagram image in low resolution. The note content is normally wrapped automatically at the right edge of the note, so there is usually no need to insert line breaks in the middle of a sentence to fit the note's display width. On some diagrams, however, the width of a note cannot be adjusted, and this tool function then fails with an error asking for line breaks; in that case, call it again with line breaks inserted at the positions that give the note the width you want.",
                this::createNote,
                NewNoteDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_note_anchor",
                "Create a new note anchor between the specified note (specified by presentation ID) and the specified target presentation (specified by ID) on the specified diagram (specified by ID), and return the newly created link presentation of the note anchor along with the updated diagram image in low resolution.",
                this::createNoteAnchor,
                NewNoteAnchorDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createNote(NewNoteDTO param) throws Exception {
        log.debug("Create note: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        BasicDiagramEditor basicDiagramEditor;
        try {
            basicDiagramEditor = (BasicDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get basic diagram editor.");
        }

        basicDiagramEditor.setDiagram(astahDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            INodePresentation notePresentation = basicDiagramEditor.createNote(
                param.noteContent(),
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));

            // If the note width exceeds the limit, adjust it to the limit.
            if (notePresentation.getWidth() > NOTE_MAX_WIDTH) {
                try {
                    notePresentation.setWidth(NOTE_MAX_WIDTH);
                    notePresentation.setLocation(new Point2D.Double(param.locationX(), param.locationY()));
                } catch (Exception e) {
                    // Some diagram types (e.g. sequence diagrams) do not support resizing note.
                    throw new RuntimeException(String.format("The width of the note cannot be changed. Adjust the width of the note by inserting line breaks at appropriate positions in the content to be written in the note. The width of a note cannot be adjusted on this diagram, so create the note again with line breaks already inserted so that every line fits within %.1f points. This note is %.1f points wide.", NOTE_MAX_WIDTH, notePresentation.getWidth()), e);
                }
            }
            return notePresentation;
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createNoteAnchor(NewNoteAnchorDTO param) throws Exception {
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

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return basicDiagramEditor.createNoteAnchor(
                astahNote,
                astahPresentation);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }
}
