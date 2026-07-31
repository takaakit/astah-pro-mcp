package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.editor.StateMachineDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

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
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        StateMachineDiagramEditor stateMachineDiagramEditor = projectAccessor.getDiagramEditorFactory().getStateMachineDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/StateMachineDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createSmallImageContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new StateMachineDiagramEditorTool(
            projectAccessor,
            transactionSupport,
            stateMachineDiagramEditor,
            astahProToolSupport,
            imageCaptureSupport,
            true);

        // Methods
        addRegion = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "addRegion",
            NewRegionDTO.class);

        deleteRegion = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "deleteRegion",
            DeleteRegionDTO.class);

        changeParentOfState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "changeParentOfState",
            ChangeParentStateDTO.class);

        createChoicePseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createChoicePseudostate",
            NewChoicePseudostateDTO.class);

        createDeepHistoryPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createDeepHistoryPseudostate",
            NewDeepHistoryPseudostateDTO.class);

        createShallowHistoryPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createShallowHistoryPseudostate",
            NewShallowHistoryPseudostateDTO.class);

        createFinalState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createFinalState",
            NewFinalStateDTO.class);

        createForkPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createForkPseudostate",
            NewForkPseudostateDTO.class);

        createInitialPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createInitialPseudostate",
            NewInitialPseudostateDTO.class);

        createJoinPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createJoinPseudostate",
            NewJoinPseudostateDTO.class);

        createJunctionPseudostate = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createJunctionPseudostate",
            NewJunctionPseudostateDTO.class);

        createState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createState",
            NewStateDTO.class);

        createStateMachineDiagram = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createStateMachineDiagram",
            NewStateMachineDiagramDTO.class);

        createSubMachineState = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createSubMachineState",
            NewSubMachineStateDTO.class);

        createTransition = TestSupport.getAccessibleMethod(
            StateMachineDiagramEditorTool.class,
            "createTransition",
            NewTransitionDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void addRegion_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");

        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            addRegion,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void deleteRegion_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            deleteRegion,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void changeParentOfState_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");

        // Get target node presentation
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "State1");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            changeParentOfState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createChoicePseudostate_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createChoicePseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createDeepHistoryPseudostate_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createDeepHistoryPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createShallowHistoryPseudostate_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createShallowHistoryPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createFinalState_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createFinalState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createForkPseudostate_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createForkPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInitialPseudostate_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createInitialPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createJoinPseudostate_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createJoinPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createJunctionPseudostate_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createJunctionPseudostate,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createState_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "State2");
        
        // Create input DTO
        NewStateDTO inputDTO = new NewStateDTO(
            stateMachineDiagram.getId(),
            parentNodePresentation.getID(),
            "NewState",
            100,
            100);

        // ----------------------------------------
        // Call createState()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createStateMachineDiagram_ok() throws Exception {
        // Get parent node presentation
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");

        // Create input DTO
        NewStateMachineDiagramDTO inputDTO = new NewStateMachineDiagramDTO(
                parentPackage.getId(),
            "NewStateMachineDiagram");

        // ----------------------------------------
        // Call createStateMachineDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createStateMachineDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createSubMachineState_ok() throws Exception {
        // Get state machine diagrams
        IStateMachineDiagram stateMachineDiagram0 = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");

        IStateMachineDiagram stateMachineDiagram1 = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
                IStateMachineDiagram.class,
                "Statemachine Diagram1");

        // Get parent node presentation
        INodePresentation parentNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createSubMachineState,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTransition_ok() throws Exception {
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Get source node presentation
        INodePresentation sourceNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "State1");
        
        // Get target node presentation
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createTransition,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Get created link presentation
        ILinkPresentation createdLinkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationById(
            outputDTO.presentation().id());
        
        // Check with the created link presentation
        assertEquals(sourceNodePresentation.getModel().getId(), ((ITransition) createdLinkPresentation.getModel()).getSource().getId());
        assertEquals(targetNodePresentation.getModel().getId(), ((ITransition) createdLinkPresentation.getModel()).getTarget().getId());
    }
}
