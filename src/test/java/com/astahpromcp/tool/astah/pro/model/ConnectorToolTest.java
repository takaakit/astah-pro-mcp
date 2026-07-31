package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ConnectorDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IConnector;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class ConnectorToolTest {

    private ProjectAccessor projectAccessor;
    private ConnectorTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/ConnectorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ConnectorTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ConnectorTool.class,
            "getInfo",
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get connector
        IConnector connector = (IConnector) TestSupport.instance().getNamedElementByClassAndName(
            IConnector.class,
            "connector0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(connector.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ConnectorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ConnectorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(connector.getId(), outputDTO.namedElement().element().id());
    }

    @Test
    void getInfo_ok_portToPort() throws Exception {
        // Get connector
        IConnector connector = (IConnector) TestSupport.instance().getNamedElementByClassAndName(
            IConnector.class,
            "connector0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(connector.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ConnectorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ConnectorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertFalse(outputDTO.ports().get(0).id().isEmpty());
        assertFalse(outputDTO.ports().get(1).id().isEmpty());
        assertTrue(outputDTO.parts().get(0).id().isEmpty());
        assertTrue(outputDTO.parts().get(1).id().isEmpty());
        assertFalse(outputDTO.partsWithPort().get(0).id().isEmpty());
        assertFalse(outputDTO.partsWithPort().get(1).id().isEmpty());
    }

    @Test
    void getInfo_ok_portToPart() throws Exception {
        // Get connector
        IConnector connector = (IConnector) TestSupport.instance().getNamedElementByClassAndName(
            IConnector.class,
            "connector1");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(connector.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ConnectorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ConnectorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertFalse(outputDTO.ports().get(0).id().isEmpty());
        assertTrue(outputDTO.ports().get(1).id().isEmpty());
        assertTrue(outputDTO.parts().get(0).id().isEmpty());
        assertFalse(outputDTO.parts().get(1).id().isEmpty());
        assertFalse(outputDTO.partsWithPort().get(0).id().isEmpty());
        assertTrue(outputDTO.partsWithPort().get(1).id().isEmpty());
    }

    @Test
    void getInfo_ok_partToPort() throws Exception {
        // Get connector
        IConnector connector = (IConnector) TestSupport.instance().getNamedElementByClassAndName(
            IConnector.class,
            "connector2");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(connector.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ConnectorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ConnectorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.ports().get(0).id().isEmpty());
        assertFalse(outputDTO.ports().get(1).id().isEmpty());
        assertFalse(outputDTO.parts().get(0).id().isEmpty());
        assertTrue(outputDTO.parts().get(1).id().isEmpty());
        assertTrue(outputDTO.partsWithPort().get(0).id().isEmpty());
        assertFalse(outputDTO.partsWithPort().get(1).id().isEmpty());
    }

    @Test
    void getInfo_ok_partToPart() throws Exception {
        // Get connector
        IConnector connector = (IConnector) TestSupport.instance().getNamedElementByClassAndName(
            IConnector.class,
            "connector3");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(connector.getId());

        // Call getInfo()
        ConnectorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ConnectorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.ports().get(0).id().isEmpty());
        assertTrue(outputDTO.ports().get(1).id().isEmpty());
        assertFalse(outputDTO.parts().get(0).id().isEmpty());
        assertFalse(outputDTO.parts().get(1).id().isEmpty());
        assertTrue(outputDTO.partsWithPort().get(0).id().isEmpty());
        assertTrue(outputDTO.partsWithPort().get(1).id().isEmpty());
    }
}
