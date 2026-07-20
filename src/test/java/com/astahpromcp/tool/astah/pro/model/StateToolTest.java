package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithDoActivityDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithEntryDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithExitDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithInternalTransitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class StateToolTest {

    private ProjectAccessor projectAccessor;
    private StateTool tool;
    private Method getInfo;
    private Method addInternalTransition;
    private Method deleteAllInternalTransitions;
    private Method setEntry;
    private Method setDoActivity;
    private Method setExit;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/StateToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new StateTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // Methods
        getInfo = TestSupport.getAccessibleMethod(
            StateTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        addInternalTransition = TestSupport.getAccessibleMethod(
            StateTool.class,
            "addInternalTransition",
            McpSyncServerExchange.class,
            StateWithInternalTransitionDTO.class);

        deleteAllInternalTransitions = TestSupport.getAccessibleMethod(
            StateTool.class,
            "deleteAllInternalTransitions",
            McpSyncServerExchange.class,
            IdDTO.class);

        setEntry = TestSupport.getAccessibleMethod(
            StateTool.class,
            "setEntry",
            McpSyncServerExchange.class,
            StateWithEntryDTO.class);

        setDoActivity = TestSupport.getAccessibleMethod(
            StateTool.class,
            "setDoActivity",
            McpSyncServerExchange.class,
            StateWithDoActivityDTO.class);

        setExit = TestSupport.getAccessibleMethod(
            StateTool.class,
            "setExit",
            McpSyncServerExchange.class,
            StateWithExitDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(state.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        StateDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            StateDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void addInternalTransition_ok() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");
        
        // Create input DTO
        StateWithInternalTransitionDTO inputDTO = new StateWithInternalTransitionDTO(
            state.getId(),
            "testEvent",
            "testGuard",
            "testAction");

        // ----------------------------------------
        // Call addInternalTransition()
        // ----------------------------------------
        StateDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addInternalTransition,
            tool,
            inputDTO,
            StateDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void deleteAllInternalTransitions_ok() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(state.getId());

        // ----------------------------------------
        // Call deleteAllInternalTransitions()
        // ----------------------------------------
        StateDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            deleteAllInternalTransitions,
            tool,
            inputDTO,
            StateDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setEntry_ok() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");
        
        // Create input DTO
        StateWithEntryDTO inputDTO = new StateWithEntryDTO(
            state.getId(),
            "testEntry");

        // ----------------------------------------
        // Call setEntry()
        // ----------------------------------------
        StateDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setEntry,
            tool,
            inputDTO,
            StateDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setDoActivity_ok() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");
        
        // Create input DTO
        StateWithDoActivityDTO inputDTO = new StateWithDoActivityDTO(
            state.getId(),
            "testDoActivity");

        // ----------------------------------------
        // Call setDoActivity()
        // ----------------------------------------
        StateDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setDoActivity,
            tool,
            inputDTO,
            StateDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setExit_ok() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");
        
        // Create input DTO
        StateWithExitDTO inputDTO = new StateWithExitDTO(
            state.getId(),
            "testExit");

        // ----------------------------------------
        // Call setExit()
        // ----------------------------------------
        StateDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setExit,
            tool,
            inputDTO,
            StateDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
