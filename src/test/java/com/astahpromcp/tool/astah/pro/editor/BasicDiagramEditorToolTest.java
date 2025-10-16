package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteAnchorDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNoteDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private BasicDiagramEditorTool tool;
    private Method createNote;
    private Method createNoteAnchor;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/editor/BasicDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);

        // Tool
        tool = new BasicDiagramEditorTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            diagramEditorSupport);

        // createNote() method
        createNote = TestSupport.getAccessibleMethod(
            BasicDiagramEditorTool.class,
            "createNote",
            McpSyncServerExchange.class,
            NewNoteDTO.class);

        // createNoteAnchor() method
        createNoteAnchor = TestSupport.getAccessibleMethod(
            BasicDiagramEditorTool.class,
            "createNoteAnchor",
            McpSyncServerExchange.class,
            NewNoteAnchorDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createNote_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createNote,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createNoteAnchor_ok() throws Exception {
        // Get class diagram
        IClassDiagram astahClassDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");

        // Get note
        INodePresentation astahNote = (INodePresentation) TestSupport.instance().getPresentation(
            "Note",
            "Note0");
        
        // Get class (node presentation)
        INodePresentation astahClassFoo = (INodePresentation) TestSupport.instance().getPresentation(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createNoteAnchor,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
