package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeleteDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeletePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewTextWithPointDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private DiagramEditorTool tool;
    private Method createText;
    private Method deleteDiagram;
    private Method deletePresentation;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/editor/DiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);

        // Tool
        tool = new DiagramEditorTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            diagramEditorSupport);

        // createText() method
        createText = TestSupport.getAccessibleMethod(
            DiagramEditorTool.class,
            "createText",
            McpSyncServerExchange.class,
            NewTextWithPointDTO.class);

        // deleteDiagram() method
        deleteDiagram = TestSupport.getAccessibleMethod(
            DiagramEditorTool.class,
            "deleteDiagram",
            McpSyncServerExchange.class,
            DeleteDiagramDTO.class);

        // deletePresentation() method
        deletePresentation = TestSupport.getAccessibleMethod(
            DiagramEditorTool.class,
            "deletePresentation",
            McpSyncServerExchange.class,
            DeletePresentationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createText_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        NewTextWithPointDTO inputDTO = new NewTextWithPointDTO(
            classDiagram.getId(),
            "Test\nText",
            10,
            20);

        // ----------------------------------------
        // Call createText()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createText,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void deleteDiagram_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        DeleteDiagramDTO inputDTO = new DeleteDiagramDTO(
            classDiagram.getId());

        // ----------------------------------------
        // Call deleteDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            deleteDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check that the diagram does not exist
        assertNull(TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0"));
    }

    @Test
    void deletePresentation_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");

        // Get class (node presentation)
        IPresentation classFoo = TestSupport.instance().getPresentation(
            "Class",
            "Foo");

        // Create input DTO
        DeletePresentationDTO inputDTO = new DeletePresentationDTO(
            classDiagram.getId(),
            classFoo.getID());

        // ----------------------------------------
        // Call deletePresentation()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            deletePresentation,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check that the presentation does not exist
        assertNull(TestSupport.instance().getPresentation(
            "Class",
            "Foo"));
    }
}
