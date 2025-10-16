package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.TransitionWithActionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.TransitionWithEventDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.TransitionWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.TransitionDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class TransitionToolTest {

    private ProjectAccessor projectAccessor;
    private TransitionTool tool;
    private Method getInfo;
    private Method setAction;
    private Method setEvent;
    private Method setGuard;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/TransitionTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new TransitionTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            TransitionTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setAction() method
        setAction = TestSupport.getAccessibleMethod(
            TransitionTool.class,
            "setAction",
            McpSyncServerExchange.class,
            TransitionWithActionDTO.class);

        // setEvent() method
        setEvent = TestSupport.getAccessibleMethod(
            TransitionTool.class,
            "setEvent",
            McpSyncServerExchange.class,
            TransitionWithEventDTO.class);

        // setGuard() method
        setGuard = TestSupport.getAccessibleMethod(
            TransitionTool.class,
            "setGuard",
            McpSyncServerExchange.class,
            TransitionWithGuardDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get transition
        ITransition transition = (ITransition) TestSupport.instance().getNamedElement(
            ITransition.class,
            "");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(transition.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        TransitionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            TransitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setAction_ok() throws Exception {
        // Get transition
        ITransition transition = (ITransition) TestSupport.instance().getNamedElement(
            ITransition.class,
            "");
        
        // Create input DTO
        TransitionWithActionDTO inputDTO = new TransitionWithActionDTO(
            transition.getId(),
            "Test Action");

        // Check action before setting
        assertNotEquals("Test Action", transition.getAction());

        // ----------------------------------------
        // Call setAction()
        // ----------------------------------------
        TransitionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAction,
            tool,
            inputDTO,
            TransitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check action after setting
        assertEquals("Test Action", transition.getAction());
    }

    @Test
    void setEvent_ok() throws Exception {
        // Get transition
        ITransition transition = (ITransition) TestSupport.instance().getNamedElement(
            ITransition.class,
            "");
        
        // Create input DTO
        TransitionWithEventDTO inputDTO = new TransitionWithEventDTO(
            transition.getId(),
            "Test Event");

        // Check event before setting
        assertNotEquals("Test Event", transition.getEvent());

        // ----------------------------------------
        // Call setEvent()
        // ----------------------------------------
        TransitionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setEvent,
            tool,
            inputDTO,
            TransitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check event after setting
        assertEquals("Test Event", transition.getEvent());
    }

    @Test
    void setGuard_ok() throws Exception {
        // Get transition
        ITransition transition = (ITransition) TestSupport.instance().getNamedElement(
            ITransition.class,
            "");
        
        // Create input DTO
        TransitionWithGuardDTO inputDTO = new TransitionWithGuardDTO(
            transition.getId(),
            "Test Guard");

        // Check guard before setting
        assertNotEquals("Test Guard", transition.getGuard());

        // ----------------------------------------
        // Call setGuard()
        // ----------------------------------------
        TransitionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setGuard,
            tool,
            inputDTO,
            TransitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check guard after setting
        assertEquals("Test Guard", transition.getGuard());
    }
}
