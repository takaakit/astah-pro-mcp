package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.NavigabilityKind;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewAssociationClassPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewInstanceWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkSourceAndTargetDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IAssociationClass;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/ClassDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createThumbnailContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new ClassDiagramEditorTool(
            projectAccessor,
            transactionSupport,
            classDiagramEditor,
            astahProToolSupport,
            imageCaptureSupport);

        // createClassDiagram() method
        createClassDiagram = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createClassDiagram",
            NewDiagramInPackageDTO.class);

        // createAssociationClassPresentation() method
        createAssociationClassPresentation = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createAssociationClassPresentation",
            NewAssociationClassPresentationDTO.class);

        // createInstanceSpecification() method
        createInstanceSpecification = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createInstanceSpecification",
            NewInstanceWithPointDTO.class);

        // createInstanceSpecificationLink() method
        createInstanceSpecificationLink = TestSupport.getAccessibleMethod(
            ClassDiagramEditorTool.class,
            "createInstanceSpecificationLink",
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
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");

        // Create input DTO
        NewDiagramInPackageDTO inputDTO = new NewDiagramInPackageDTO(
            astahPackage.getId(),
            "TestClassDiagram");

        // Check that the diagram does not exist
        assertNull(TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "TestClassDiagram"));

        // ----------------------------------------
        // Call createClassDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createClassDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestClassDiagram", outputDTO.namedElement().name());

        // Check that the diagram exists
        assertNotNull(TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "TestClassDiagram"));
    }

    @Test
    void createAssociationClassPresentation_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association class
        IAssociationClass associationClass = (IAssociationClass) TestSupport.instance().getNamedElementByClassAndName(
            IAssociationClass.class,
            "AssociationClass0");

        // Get source node presentation (class presentation)
        INodePresentation sourceNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Get target node presentation (class presentation)
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createAssociationClassPresentation,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createAssociationClassPresentation_ok_reversed() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association class
        IAssociationClass associationClass = (IAssociationClass) TestSupport.instance().getNamedElementByClassAndName(
            IAssociationClass.class,
            "AssociationClass0");

        // Get class presentations
        INodePresentation classPresentationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation classPresentationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // ----------------------------------------
        // Call createAssociationClassPresentation()
        // ----------------------------------------
        // Either order draws the same, so the reversed pair is accepted too
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createAssociationClassPresentation,
            tool,
            new NewAssociationClassPresentationDTO(classDiagram.getId(), associationClass.getId(), classPresentationBar.getID(), classPresentationFoo.getID()),
            PresentationListDTO.class);

        // Check output DTO
        assertEquals(2, outputDTO.value().size());

        // Check that the line joins the two classes at the ends
        ILinkPresentation createdLinkPresentation = null;
        for (IPresentation presentation : associationClass.getPresentations()) {
            if (presentation instanceof ILinkPresentation linkPresentation) {
                createdLinkPresentation = linkPresentation;
            }
        }
        assertNotNull(createdLinkPresentation);
        assertEquals(classDiagram, createdLinkPresentation.getDiagram());
        assertEquals(
            Set.of(classPresentationFoo, classPresentationBar),
            Set.of(createdLinkPresentation.getSource(), createdLinkPresentation.getTarget()));
    }

    @Test
    void createAssociationClassPresentation_ng_notOnTargetDiagram() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association class
        IAssociationClass associationClass = (IAssociationClass) TestSupport.instance().getNamedElementByClassAndName(
            IAssociationClass.class,
            "AssociationClass0");

        // Get class presentations
        INodePresentation classPresentationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation classPresentationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // Draw the same two classes on another class diagram
        INodePresentation[] elsewhere = new TransactionSupport(projectAccessor.getTransactionManager()).call(() -> {
            ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
            classDiagramEditor.setDiagram(classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Another Class Diagram"));
            return new INodePresentation[] {
                classDiagramEditor.createNodePresentation(classPresentationFoo.getModel(), new Point2D.Double(0, 0)),
                classDiagramEditor.createNodePresentation(classPresentationBar.getModel(), new Point2D.Double(300, 0))
            };
        });
        int beforeCount = classDiagram.getPresentations().length;

        // The source, the target, and both ends are on the other class diagram
        String[][] ends = {
            { elsewhere[0].getID(), classPresentationBar.getID(), "source" },
            { classPresentationFoo.getID(), elsewhere[1].getID(), "target" },
            { elsewhere[0].getID(), elsewhere[1].getID(), "source" }
        };

        for (String[] end : ends) {
            // ----------------------------------------
            // Call createAssociationClassPresentation()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    createAssociationClassPresentation,
                    tool,
                    new NewAssociationClassPresentationDTO(classDiagram.getId(), associationClass.getId(), end[0], end[1]),
                    PresentationListDTO.class);
            });

            // Check exception
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("The " + end[2] + " node presentation is not on the target class diagram"));
        }

        // Check that the association class is drawn nowhere
        assertEquals(beforeCount, classDiagram.getPresentations().length);
        assertEquals(0, associationClass.getPresentations().length);
    }

    @Test
    void createAssociationClassPresentation_ng_notEndClass() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association class
        IAssociationClass associationClass = (IAssociationClass) TestSupport.instance().getNamedElementByClassAndName(
            IAssociationClass.class,
            "AssociationClass0");

        // Get class presentations
        INodePresentation classPresentationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation classPresentationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // Get instance specification of Bar
        INodePresentation instanceSpecificationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "InstanceSpecification",
            "bar");

        // Draw a class that is at neither end on the same class diagram
        INodePresentation classPresentationBaz = new TransactionSupport(projectAccessor.getTransactionManager()).call(() -> {
            IClass clazz = projectAccessor.getModelEditorFactory().getBasicModelEditor().createClass(projectAccessor.getProject(), "Baz");
            ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
            classDiagramEditor.setDiagram(classDiagram);
            return classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(600, 600));
        });
        int beforeCount = classDiagram.getPresentations().length;

        // A class at neither end, the same end twice, and an instance specification of an end class
        String[][] ends = {
            { classPresentationFoo.getID(), classPresentationBaz.getID() },
            { classPresentationBaz.getID(), classPresentationBar.getID() },
            { classPresentationFoo.getID(), classPresentationFoo.getID() },
            { classPresentationFoo.getID(), instanceSpecificationBar.getID() }
        };

        for (String[] end : ends) {
            // ----------------------------------------
            // Call createAssociationClassPresentation()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    createAssociationClassPresentation,
                    tool,
                    new NewAssociationClassPresentationDTO(classDiagram.getId(), associationClass.getId(), end[0], end[1]),
                    PresentationListDTO.class);
            });

            // Check exception
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("must draw the classes at the two ends of association class 'AssociationClass0'"));
        }

        // Check that the association class is drawn nowhere
        assertEquals(beforeCount, classDiagram.getPresentations().length);
        assertEquals(0, associationClass.getPresentations().length);
    }

    @Test
    void createAssociationClassPresentation_ng_alreadyDrawn() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association class
        IAssociationClass associationClass = (IAssociationClass) TestSupport.instance().getNamedElementByClassAndName(
            IAssociationClass.class,
            "AssociationClass0");

        // Get class presentations
        INodePresentation classPresentationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation classPresentationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // Draw the association class first
        TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createAssociationClassPresentation,
            tool,
            new NewAssociationClassPresentationDTO(classDiagram.getId(), associationClass.getId(), classPresentationFoo.getID(), classPresentationBar.getID()),
            PresentationListDTO.class);
        int beforeCount = classDiagram.getPresentations().length;

        // Either order would draw it again
        String[][] ends = {
            { classPresentationFoo.getID(), classPresentationBar.getID() },
            { classPresentationBar.getID(), classPresentationFoo.getID() }
        };

        for (String[] end : ends) {
            // ----------------------------------------
            // Call createAssociationClassPresentation()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    createAssociationClassPresentation,
                    tool,
                    new NewAssociationClassPresentationDTO(classDiagram.getId(), associationClass.getId(), end[0], end[1]),
                    PresentationListDTO.class);
            });

            // Check exception
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("The association class is already drawn on the target class diagram (presentation ID: "));
        }

        // Check that the association class is not drawn twice
        assertEquals(beforeCount, classDiagram.getPresentations().length);
    }

    @Test
    void createAssociationClassPresentation_ng_astahFailure() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get association class
        IAssociationClass associationClass = (IAssociationClass) TestSupport.instance().getNamedElementByClassAndName(
            IAssociationClass.class,
            "AssociationClass0");

        // Get class presentations
        INodePresentation classPresentationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation classPresentationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // Astah fails this way on some class diagrams that were already in the project file, which the test model has none of
        ClassDiagramEditor failingClassDiagramEditor = mock(ClassDiagramEditor.class);
        when(failingClassDiagramEditor.createAssociationClassPresentation(any(), any(), any()))
            .thenThrow(new NullPointerException("Cannot invoke \"Object.toString()\" because the return value of \"java.util.Map.get(Object)\" is null"));
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        ClassDiagramEditorTool failingTool = new ClassDiagramEditorTool(
            projectAccessor,
            new TransactionSupport(projectAccessor.getTransactionManager()),
            failingClassDiagramEditor,
            new AstahProToolSupport(projectAccessor),
            imageCaptureSupport);

        // ----------------------------------------
        // Call createAssociationClassPresentation()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createAssociationClassPresentation,
                failingTool,
                new NewAssociationClassPresentationDTO(classDiagram.getId(), associationClass.getId(), classPresentationFoo.getID(), classPresentationBar.getID()),
                PresentationListDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Astah could not draw association class 'AssociationClass0' on class diagram 'Class Diagram0'"));
        assertTrue(exception.getCause().getMessage().contains("calling this tool again, even in a separate run, fails the same way"));
        assertTrue(exception.getCause().getMessage().contains("create_class_dgm"));
        assertFalse(projectAccessor.getTransactionManager().isInTransaction());
    }

    @Test
    void createInstanceSpecification_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");

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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createInstanceSpecification,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Get instance specification
        IInstanceSpecification instanceSpecification = (IInstanceSpecification) TestSupport.instance().getNamedElementByClassAndName(
            IInstanceSpecification.class,
            "TestInstance");
        assertNotNull(instanceSpecification);
        assertEquals("Bar", instanceSpecification.getClassifier().getName());
    }

    @Test
    void createInstanceSpecification_ok_primitiveTypeAsClassifier() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get primitive type
        IClass primitiveType = null;
        for (IClass candidate : new AstahProToolSupport(projectAccessor).getPrimitiveTypes()) {
            if ("int".equals(candidate.getName())) {
                primitiveType = candidate;
            }
        }
        assertNotNull(primitiveType);

        // Create input DTO
        NewInstanceWithPointDTO inputDTO = new NewInstanceWithPointDTO(
            classDiagram.getId(),
            primitiveType.getId(),
            "TestPrimitiveInstance",
            300,
            200);

        // ----------------------------------------
        // Call createInstanceSpecification()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createInstanceSpecification,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Get instance specification
        IInstanceSpecification instanceSpecification = (IInstanceSpecification) TestSupport.instance().getNamedElementByClassAndName(
            IInstanceSpecification.class,
            "TestPrimitiveInstance");
        assertNotNull(instanceSpecification);
        assertEquals("int", instanceSpecification.getClassifier().getName());
    }

    @Test
    void createInstanceSpecificationLink_ok() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get instance specification
        INodePresentation instanceSpecificationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "InstanceSpecification",
            "foo");

        // Get instance specification
        INodePresentation instanceSpecificationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createInstanceSpecificationLink,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Get created instance specification link
        ILinkPresentation createdInstanceSpecificationLinkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationById(
            outputDTO.presentation().id());
        ILink createdInstanceSpecificationLink = (ILink) TestSupport.instance().getNamedElementById(
            ((INamedElement) createdInstanceSpecificationLinkPresentation.getModel()).getId());

        // Get created source link end
        ILinkEnd createdSourceLinkEnd = createdInstanceSpecificationLink.getMemberEnds()[0];
        ILinkEnd createdTargetLinkEnd = createdInstanceSpecificationLink.getMemberEnds()[1];

        // Check with the created instance specification link
        assertEquals(NavigabilityKind.UNSPECIFIED.astahValue, createdSourceLinkEnd.getNavigability());
        assertEquals(NavigabilityKind.UNSPECIFIED.astahValue, createdTargetLinkEnd.getNavigability());
    }

    @Test
    void createInstanceSpecificationLink_ng_notOnTargetDiagram() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get instance specification
        INodePresentation instanceSpecificationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "InstanceSpecification",
            "foo");

        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");

        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");

        // Create two instance specifications on another class diagram
        DiagramDTO anotherClassDiagramDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createClassDiagram,
            tool,
            new NewDiagramInPackageDTO(astahPackage.getId(), "Another Class Diagram"),
            DiagramDTO.class);
        NodePresentationDTO instanceSpecificationBazDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createInstanceSpecification,
            tool,
            new NewInstanceWithPointDTO(anotherClassDiagramDTO.namedElement().element().id(), clazz.getId(), "baz", 100, 200),
            NodePresentationDTO.class);
        NodePresentationDTO instanceSpecificationQuxDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createInstanceSpecification,
            tool,
            new NewInstanceWithPointDTO(anotherClassDiagramDTO.namedElement().element().id(), clazz.getId(), "qux", 300, 200),
            NodePresentationDTO.class);
        int beforeCount = classDiagram.getPresentations().length;

        // The source, the target, and both ends are on the other class diagram
        String[][] ends = {
            { instanceSpecificationBazDTO.presentation().id(), instanceSpecificationFoo.getID(), "source" },
            { instanceSpecificationFoo.getID(), instanceSpecificationBazDTO.presentation().id(), "target" },
            { instanceSpecificationBazDTO.presentation().id(), instanceSpecificationQuxDTO.presentation().id(), "source" }
        };

        for (String[] end : ends) {
            // ----------------------------------------
            // Call createInstanceSpecificationLink()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    createInstanceSpecificationLink,
                    tool,
                    new NewLinkSourceAndTargetDTO(classDiagram.getId(), end[0], end[1]),
                    LinkPresentationDTO.class);
            });

            // Check exception
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("The " + end[2] + " node presentation is not on the target class diagram"));
        }

        // Check that no link is drawn on the class diagram
        assertEquals(beforeCount, classDiagram.getPresentations().length);
    }

    @Test
    void createInstanceSpecificationLink_ng_notInstanceSpecification() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get instance specification
        INodePresentation instanceSpecificationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "InstanceSpecification",
            "foo");

        // Get class presentation
        INodePresentation classPresentationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        int beforeCount = classDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createInstanceSpecificationLink()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createInstanceSpecificationLink,
                tool,
                new NewLinkSourceAndTargetDTO(classDiagram.getId(), instanceSpecificationFoo.getID(), classPresentationFoo.getID()),
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The target node presentation is not an instance specification"));

        // Check that no link is drawn on the class diagram
        assertEquals(beforeCount, classDiagram.getPresentations().length);
    }

    @Test
    void createInstanceSpecificationLink_ng_alreadyLinked() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Get instance specification
        INodePresentation instanceSpecificationFoo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "InstanceSpecification",
            "foo");

        // Get instance specification
        INodePresentation instanceSpecificationBar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "InstanceSpecification",
            "bar");

        // Create a link first
        LinkPresentationDTO createdLinkDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createInstanceSpecificationLink,
            tool,
            new NewLinkSourceAndTargetDTO(classDiagram.getId(), instanceSpecificationFoo.getID(), instanceSpecificationBar.getID()),
            LinkPresentationDTO.class);
        int beforeCount = classDiagram.getPresentations().length;

        // A link has no direction, so the reversed pair is the same link
        String[][] ends = {
            { instanceSpecificationFoo.getID(), instanceSpecificationBar.getID() },
            { instanceSpecificationBar.getID(), instanceSpecificationFoo.getID() }
        };

        for (String[] end : ends) {
            // ----------------------------------------
            // Call createInstanceSpecificationLink()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    createInstanceSpecificationLink,
                    tool,
                    new NewLinkSourceAndTargetDTO(classDiagram.getId(), end[0], end[1]),
                    LinkPresentationDTO.class);
            });

            // Check exception
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("A link (ID: " + createdLinkDTO.presentation().id() + ") already exists"));
        }

        // Check that the link is not duplicated
        assertEquals(beforeCount, classDiagram.getPresentations().length);
    }
}
