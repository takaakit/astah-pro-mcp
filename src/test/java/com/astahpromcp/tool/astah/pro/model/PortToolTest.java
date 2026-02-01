package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PortWithBehaviorDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PortWithServiceDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PortDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class PortToolTest {

    private ProjectAccessor projectAccessor;
    private PortTool tool;
    private Method getInfo;
    private Method setBehavior;
    private Method setService;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/PortToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new PortTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            PortTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setBehavior() method
        setBehavior = TestSupport.getAccessibleMethod(
            PortTool.class,
            "setBehavior",
            McpSyncServerExchange.class,
            PortWithBehaviorDTO.class);

        // setService() method
        setService = TestSupport.getAccessibleMethod(
            PortTool.class,
            "setService",
            McpSyncServerExchange.class,
            PortWithServiceDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get port
        IPort port = (IPort) TestSupport.instance().getNamedElement(
            IPort.class,
            "port0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(port.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        PortDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            PortDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(port.getId(), outputDTO.attribute().namedElement().element().id());
    }

    @Test
    void setBehavior_ok() throws Exception {
        // Get port
        IPort port = (IPort) TestSupport.instance().getNamedElement(
            IPort.class,
            "port0");
        
        // Create input DTO
        PortWithBehaviorDTO inputDTO = new PortWithBehaviorDTO(
            port.getId(),
            true);

        // Check behavior before setting
        assertFalse(port.isBehavior());

        // ----------------------------------------
        // Call setBehavior()
        // ----------------------------------------
        PortDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setBehavior,
            tool,
            inputDTO,
            PortDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.isBehavior());

        // Check behavior after setting
        assertTrue(port.isBehavior());
    }

    @Test
    void setService_ok() throws Exception {
        // Get port
        IPort port = (IPort) TestSupport.instance().getNamedElement(
            IPort.class,
            "port0");
        
        // Create input DTO
        PortWithServiceDTO inputDTO = new PortWithServiceDTO(
            port.getId(),
            true);

        // Check service before setting
        assertFalse(port.isService());

        // ----------------------------------------
        // Call setService()
        // ----------------------------------------
        PortDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setService,
            tool,
            inputDTO,
            PortDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.isService());

        // Check service after setting
        assertTrue(port.isService());
    }
}
