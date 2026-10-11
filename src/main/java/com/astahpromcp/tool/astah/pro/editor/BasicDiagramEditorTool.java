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
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.editor.BasicDiagramEditor;
import com.change_vision.jude.api.inf.model.IComment;
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
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        BasicDiagramEditor basicDiagramEditor;
        try {
            basicDiagramEditor = (BasicDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get basic diagram editor.", e);
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

        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(param.targetDiagramId());

        return Pair.of(dto, List.of(thumbnailContent));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createNoteAnchor(NewNoteAnchorDTO param) throws Exception {
        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());
        INodePresentation astahNote = astahProToolSupport.getNodePresentation(param.targetNoteId());
        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.targetPresentationId());

        requireNote(astahNote);
        requireOnDiagram(astahNote, "note", astahDiagram);
        requireOnDiagram(astahPresentation, "target presentation", astahDiagram);
        requireNoNoteAnchorBetween(astahNote, astahPresentation);

        BasicDiagramEditor basicDiagramEditor;
        try {
            basicDiagramEditor = (BasicDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get basic diagram editor.", e);
        }

        basicDiagramEditor.setDiagram(astahDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return basicDiagramEditor.createNoteAnchor(
                astahNote,
                astahPresentation);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(param.targetDiagramId());

        return Pair.of(dto, List.of(thumbnailContent));
    }

    // Astah casts the model of the note to a comment, so any other node fails with an unexplained error
    private static void requireNote(INodePresentation presentation) {
        if (presentation.getModel() instanceof IComment) {
            return;
        }

        throw new IllegalArgumentException(String.format(
                "Presentation (ID: %s, type: %s) is not a note. Specify the presentation ID of a note as the target note.",
                presentation.getID(), presentation.getType()));
    }

    // Astah accepts presentations on other diagrams, and leaves a note anchor that no diagram draws
    private static void requireOnDiagram(IPresentation presentation, String role, IDiagram diagram) {
        IDiagram actualDiagram = presentation.getDiagram();
        if (actualDiagram != null && diagram.getId().equals(actualDiagram.getId())) {
            return;
        }

        throw new IllegalArgumentException(String.format(
                "The %s (ID: %s) is not on the target diagram '%s' (ID: %s)%s. A note anchor connects a note and a presentation on the same diagram, so specify presentations on the target diagram.",
                role, presentation.getID(), diagram.getName(), diagram.getId(),
                actualDiagram == null ? "" : " but on '" + actualDiagram.getName() + "' (ID: " + actualDiagram.getId() + ")"));
    }

    // Astah accepts the same note anchor again, and the copies overlap exactly into one visible line
    private static void requireNoNoteAnchorBetween(INodePresentation note, IPresentation target) {
        for (ILinkPresentation link : note.getLinks()) {
            if ("NoteAnchor".equals(link.getType()) && connects(link, note, target)) {
                throw new IllegalArgumentException(String.format(
                        "A note anchor (ID: %s) already connects the note (ID: %s) and the target presentation (ID: %s). Use the existing note anchor instead of creating another one.",
                        link.getID(), note.getID(), target.getID()));
            }
        }
    }

    // A note anchor has no direction, so either end may be the note
    private static boolean connects(ILinkPresentation link, IPresentation end1, IPresentation end2) {
        String sourceId = link.getSource().getID();
        String targetId = link.getTarget().getID();
        return (sourceId.equals(end1.getID()) && targetId.equals(end2.getID()))
            || (sourceId.equals(end2.getID()) && targetId.equals(end1.getID()));
    }
}
