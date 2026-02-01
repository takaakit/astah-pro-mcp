package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkPresentationOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNodePresentationOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewSubtypeRelationshipGroupOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ERDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IERDiagram;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.model.IERPackage;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ERDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private ERDiagramEditorTool tool;
    private Method createERDiagram;
    private Method createNodePresentation;
    private Method createLinkPresentation;
    private Method createSubtypeRelationshipGroup;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        ERDiagramEditor erDiagramEditor = projectAccessor.getDiagramEditorFactory().getERDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/ERDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERDiagramEditorTool(
            projectAccessor,
            transactionManager,
            erDiagramEditor,
            astahProToolSupport,
            true);

        // createERDiagram() method
        createERDiagram = TestSupport.getAccessibleMethod(
            ERDiagramEditorTool.class,
            "createERDiagram",
            McpSyncServerExchange.class,
            NewERDiagramDTO.class);

        // createNodePresentation() method
        createNodePresentation = TestSupport.getAccessibleMethod(
            ERDiagramEditorTool.class,
            "createNodePresentation",
            McpSyncServerExchange.class,
            NewNodePresentationOnERDiagramDTO.class);

        // createLinkPresentation() method
        createLinkPresentation = TestSupport.getAccessibleMethod(
            ERDiagramEditorTool.class,
            "createLinkPresentation",
            McpSyncServerExchange.class,
            NewLinkPresentationOnERDiagramDTO.class);

        // createSubtypeRelationshipGroup() method
        createSubtypeRelationshipGroup = TestSupport.getAccessibleMethod(
            ERDiagramEditorTool.class,
            "createSubtypeRelationshipGroup",
            McpSyncServerExchange.class,
            NewSubtypeRelationshipGroupOnERDiagramDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createERDiagram_ok() throws Exception {
        // Get ER package
        IERPackage erPackage = (IERPackage) TestSupport.instance().getNamedElement(
            IERPackage.class,
            "package0");

        // Create input DTO
        NewERDiagramDTO inputDTO = new NewERDiagramDTO(
            erPackage.getId(),
            "TestERDiagram");

        // ----------------------------------------
        // Call createERDiagram()
        // ----------------------------------------
        ERDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERDiagram,
            tool,
            inputDTO,
            ERDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestERDiagram", outputDTO.diagram().namedElement().name());
    }

    @Test
    void createNodePresentation_ok() throws Exception {
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");

        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        NewNodePresentationOnERDiagramDTO inputDTO = new NewNodePresentationOnERDiagramDTO(
            erDiagram.getId(),
            erEntity.getId(),
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
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");

        // Get relationship
        IERRelationship relationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Get source node presentation
        IPresentation sourceNodePresentation = TestSupport.instance().getPresentation(
            "EREntity",
            "Entity0");

        // Get target node presentation
        IPresentation targetNodePresentation = TestSupport.instance().getPresentation(
            "EREntity",
            "Entity1");

        // Create input DTO
        NewLinkPresentationOnERDiagramDTO inputDTO = new NewLinkPresentationOnERDiagramDTO(
            erDiagram.getId(),
            relationship.getId(),
            sourceNodePresentation.getID(),
            targetNodePresentation.getID());

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

    @Test
    void createSubtypeRelationshipGroup_ok() throws Exception {
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");

        // Get subtype relationship link presentation
        ILinkPresentation subtypeRelationshipPresentation0 = (ILinkPresentation) TestSupport.instance().getPresentation(
            "Subtype",
            "Subtype0");

        ILinkPresentation subtypeRelationshipPresentation1 = (ILinkPresentation) TestSupport.instance().getPresentation(
            "Subtype",
            "Subtype1");
        
        List<String> subtypeRelationshipIds = new ArrayList<>();
        subtypeRelationshipIds.add(subtypeRelationshipPresentation0.getID());
        subtypeRelationshipIds.add(subtypeRelationshipPresentation1.getID());
    
        // Create input DTO
        NewSubtypeRelationshipGroupOnERDiagramDTO inputDTO = new NewSubtypeRelationshipGroupOnERDiagramDTO(
            erDiagram.getId(),
            subtypeRelationshipIds,
            "vertical");

        // ----------------------------------------
        // Call createSubtypeRelationshipGroup()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createSubtypeRelationshipGroup,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
