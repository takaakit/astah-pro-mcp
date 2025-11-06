package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.TransitionDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.StateMachineDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StateMachineDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private StateMachineDiagramEditorTool tool;
    private Method addRegion;
    private Method deleteRegion;
    private Method changeParentOfState;
    private Method createChoicePseudostate;
    private Method createDeepHistoryPseudostate;
    private Method createShallowHistoryPseudostate;
    private Method createFinalState;
    private Method createForkPseudostate;
    private Method createInitialPseudostate;
    private Method createJoinPseudostate;
    private Method createJunctionPseudostate;
    private Method createState;
    private Method createStateMachineDiagram;
    private Method createSubMachineState;
    private Method createTransition;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        StateMachineDiagramEditor stateMachineDiagramEditor = projectAccessor.getDiagramEditorFactory().getStateMachineDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/StateMachineDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new StateMachineDiagramEditorTool(
            projectAccessor,
            transactionManager,
            stateMachineDiagramEditor,
            astahProToolSupport,
            true);

        // Methods
        addRegion = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "addRegion",
            McpSyncServerExchange.class,
            NewRegionDTO.class);

        deleteRegion = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "deleteRegion",
            McpSyncServerExchange.class,
            DeleteRegionDTO.class);

        changeParentOfState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "changeParentOfState",
            McpSyncServerExchange.class,
            ChangeParentStateDTO.class);

        createChoicePseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createChoicePseudostate",
            McpSyncServerExchange.class,
            NewChoicePseudostateDTO.class);

        createDeepHistoryPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createDeepHistoryPseudostate",
            McpSyncServerExchange.class,
            NewDeepHistoryPseudostateDTO.class);

        createShallowHistoryPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createShallowHistoryPseudostate",
            McpSyncServerExchange.class,
            NewShallowHistoryPseudostateDTO.class);

        createFinalState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createFinalState",
            McpSyncServerExchange.class,
            NewFinalStateDTO.class);

        createForkPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createForkPseudostate",
            McpSyncServerExchange.class,
            NewForkPseudostateDTO.class);

        createInitialPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createInitialPseudostate",
            McpSyncServerExchange.class,
            NewInitialPseudostateDTO.class);

        createJoinPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createJoinPseudostate",
            McpSyncServerExchange.class,
            NewJoinPseudostateDTO.class);

        createJunctionPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createJunctionPseudostate",
            McpSyncServerExchange.class,
            NewJunctionPseudostateDTO.class);

        createState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createState",
            McpSyncServerExchange.class,
            NewStateDTO.class);

        createStateMachineDiagram = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createStateMachineDiagram",
            McpSyncServerExchange.class,
            NewStateMachineDiagramDTO.class);

        createSubMachineState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createSubMachineState",
            McpSyncServerExchange.class,
            NewSubMachineStateDTO.class);

        createTransition = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createTransition",
            McpSyncServerExchange.class,
            NewTransitionDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void addRegion() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");

        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State0");
        
        // Create input DTO
        NewRegionDTO inputDTO = new NewRegionDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            true);

        // ----------------------------------------
        // Call addRegion()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            addRegion,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void deleteRegion() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State3");
        
        // Create input DTO
        DeleteRegionDTO inputDTO = new DeleteRegionDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            0);

        // ----------------------------------------
        // Call deleteRegion()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            deleteRegion,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void changeParentOfState() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");

        // Get target node presentation
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State1");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        ChangeParentStateDTO inputDTO = new ChangeParentStateDTO(
            stateMachineDiagram.getId(),
            targetNodePresentation.getID(),
            parentNodePresentation.getID());

        // ----------------------------------------
        // Call changeParentOfState()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeParentOfState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createChoicePseudostate() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewChoicePseudostateDTO inputDTO = new NewChoicePseudostateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100);

        // ----------------------------------------
        // Call createChoicePseudostate()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createChoicePseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createDeepHistoryPseudostate() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewDeepHistoryPseudostateDTO inputDTO = new NewDeepHistoryPseudostateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100);

        // ----------------------------------------
        // Call createDeepHistoryPseudostate()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createDeepHistoryPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createShallowHistoryPseudostate() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewShallowHistoryPseudostateDTO inputDTO = new NewShallowHistoryPseudostateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100);

        // ----------------------------------------
        // Call createShallowHistoryPseudostate()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createShallowHistoryPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createFinalState() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewFinalStateDTO inputDTO = new NewFinalStateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100);

        // ----------------------------------------
        // Call createFinalState()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createFinalState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createForkPseudostate() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewForkPseudostateDTO inputDTO = new NewForkPseudostateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100,
            50,
            30);

        // ----------------------------------------
        // Call createForkPseudostate()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createForkPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInitialPseudostate() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewInitialPseudostateDTO inputDTO = new NewInitialPseudostateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100);

        // ----------------------------------------
        // Call createInitialPseudostate()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInitialPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createJoinPseudostate() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewJoinPseudostateDTO inputDTO = new NewJoinPseudostateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100,
            50,
            30);

        // ----------------------------------------
        // Call createJoinPseudostate()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createJoinPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createJunctionPseudostate() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewJunctionPseudostateDTO inputDTO = new NewJunctionPseudostateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100);

        // ----------------------------------------
        // Call createJunctionPseudostate()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createJunctionPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createState() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewStateDTO inputDTO = new NewStateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            100,
            100);

        // ----------------------------------------
        // Call createState()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createStateMachineDiagram() throws Exception {
        // Get parent node presentation
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");

        // Create input DTO
        NewStateMachineDiagramDTO inputDTO = new NewStateMachineDiagramDTO(
                parentPackage.getId(),
            "NewStateMachineDiagram");

        // ----------------------------------------
        // Call createStateMachineDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createStateMachineDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createSubMachineState() throws Exception {
        // Get state machine diagrams
        IStateMachineDiagram stateMachineDiagram0 = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");

        IStateMachineDiagram stateMachineDiagram1 = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
                IStateMachineDiagram.class,
                "Statemachine Diagram1");

        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "SubmachineState",
            "SubmachineState0");

        //
        
        // Create input DTO
        NewSubMachineStateDTO inputDTO = new NewSubMachineStateDTO(
            stateMachineDiagram0.getId(),
            parentNodePresentation.getID(),
            stateMachineDiagram1.getId(),
            1000,
            100);

        // ----------------------------------------
        // Call createSubMachineState()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createSubMachineState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTransition() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get source node presentation
        INodePresentation sourceNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State1");
        
        // Get target node presentation
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "State",
            "State2");
        
        // Create input DTO
        NewTransitionDTO inputDTO = new NewTransitionDTO(
            stateMachineDiagram.getId(),
            sourceNodePresentation.getID(),
            targetNodePresentation.getID());

        // ----------------------------------------
        // Call createTransition()
        // ----------------------------------------
        TransitionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTransition,
            tool,
            inputDTO,
            TransitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
