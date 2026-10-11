package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteAnchorDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private BasicDiagramEditorTool tool;
    private Method createNote;
    private Method createNoteAnchor;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/editor/BasicDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createThumbnailContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new BasicDiagramEditorTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            diagramEditorSupport,
            imageCaptureSupport);

        // createNote() method
        createNote = TestSupport.getAccessibleMethod(
            BasicDiagramEditorTool.class,
            "createNote",
            NewNoteDTO.class);

        // createNoteAnchor() method
        createNoteAnchor = TestSupport.getAccessibleMethod(
            BasicDiagramEditorTool.class,
            "createNoteAnchor",
            NewNoteAnchorDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createNote_ok_1() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Create input DTO
        NewNoteDTO inputDTO = new NewNoteDTO(
            classDiagram.getId(),
            "Test Note",
            100,
            200);

        // ----------------------------------------
        // Call createNote()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createNote,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createNote_ok_2() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Create input DTO
        NewNoteDTO inputDTO = new NewNoteDTO(
            classDiagram.getId(),
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            100,
            200);

        // ----------------------------------------
        // Call createNote()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createNote,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Get created note
        INodePresentation createdNote = (INodePresentation) TestSupport.instance().getPresentationById(
            outputDTO.presentation().id());

        // Check with the created note
        assertEquals(800.0, createdNote.getWidth(), 0.1);
        assertEquals(100.0, createdNote.getLocation().getX(), 0.1);
        assertEquals(200.0, createdNote.getLocation().getY(), 0.1);
    }

    @Test
    void createNoteAnchor_ok() throws Exception {
        // Get class diagram
        IClassDiagram astahClassDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get note
        INodePresentation astahNote = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Note",
            "Note0");

        // Get class (node presentation)
        INodePresentation astahClassFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        NewNoteAnchorDTO inputDTO = new NewNoteAnchorDTO(
            astahClassDiagram.getId(),
            astahNote.getID(),
            astahClassFoo.getID());

        // ----------------------------------------
        // Call createNoteAnchor()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createNoteAnchor,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createNoteAnchor_ng_noteOnAnotherDiagram() throws Exception {
        INodePresentation[] elsewhere = drawNoteAndClassOnAnotherDiagram();

        assertNoteAnchorRejected(
            elsewhere[0],
            getNodePresentation("Class", "Foo"),
            "'Another Class Diagram'");
    }

    @Test
    void createNoteAnchor_ng_targetOnAnotherDiagram() throws Exception {
        INodePresentation[] elsewhere = drawNoteAndClassOnAnotherDiagram();

        assertNoteAnchorRejected(
            getNodePresentation("Note", "Note0"),
            elsewhere[1],
            "'Another Class Diagram'");
    }

    @Test
    void createNoteAnchor_ng_bothOnAnotherDiagram() throws Exception {
        INodePresentation[] elsewhere = drawNoteAndClassOnAnotherDiagram();

        // Astah would draw the anchor on the target diagram, where neither end is
        assertNoteAnchorRejected(
            elsewhere[0],
            elsewhere[1],
            "'Another Class Diagram'");
    }

    @Test
    void createNoteAnchor_ng_notANote() throws Exception {
        assertNoteAnchorRejected(
            getNodePresentation("Class", "Foo"),
            getNodePresentation("Note", "Note0"),
            "is not a note");
    }

    @Test
    void createNoteAnchor_ng_alreadyAnchored() throws Exception {
        INodePresentation note = getNodePresentation("Note", "Note0");
        INodePresentation foo = getNodePresentation("Class", "Foo");
        String anchorId = invokeCreateNoteAnchor(note, foo).presentation().id();

        assertNoteAnchorRejected(note, foo, anchorId);
    }

    @Test
    void createNoteAnchor_ng_alreadyAnchoredFromTheOtherNote() throws Exception {
        INodePresentation note = getNodePresentation("Note", "Note0");
        INodePresentation otherNote = (INodePresentation) TestSupport.instance().getPresentationById(
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createNote,
                tool,
                new NewNoteDTO(getClassDiagram0().getId(), "Other Note", 100, 300),
                NodePresentationDTO.class).presentation().id());
        String anchorId = invokeCreateNoteAnchor(note, otherNote).presentation().id();

        // A note anchor has no direction, so the reversed pair is the same anchor
        assertNoteAnchorRejected(otherNote, note, anchorId);
    }

    @Test
    void createNote_ok_succeedsWithANoticeWhenOnlyTheThumbnailFails(@TempDir Path workspace) throws Exception {
        // A plain file where the image output directory must go makes every thumbnail fail, on every platform.
        Path imageOutputDir = Files.createFile(workspace.resolve("images"));
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        BasicDiagramEditorTool toolWithFailingThumbnails = new BasicDiagramEditorTool(
            projectAccessor,
            new TransactionSupport(projectAccessor.getTransactionManager()),
            astahProToolSupport,
            new DiagramEditorSupport(projectAccessor),
            new ImageCaptureSupport(astahProToolSupport, new SystemPropertySupport(), imageOutputDir));

        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");
        int notesBefore = countNotes(classDiagram);

        // ----------------------------------------
        // Call create_note through its tool handler
        // ----------------------------------------
        McpSchema.CallToolResult result = call(toolWithFailingThumbnails, "create_note", Map.of(
            "targetDiagramId", classDiagram.getId(),
            "noteContent", "Test Note",
            "locationX", 100,
            "locationY", 200));

        // Check the result and the diagram
        assertFalse(result.isError(), "Only the thumbnail failed, so the call must succeed: " + result.content());
        assertNotNull(result.structuredContent(), "The created note must still be returned");
        String notice = assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text();
        assertTrue(notice.contains("do not repeat it"), "The notice must keep the client from creating the note again: " + notice);
        assertEquals(notesBefore + 1, countNotes(classDiagram), "The note must be created exactly once");
    }

    private void assertNoteAnchorRejected(INodePresentation note, INodePresentation target, String expectedMessagePart) throws Exception {
        int noteLinksBefore = note.getLinks().length;
        int targetLinksBefore = target.getLinks().length;

        // ----------------------------------------
        // Call createNoteAnchor()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> invokeCreateNoteAnchor(note, target));

        // Check exception
        IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(cause.getMessage().contains(expectedMessagePart), cause.getMessage());

        // Check no note anchor is created
        assertEquals(noteLinksBefore, note.getLinks().length);
        assertEquals(targetLinksBefore, target.getLinks().length);
    }

    private LinkPresentationDTO invokeCreateNoteAnchor(INodePresentation note, INodePresentation target) throws Exception {
        return TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createNoteAnchor,
            tool,
            new NewNoteAnchorDTO(getClassDiagram0().getId(), note.getID(), target.getID()),
            LinkPresentationDTO.class);
    }

    private INodePresentation[] drawNoteAndClassOnAnotherDiagram() throws Exception {
        return new TransactionSupport(projectAccessor.getTransactionManager()).call(() -> {
            IClass clazz = projectAccessor.getModelEditorFactory().getBasicModelEditor().createClass(projectAccessor.getProject(), "Elsewhere");
            ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
            classDiagramEditor.setDiagram(classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Another Class Diagram"));
            return new INodePresentation[] {
                classDiagramEditor.createNote("Elsewhere Note", new Point2D.Double(0, 0)),
                classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(200, 0))
            };
        });
    }

    private static IClassDiagram getClassDiagram0() {
        return (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");
    }

    private static INodePresentation getNodePresentation(String type, String label) {
        return (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(type, label);
    }

    private static McpSchema.CallToolResult call(BasicDiagramEditorTool tool, String toolName, Map<String, Object> arguments) {
        ToolDefinition definition = tool.createToolDefinitions().stream()
            .filter(candidate -> candidate.toolSchema().name().equals(toolName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No tool function is named " + toolName));

        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(toolName, arguments, null));
    }

    private static int countNotes(IDiagram diagram) throws Exception {
        int notes = 0;
        for (IPresentation presentation : diagram.getPresentations()) {
            if ("Note".equals(presentation.getType())) {
                notes++;
            }
        }
        return notes;
    }
}
