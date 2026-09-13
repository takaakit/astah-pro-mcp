package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.CompositeStructureModelEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class CompositeStructureModelEditorToolTest {

    private ProjectAccessor projectAccessor;
    private CompositeStructureModelEditorTool tool;
    private Method createConnector;
    private Method createRealization;
    private Method createUsage;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        CompositeStructureModelEditor compositeStructureModelEditor = projectAccessor.getModelEditorFactory().getCompositeStructureModelEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/CompositeStructureModelEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new CompositeStructureModelEditorTool(
            compositeStructureModelEditor,
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // createConnector() method
        createConnector = TestSupport.getAccessibleMethod(
            CompositeStructureModelEditorTool.class,
            "createConnector",
            NewConnectorBetweenPartsAndPortsDTO.class);

        // createRealization() method
        createRealization = TestSupport.getAccessibleMethod(
            CompositeStructureModelEditorTool.class,
            "createRealization",
            NewProvidedInterfaceOfPortDTO.class);

        // createUsage() method
        createUsage = TestSupport.getAccessibleMethod(
            CompositeStructureModelEditorTool.class,
            "createUsage",
            NewRequiredInterfaceOfPortDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createConnector_ok_1() throws Exception {
        // Get parts
        IAttribute sourcePart = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part2");
        IAttribute targetPart = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part3");

        // Get ports
        IClass sourcePartType = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class3");
        IClass targetPartType = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class4");
        IPort sourcePort = sourcePartType.getPorts()[0];
        IPort targetPort = targetPartType.getPorts()[0];

        // Create input DTO
        NewConnectorBetweenPartsAndPortsDTO inputDTO = new NewConnectorBetweenPartsAndPortsDTO(
            sourcePart.getId(),
            sourcePort.getId(),
            targetPart.getId(),
            targetPort.getId());

        // ----------------------------------------
        // Call createConnector()
        // ----------------------------------------
        ConnectorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createConnector,
            tool,
            inputDTO,
            ConnectorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(sourcePort.getId(), outputDTO.ports().get(0).id());
        assertEquals(targetPort.getId(), outputDTO.ports().get(1).id());
        assertEquals(sourcePart.getId(), outputDTO.partsWithPort().get(0).id());
        assertEquals(targetPart.getId(), outputDTO.partsWithPort().get(1).id());
    }

    @Test
    void createConnector_ok_2() throws Exception {
        // Get parts
        IAttribute sourcePart = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part0");
        IAttribute targetPart = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part1");

        // Create input DTO
        NewConnectorBetweenPartsAndPortsDTO inputDTO = new NewConnectorBetweenPartsAndPortsDTO(
            sourcePart.getId(),
            "",
            targetPart.getId(),
            "");

        // ----------------------------------------
        // Call createConnector()
        // ----------------------------------------
        ConnectorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createConnector,
            tool,
            inputDTO,
            ConnectorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(sourcePart.getId(), outputDTO.parts().get(0).id());
        assertEquals(targetPart.getId(), outputDTO.parts().get(1).id());
        assertEquals("", outputDTO.ports().get(0).id());
        assertEquals("", outputDTO.ports().get(1).id());
    }

    @Test
    void createConnector_ng_1() throws Exception {
        // Get part
        IAttribute targetPart = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part3");

        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class3");
        IPort port = portOwner.getPorts()[0];

        // Create input DTO
        NewConnectorBetweenPartsAndPortsDTO inputDTO = new NewConnectorBetweenPartsAndPortsDTO(
            port.getId(),
            "",
            targetPart.getId(),
            "");

        // ----------------------------------------
        // Call createConnector()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createConnector,
                tool,
                inputDTO,
                ConnectorDTO.class);
        });
    }

    @Test
    void createConnector_ng_2() throws Exception {
        // Get parts
        IAttribute sourcePart = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part2");
        IAttribute targetPart = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "Part3");

        // Get port
        IClass targetPartType = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class4");
        IPort targetPort = targetPartType.getPorts()[0];

        // Create input DTO
        NewConnectorBetweenPartsAndPortsDTO inputDTO = new NewConnectorBetweenPartsAndPortsDTO(
            sourcePart.getId(),
            targetPort.getId(),
            targetPart.getId(),
            targetPort.getId());

        // ----------------------------------------
        // Call createConnector()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createConnector,
                tool,
                inputDTO,
                ConnectorDTO.class);
        });
    }

    @Test
    void createRealization_ok() throws Exception {
        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class4");
        IPort port = portOwner.getPorts()[0];

        // Get interface
        IClass targetInterface = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Interface0");

        // Check the provided interfaces before creating one
        assertEquals(0, port.getClientRealizations().length);

        // Create input DTO
        NewProvidedInterfaceOfPortDTO inputDTO = new NewProvidedInterfaceOfPortDTO(
            port.getId(),
            targetInterface.getId());

        // ----------------------------------------
        // Call createRealization()
        // ----------------------------------------
        RealizationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createRealization,
            tool,
            inputDTO,
            RealizationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(port.getId(), outputDTO.client().id());
        assertEquals(targetInterface.getId(), outputDTO.supplier().id());

        // Check that the interface became a provided interface of the port
        assertEquals(1, port.getClientRealizations().length);
        assertEquals(targetInterface.getId(), port.getClientRealizations()[0].getSupplier().getId());
    }

    @Test
    void createUsage_ok() throws Exception {
        // Get port
        IClass portOwner = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Class3");
        IPort port = portOwner.getPorts()[0];

        // Get interface
        IClass targetInterface = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Interface1");

        // Check the required interfaces before creating one
        assertEquals(0, port.getClientUsages().length);

        // Create input DTO
        NewRequiredInterfaceOfPortDTO inputDTO = new NewRequiredInterfaceOfPortDTO(
            port.getId(),
            targetInterface.getId());

        // ----------------------------------------
        // Call createUsage()
        // ----------------------------------------
        UsageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createUsage,
            tool,
            inputDTO,
            UsageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(port.getId(), outputDTO.client().id());
        assertEquals(targetInterface.getId(), outputDTO.supplier().id());

        // Check that the interface became a required interface of the port
        assertEquals(1, port.getClientUsages().length);
        assertEquals(targetInterface.getId(), port.getClientUsages()[0].getSupplier().getId());
    }
}
