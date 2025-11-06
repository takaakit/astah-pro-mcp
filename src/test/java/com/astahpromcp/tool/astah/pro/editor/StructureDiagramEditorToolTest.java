package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StructureDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private StructureDiagramEditorTool tool;
    private Method createNodePresentation;
    private Method createLinkPresentation;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/editor/StructureDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);

        // Tool
        tool = new StructureDiagramEditorTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            diagramEditorSupport,
            true);

        // createNodePresentation() method
        createNodePresentation = TestSupport.getAccessibleMethod(
            StructureDiagramEditorTool.class,
            "createNodePresentation",
            McpSyncServerExchange.class,
            NewNodePresentationDTO.class);

        // createLinkPresentation() method
        createLinkPresentation = TestSupport.getAccessibleMethod(
            StructureDiagramEditorTool.class,
            "createLinkPresentation",
            McpSyncServerExchange.class,
            NewLinkPresentationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createNodePresentation_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");
        
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewNodePresentationDTO inputDTO = new NewNodePresentationDTO(
            classDiagram.getId(),
            clazz.getId(),
            10,
            20);

        // ----------------------------------------
        // Call createNodePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createNodePresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createLinkPresentation_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");
        
        // Get class (source node presentation)
        INodePresentation classBar = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Bar");
        
        // Get class (target node presentation)
        INodePresentation classBaz = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Baz");

        // Create input DTO
        NewLinkPresentationDTO inputDTO = new NewLinkPresentationDTO(
            classDiagram.getId(),
            association.getId(),
            classBar.getID(),
            classBaz.getID());

        // ----------------------------------------
        // Call createLinkPresentation()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createLinkPresentation,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
