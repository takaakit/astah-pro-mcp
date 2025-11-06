package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewUseCaseDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.UseCaseDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UseCaseDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private UseCaseDiagramEditorTool tool;
    private Method createUseCaseDiagram;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        UseCaseDiagramEditor useCaseDiagramEditor = projectAccessor.getDiagramEditorFactory().getUseCaseDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/UseCaseDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new UseCaseDiagramEditorTool(
            projectAccessor,
            transactionManager,
            useCaseDiagramEditor,
            astahProToolSupport,
            true);

        // createUseCaseDiagram() method
        createUseCaseDiagram = TestSupport.getAccessibleMethod(
            UseCaseDiagramEditorTool.class,
            "createUseCaseDiagram",
            McpSyncServerExchange.class,
            NewUseCaseDiagramDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createUseCaseDiagram_ok() throws Exception {
        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewUseCaseDiagramDTO inputDTO = new NewUseCaseDiagramDTO(
            astahPackage.getId(),
            "Test Use Case Diagram"
        );

        // ----------------------------------------
        // Call createUseCaseDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createUseCaseDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
