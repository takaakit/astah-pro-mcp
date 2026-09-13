package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class CompositeStructureDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private CompositeStructureDiagramEditorTool tool;
    private Method createCompositeStructureDiagram;
    private Method createStructuredClassPresentation;
    private Method createPartPresentation;
    private Method createPortPresentation;
    private Method createProvidedInterfacePresentation;
    private Method createRequiredInterfacePresentation;
    private Method showInterfacePresentationsOfPort;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        CompositeStructureDiagramEditor compositeStructureDiagramEditor = projectAccessor.getDiagramEditorFactory().getCompositeStructureDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/CompositeStructureDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createSmallImageContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new CompositeStructureDiagramEditorTool(
            projectAccessor,
            transactionSupport,
            compositeStructureDiagramEditor,
            astahProToolSupport,
            imageCaptureSupport);

        // createCompositeStructureDiagram() method
        createCompositeStructureDiagram = TestSupport.getAccessibleMethod(
            CompositeStructureDiagramEditorTool.class,
            "createCompositeStructureDiagram",
            NewDiagramInPackageDTO.class);

        // createStructuredClassPresentation() method
        createStructuredClassPresentation = TestSupport.getAccessibleMethod(
            CompositeStructureDiagramEditorTool.class,
            "createStructuredClassPresentation",
            NewStructuredClassPresentationDTO.class);

        // createPartPresentation() method
        createPartPresentation = TestSupport.getAccessibleMethod(
            CompositeStructureDiagramEditorTool.class,
            "createPartPresentation",
            NewPartPresentationDTO.class);

        // createPortPresentation() method
        createPortPresentation = TestSupport.getAccessibleMethod(
            CompositeStructureDiagramEditorTool.class,
            "createPortPresentation",
            NewPortPresentationDTO.class);

        // createProvidedInterfacePresentation() method
        createProvidedInterfacePresentation = TestSupport.getAccessibleMethod(
            CompositeStructureDiagramEditorTool.class,
            "createProvidedInterfacePresentation",
            NewProvidedInterfacePresentationDTO.class);

        // createRequiredInterfacePresentation() method
        createRequiredInterfacePresentation = TestSupport.getAccessibleMethod(
            CompositeStructureDiagramEditorTool.class,
            "createRequiredInterfacePresentation",
            NewRequiredInterfacePresentationDTO.class);

        // showInterfacePresentationsOfPort() method
        showInterfacePresentationsOfPort = TestSupport.getAccessibleMethod(
            CompositeStructureDiagramEditorTool.class,
            "showInterfacePresentationsOfPort",
            ShowInterfacePresentationsOfPortDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createCompositeStructureDiagram_ok() throws Exception {
        // Get root package
        IPackage rootPackage = projectAccessor.getProject();

        // Check that the diagram does not exist
        assertNull(TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "TestCompositeStructureDiagram"));

        // Create input DTO
        NewDiagramInPackageDTO inputDTO = new NewDiagramInPackageDTO(
            rootPackage.getId(),
            "TestCompositeStructureDiagram");

        // ----------------------------------------
        // Call createCompositeStructureDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createCompositeStructureDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestCompositeStructureDiagram", outputDTO.namedElement().name());

        // Check that the diagram exists
        assertNotNull(TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "TestCompositeStructureDiagram"));
    }

    @Test
    void createStructuredClassPresentation_ok() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class1");

        // Create input DTO
        NewStructuredClassPresentationDTO inputDTO = new NewStructuredClassPresentationDTO(
            compositeStructureDiagram.getId(),
            clazz.getId(),
            800,
            600);

        // ----------------------------------------
        // Call createStructuredClassPresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createStructuredClassPresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(PresentationDTO.Type.STRUCTURED_CLASS.matches(outputDTO.presentation().type()));
        assertEquals(clazz.getId(), outputDTO.presentation().correspondingModelElement().id());
    }

    @Test
    void createPartPresentation_ok() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get elements
        IClass ownerClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class0");
        IClass typeClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class1");
        IAttribute attribute = new TransactionSupport(projectAccessor.getTransactionManager()).call(() ->
            projectAccessor.getModelEditorFactory().getBasicModelEditor().createAttribute(ownerClass, "Part4", typeClass));

        // Get structured class
        INodePresentation structuredClass = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "StructuredClass",
            "Class0");

        // Create input DTO. The point is an absolute coordinate on the diagram, inside the parent.
        NewPartPresentationDTO inputDTO = new NewPartPresentationDTO(
            compositeStructureDiagram.getId(),
            attribute.getId(),
            structuredClass.getID(),
            200,
            130);

        // ----------------------------------------
        // Call createPartPresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createPartPresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(PresentationDTO.Type.PART.matches(outputDTO.presentation().type()));
        assertEquals(attribute.getId(), outputDTO.presentation().correspondingModelElement().id());
        assertEquals(compositeStructureDiagram.getId(), outputDTO.presentation().renderedInDiagram().id());
    }

    @Test
    void createPartPresentation_ng_1() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class3");
        IPort port = portOwner.getPorts()[0];

        // Get structured class (parent node presentation)
        INodePresentation structuredClass = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "StructuredClass",
            "Class0");

        // Create input DTO
        NewPartPresentationDTO inputDTO = new NewPartPresentationDTO(
            compositeStructureDiagram.getId(),
            port.getId(),
            structuredClass.getID(),
            200,
            130);

        // ----------------------------------------
        // Call createPartPresentation()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createPartPresentation,
                tool,
                inputDTO,
                NodePresentationDTO.class);
        });
    }

    @Test
    void createPartPresentation_ng_2() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part0");

        // Get part
        INodePresentation part = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Part",
            "Part0");

        // Create input DTO
        NewPartPresentationDTO inputDTO = new NewPartPresentationDTO(
            compositeStructureDiagram.getId(),
            attribute.getId(),
            part.getID(),
            80,
            235);

        // ----------------------------------------
        // Call createPartPresentation()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createPartPresentation,
                tool,
                inputDTO,
                NodePresentationDTO.class);
        });
    }

    @Test
    void createPartPresentation_ng_3() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part0");

        // Get structured class
        INodePresentation structuredClass = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "StructuredClass",
            "Class3");

        // Create input DTO
        NewPartPresentationDTO inputDTO = new NewPartPresentationDTO(
            compositeStructureDiagram.getId(),
            attribute.getId(),
            structuredClass.getID(),
            570,
            330);

        // ----------------------------------------
        // Call createPartPresentation()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createPartPresentation,
                tool,
                inputDTO,
                NodePresentationDTO.class);
        });
    }

    @Test
    void createPartPresentation_ng_4() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part0");

        // Get structured class
        INodePresentation structuredClass = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "StructuredClass",
            "Class0");

        // Create input DTO
        NewPartPresentationDTO inputDTO = new NewPartPresentationDTO(
            compositeStructureDiagram.getId(),
            attribute.getId(),
            structuredClass.getID(),
            200,
            130);

        // ----------------------------------------
        // Call createPartPresentation() for an attribute whose part presentation is already drawn inside the same parent
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createPartPresentation,
                tool,
                inputDTO,
                NodePresentationDTO.class);
        });

        // Check that only one part presentation of the attribute is drawn inside the parent
        int partCount = 0;
        for (IPresentation presentation : attribute.getPresentations()) {
            if (presentation instanceof INodePresentation nodePresentation && structuredClass.equals(nodePresentation.getParent())) {
                partCount++;
            }
        }
        assertEquals(1, partCount);
    }

    @Test
    void createPortPresentation_ok() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class3");
        IPort port = portOwner.getPorts()[0];

        // Get structured class
        INodePresentation structuredClass = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "StructuredClass",
            "Class3");

        // Create input DTO
        NewPortPresentationDTO inputDTO = new NewPortPresentationDTO(
            compositeStructureDiagram.getId(),
            structuredClass.getID(),
            port.getId(),
            694,
            348);

        // ----------------------------------------
        // Call createPortPresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createPortPresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(PresentationDTO.Type.PORT.matches(outputDTO.presentation().type()));
        assertEquals(port.getId(), outputDTO.presentation().correspondingModelElement().id());
    }

    @Test
    void createProvidedInterfacePresentation_ok() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class4");
        IPort port = portOwner.getPorts()[0];
        INodePresentation portPresentation = (INodePresentation) port.getPresentations()[0];

        // Get interface
        IClass targetInterface = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Interface0");

        // Create input DTO
        NewProvidedInterfacePresentationDTO inputDTO = new NewProvidedInterfacePresentationDTO(
            compositeStructureDiagram.getId(),
            portPresentation.getID(),
            targetInterface.getId(),
            300,
            430);

        // ----------------------------------------
        // Call createProvidedInterfacePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createProvidedInterfacePresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(targetInterface.getId(), outputDTO.presentation().correspondingModelElement().id());

        // Check that the interface became a provided interface of the port
        assertEquals(1, port.getClientRealizations().length);
        assertEquals(targetInterface.getId(), port.getClientRealizations()[0].getSupplier().getId());
    }

    @Test
    void createRequiredInterfacePresentation_ok() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class3");
        IPort port = portOwner.getPorts()[0];
        INodePresentation portPresentation = (INodePresentation) port.getPresentations()[0];

        // Get interface
        IClass targetInterface = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Interface1");

        // Create input DTO
        NewRequiredInterfacePresentationDTO inputDTO = new NewRequiredInterfacePresentationDTO(
            compositeStructureDiagram.getId(),
            portPresentation.getID(),
            targetInterface.getId(),
            200,
            430);

        // ----------------------------------------
        // Call createRequiredInterfacePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createRequiredInterfacePresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(targetInterface.getId(), outputDTO.presentation().correspondingModelElement().id());

        // Check that the interface became a required interface of the port
        assertEquals(1, port.getClientUsages().length);
        assertEquals(targetInterface.getId(), port.getClientUsages()[0].getSupplier().getId());
    }

    @Test
    void showInterfacePresentationsOfPort_ok() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class3");
        IPort port = portOwner.getPorts()[0];

        // Get structured class
        INodePresentation structuredClass = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "StructuredClass",
            "Class3");

        // Draw the port on the structured class
        NodePresentationDTO portPresentation = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createPortPresentation,
            tool,
            new NewPortPresentationDTO(
                compositeStructureDiagram.getId(),
                structuredClass.getID(),
                port.getId(),
                694,
                348),
            NodePresentationDTO.class);

        // Create input DTO
        ShowInterfacePresentationsOfPortDTO inputDTO = new ShowInterfacePresentationsOfPortDTO(
            compositeStructureDiagram.getId(),
            portPresentation.presentation().id(),
            740,
            400);

        // ----------------------------------------
        // Call showInterfacePresentationsOfPort()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            showInterfacePresentationsOfPort,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(2, outputDTO.value().size());
    }

    @Test
    void showInterfacePresentationsOfPort_ng() throws Exception {
        // Get composite structure diagram
        ICompositeStructureDiagram compositeStructureDiagram = (ICompositeStructureDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ICompositeStructureDiagram.class,
            "Composite Structure Diagram0");

        // Get structured class
        INodePresentation structuredClass = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "StructuredClass",
            "Class0");

        // Create input DTO
        ShowInterfacePresentationsOfPortDTO inputDTO = new ShowInterfacePresentationsOfPortDTO(
            compositeStructureDiagram.getId(),
            structuredClass.getID(),
            200,
            450);

        // ----------------------------------------
        // Call showInterfacePresentationsOfPort()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                showInterfacePresentationsOfPort,
                tool,
                inputDTO,
                PresentationListDTO.class);
        });
    }
}
