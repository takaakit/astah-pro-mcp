package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithDoActivityDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithEntryDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithExitDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.StateWithInternalTransitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.StateMachineDiagramEditor;
import com.change_vision.jude.api.inf.model.IFinalState;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class StateToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
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
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/StateToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new StateTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            StateTool.class,
            "getInfo",
            IdDTO.class);

        // addInternalTransition() method
        addInternalTransition = TestSupport.getAccessibleMethod(
            StateTool.class,
            "addInternalTransition",
            StateWithInternalTransitionDTO.class);

        // deleteAllInternalTransitions() method
        deleteAllInternalTransitions = TestSupport.getAccessibleMethod(
            StateTool.class,
            "deleteAllInternalTransitions",
            IdDTO.class);

        // setEntry() method
        setEntry = TestSupport.getAccessibleMethod(
            StateTool.class,
            "setEntry",
            StateWithEntryDTO.class);

        // setDoActivity() method
        setDoActivity = TestSupport.getAccessibleMethod(
            StateTool.class,
            "setDoActivity",
            StateWithDoActivityDTO.class);

        // setExit() method
        setExit = TestSupport.getAccessibleMethod(
            StateTool.class,
            "setExit",
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
    void addInternalTransition_ok_nullGuardAndActionTreatedAsEmpty() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");

        int countBefore = state.getInternalTransitions().length;

        // Create input DTO
        StateWithInternalTransitionDTO inputDTO = new StateWithInternalTransitionDTO(
            state.getId(),
            " nullTestEvent ",
            null,
            null);

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

        // Check that the event is stripped and that the guard and the action are empty rather than the text "null"
        ITransition[] transitions = state.getInternalTransitions();
        assertEquals(countBefore + 1, transitions.length);
        ITransition added = transitions[transitions.length - 1];
        assertEquals("nullTestEvent", added.getEvent());
        assertEquals("", added.getGuard());
        assertEquals("", added.getAction());
    }

    @Test
    void addInternalTransition_ok_sameEventDifferentGuard() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");

        int countBefore = state.getInternalTransitions().length;

        // ----------------------------------------
        // Call addInternalTransition()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            addInternalTransition,
            tool,
            new StateWithInternalTransitionDTO(state.getId(), "guardTestEvent", "x > 0", "testAction"),
            StateDTO.class);
        StateDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addInternalTransition,
            tool,
            new StateWithInternalTransitionDTO(state.getId(), "guardTestEvent", "x <= 0", "testAction"),
            StateDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check that both internal transitions are added
        assertEquals(countBefore + 2, state.getInternalTransitions().length);
    }

    @Test
    void addInternalTransition_ng_blankEvent() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");

        int countBefore = state.getInternalTransitions().length;

        // ----------------------------------------
        // Call addInternalTransition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addInternalTransition,
                tool,
                new StateWithInternalTransitionDTO(state.getId(), " ", "testGuard", "testAction"),
                StateDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The event of an internal transition must not be empty or blank."));

        // Check that nothing was added
        assertEquals(countBefore, state.getInternalTransitions().length);
    }

    @Test
    void addInternalTransition_ng_duplicate() throws Exception {
        // Get state
        IState state = (IState) TestSupport.instance().getNamedElementByClassAndName(
            IState.class,
            "State0");

        int countBefore = state.getInternalTransitions().length;

        TestSupport.instance().invokeToolMethodReturningDto(
            addInternalTransition,
            tool,
            new StateWithInternalTransitionDTO(state.getId(), "dupEvent", "testGuard", "testAction"),
            StateDTO.class);

        // ----------------------------------------
        // Call addInternalTransition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addInternalTransition,
                tool,
                new StateWithInternalTransitionDTO(state.getId(), " dupEvent ", "testGuard ", " testAction"),
                StateDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("already has the internal transition 'dupEvent [testGuard] / testAction'"));

        // Check that only the first one was added
        assertEquals(countBefore + 1, state.getInternalTransitions().length);
    }

    @Test
    void addInternalTransition_ng_finalState() throws Exception {
        // Create final state
        StateMachineDiagramEditor stateMachineDiagramEditor = projectAccessor.getDiagramEditorFactory().getStateMachineDiagramEditor();
        IFinalState finalState = transactionSupport.call(() -> {
            IStateMachineDiagram diagram = stateMachineDiagramEditor.createStatemachineDiagram(projectAccessor.getProject(), "Final State Fixture");
            stateMachineDiagramEditor.setDiagram(diagram);
            return (IFinalState) stateMachineDiagramEditor.createFinalState(null, new Point2D.Double(100, 100)).getModel();
        });

        // ----------------------------------------
        // Call addInternalTransition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addInternalTransition,
                tool,
                new StateWithInternalTransitionDTO(finalState.getId(), "testEvent", "testGuard", "testAction"),
                StateDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The state (ID: " + finalState.getId() + ") is a final state."));

        // Check that nothing was added
        assertEquals(0, finalState.getInternalTransitions().length);
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
