package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.FlowWithActionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.FlowWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.FlowDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IFlow;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class FlowToolTest {

    private ProjectAccessor projectAccessor;
    private FlowTool tool;
    private Method getInfo;
    private Method setAction;
    private Method setGuard;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/FlowTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new FlowTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            FlowTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setAction() method
        setAction = TestSupport.getAccessibleMethod(
            FlowTool.class,
            "setAction",
            McpSyncServerExchange.class,
            FlowWithActionDTO.class);

        // setGuard() method
        setGuard = TestSupport.getAccessibleMethod(
            FlowTool.class,
            "setGuard",
            McpSyncServerExchange.class,
            FlowWithGuardDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get flow
        IFlow flow = (IFlow) TestSupport.instance().getNamedElement(
            IFlow.class,
            "");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(flow.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        FlowDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            FlowDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setAction_ok() throws Exception {
        // Get flow
        IFlow flow = (IFlow) TestSupport.instance().getNamedElement(
            IFlow.class,
            "");
        
        // Create input DTO
        FlowWithActionDTO inputDTO = new FlowWithActionDTO(
            flow.getId(),
            "Test Action");

        // Check action before setting
        assertNotEquals("Test Action", flow.getAction());

        // ----------------------------------------
        // Call setAction()
        // ----------------------------------------
        FlowDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAction,
            tool,
            inputDTO,
            FlowDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check action after setting
        assertEquals("Test Action", flow.getAction());
    }

    @Test
    void setGuard_ok() throws Exception {
        // Get flow
        IFlow flow = (IFlow) TestSupport.instance().getNamedElement(
            IFlow.class,
            "");
        
        // Create input DTO
        FlowWithGuardDTO inputDTO = new FlowWithGuardDTO(
            flow.getId(),
            "Test Guard");

        // Check guard before setting
        assertNotEquals("Test Guard", flow.getGuard());

        // ----------------------------------------
        // Call setGuard()
        // ----------------------------------------
        FlowDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setGuard,
            tool,
            inputDTO,
            FlowDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check guard after setting
        assertEquals("Test Guard", flow.getGuard());
    }
}
