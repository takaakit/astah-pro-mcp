package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.RequirementDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RequirementDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private RequirementDiagramEditorTool tool;
    private Method createRequirementDiagram;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        RequirementDiagramEditor requirementDiagramEditor = projectAccessor.getDiagramEditorFactory().getRequirementDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/RequirementDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new RequirementDiagramEditorTool(
            projectAccessor,
            transactionManager,
            requirementDiagramEditor,
            astahProToolSupport);

        // createRequirementDiagram() method
        createRequirementDiagram = TestSupport.getAccessibleMethod(
            RequirementDiagramEditorTool.class,
            "createRequirementDiagram",
            McpSyncServerExchange.class,
            NewDiagramInPackageDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createRequirementDiagram_ok() throws Exception {
        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewDiagramInPackageDTO inputDTO = new NewDiagramInPackageDTO(
            astahPackage.getId(),
            "TestRequirementDiagram");

        // ----------------------------------------
        // Call createRequirementDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createRequirementDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
