package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewAssociationClassPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewInstanceWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkSourceAndTargetDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAssociationClass;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ClassDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private ClassDiagramEditorTool tool;
    private Method createClassDiagram;
    private Method createAssociationClassPresentation;
    private Method createInstanceSpecification;
    private Method createInstanceSpecificationLink;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/ClassDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ClassDiagramEditorTool(
            projectAccessor,
            transactionManager,
            classDiagramEditor,
            astahProToolSupport,
            true);

        // createClassDiagram() method
        createClassDiagram = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createClassDiagram",
            McpSyncServerExchange.class,
            NewDiagramInPackageDTO.class);

        // createAssociationClassPresentation() method
        createAssociationClassPresentation = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createAssociationClassPresentation",
            McpSyncServerExchange.class,
            NewAssociationClassPresentationDTO.class);

        // createInstanceSpecification() method
        createInstanceSpecification = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createInstanceSpecification",
            McpSyncServerExchange.class,
            NewInstanceWithPointDTO.class);

        // createInstanceSpecificationLink() method
        createInstanceSpecificationLink = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createInstanceSpecificationLink",
            McpSyncServerExchange.class,
            NewLinkSourceAndTargetDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createClassDiagram_ok() throws Exception {
        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewDiagramInPackageDTO inputDTO = new NewDiagramInPackageDTO(
            astahPackage.getId(),
            "TestClassDiagram");
        
        // Check that the diagram does not exist
        assertNull(TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "TestClassDiagram"));

        // ----------------------------------------
        // Call createClassDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createClassDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestClassDiagram", outputDTO.namedElement().name());

        // Check that the diagram exists
        assertNotNull(TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "TestClassDiagram"));
    }

    @Test
    void createAssociationClassPresentation_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association class
        IAssociationClass associationClass = (IAssociationClass) TestSupport.instance().getNamedElement(
            IAssociationClass.class,
            "AssociationClass0");

        // Get source node presentation (class presentation)
        INodePresentation sourceNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");

        // Get target node presentation (class presentation)
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Bar");

        // Create input DTO
        NewAssociationClassPresentationDTO inputDTO = new NewAssociationClassPresentationDTO(
            classDiagram.getId(),
            associationClass.getId(),
            sourceNodePresentation.getID(),
            targetNodePresentation.getID());

        // ----------------------------------------
        // Call createAssociationClassPresentation()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createAssociationClassPresentation,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInstanceSpecification_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");
        
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewInstanceWithPointDTO inputDTO = new NewInstanceWithPointDTO(
            classDiagram.getId(),
            clazz.getId(),
            "TestInstance",
            100,
            200);

        // ----------------------------------------
        // Call createInstanceSpecification()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInstanceSpecification,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInstanceSpecificationLink_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElement(
            IClassDiagram.class,
            "Class Diagram0");

        // Get instance specification
        INodePresentation instanceSpecificationFoo = (INodePresentation) TestSupport.instance().getPresentation(
            "InstanceSpecification",
            "foo");

        // Get instance specification
        INodePresentation instanceSpecificationBar = (INodePresentation) TestSupport.instance().getPresentation(
            "InstanceSpecification",
            "bar");

        // Create input DTO
        NewLinkSourceAndTargetDTO inputDTO = new NewLinkSourceAndTargetDTO(
            classDiagram.getId(),
            instanceSpecificationFoo.getID(),
            instanceSpecificationBar.getID());

        // ----------------------------------------
        // Call createInstanceSpecificationLink()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInstanceSpecificationLink,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
