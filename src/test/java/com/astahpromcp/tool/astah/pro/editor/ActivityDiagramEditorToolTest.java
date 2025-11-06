package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ActivityDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActivityDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private ActivityDiagramEditorTool tool;
    private Method createAcceptEventAction;
    private Method createAcceptTimeEventAction;
    private Method createAction;
    private Method createActivityDiagram;
    private Method createActivityParameterNode;
    private Method createCallBehaviorAction;
    private Method createConnector;
    private Method createDecisionMergeNode;
    private Method createDependency;
    private Method createFinalNode;
    private Method createFlow;
    private Method createFlowFinalNode;
    private Method createForkNode;
    private Method createInitialNode;
    private Method createJoinNode;
    private Method createObjectNode;
    private Method createPartition;
    private Method createPin;
    private Method createProcess;
    private Method createSendSignalAction;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        ActivityDiagramEditor activityDiagramEditor = projectAccessor.getDiagramEditorFactory().getActivityDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/ActivityDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ActivityDiagramEditorTool(
            projectAccessor,
            transactionManager,
            activityDiagramEditor,
            astahProToolSupport,
            true);

        // createAcceptEventAction() method
        createAcceptEventAction = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createAcceptEventAction",
            McpSyncServerExchange.class,
            NewAcceptEventActionDTO.class);

        // createAcceptTimeEventAction() method
        createAcceptTimeEventAction = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createAcceptTimeEventAction",
            McpSyncServerExchange.class,
            NewAcceptTimeEventActionDTO.class);

        // createAction() method
        createAction = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createAction",
            McpSyncServerExchange.class,
            NewActionDTO.class);

        // createActivityDiagram() method
        createActivityDiagram = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createActivityDiagram",
            McpSyncServerExchange.class,
            NewActivityDiagramDTO.class);

        // createActivityParameterNode() method
        createActivityParameterNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createActivityParameterNode",
            McpSyncServerExchange.class,
            NewActivityParameterNodeDTO.class);

        // createCallBehaviorAction() method
        createCallBehaviorAction = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createCallBehaviorAction",
            McpSyncServerExchange.class,
            NewCallBehaviorActionDTO.class);

        // createConnector() method
        createConnector = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createConnector",
            McpSyncServerExchange.class,
            NewConnectorDTO.class);

        // createDecisionMergeNode() method
        createDecisionMergeNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createDecisionMergeNode",
            McpSyncServerExchange.class,
            NewDecisionMergeNodeDTO.class);

        // createDependency() method
        createDependency = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createDependency",
            McpSyncServerExchange.class,
            NewDependencyDTO.class);

        // createFinalNode() method
        createFinalNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createFinalNode",
            McpSyncServerExchange.class,
            NewFinalNodeDTO.class);

        // createFlow() method
        createFlow = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createFlow",
            McpSyncServerExchange.class,
            NewFlowDTO.class);

        // createFlowFinalNode() method
        createFlowFinalNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createFlowFinalNode",
            McpSyncServerExchange.class,
            NewFlowFinalNodeDTO.class);

        // createForkNode() method
        createForkNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createForkNode",
            McpSyncServerExchange.class,
            NewForkNodeDTO.class);

        // createInitialNode() method
        createInitialNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createInitialNode",
            McpSyncServerExchange.class,
            NewInitialNodeDTO.class);

        // createJoinNode() method
        createJoinNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createJoinNode",
            McpSyncServerExchange.class,
            NewJoinNodeDTO.class);

        // createObjectNode() method
        createObjectNode = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createObjectNode",
            McpSyncServerExchange.class,
            NewObjectNodeDTO.class);

        // createPartition() method
        createPartition = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createPartition",
            McpSyncServerExchange.class,
            NewPartitionDTO.class);

        // createPin() method
        createPin = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createPin",
            McpSyncServerExchange.class,
            NewPinWithBaseClassAndParentActionDTO.class);

        // createProcess() method
        createProcess = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createProcess",
            McpSyncServerExchange.class,
            NewProcessDTO.class);

        // createSendSignalAction() method
        createSendSignalAction = TestSupport.getAccessibleMethod(
            ActivityDiagramEditorTool.class,
            "createSendSignalAction",
            McpSyncServerExchange.class,
            NewSendSignalActionDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createAcceptEventAction_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewAcceptEventActionDTO inputDTO = new NewAcceptEventActionDTO(
            activityDiagram.getId(),
            "Test Accept Event Action",
            100,
            200);

        // ----------------------------------------
        // Call createAcceptEventAction()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createAcceptEventAction,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createAcceptTimeEventAction_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewAcceptTimeEventActionDTO inputDTO = new NewAcceptTimeEventActionDTO(
            activityDiagram.getId(),
            "Test Accept Time Event Action",
            120,
            220);

        // ----------------------------------------
        // Call createAcceptTimeEventAction()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createAcceptTimeEventAction,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createAction_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewActionDTO inputDTO = new NewActionDTO(
            activityDiagram.getId(),
            "Test Action",
            150,
            250);

        // ----------------------------------------
        // Call createAction()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createAction,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createActivityDiagram_ok() throws Exception {
        // Get package
        IPackage package_ = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewActivityDiagramDTO inputDTO = new NewActivityDiagramDTO(
            package_.getId(),
            "Test Activity Diagram");

        // ----------------------------------------
        // Call createActivityDiagram()
        // ----------------------------------------
        ActivityDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createActivityDiagram,
            tool,
            inputDTO,
            ActivityDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createActivityParameterNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Get class
        IClass class_ = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewActivityParameterNodeDTO inputDTO = new NewActivityParameterNodeDTO(
            activityDiagram.getId(),
            class_.getId(),
            "Test Activity Parameter Node",
            130,
            230);

        // ----------------------------------------
        // Call createActivityParameterNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createActivityParameterNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createCallBehaviorAction_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Get reference activity diagram
        IActivityDiagram referenceActivityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram1");
        
        // Create input DTO
        NewCallBehaviorActionDTO inputDTO = new NewCallBehaviorActionDTO(
            activityDiagram.getId(),
            referenceActivityDiagram.getId(),
            "Test Call Behavior Action",
            140,
            240);

        // ----------------------------------------
        // Call createCallBehaviorAction()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createCallBehaviorAction,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createConnector_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewConnectorDTO inputDTO = new NewConnectorDTO(
            activityDiagram.getId(),
            "Test Connector",
            160,
            260);

        // ----------------------------------------
        // Call createConnector()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createConnector,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createDecisionMergeNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewDecisionMergeNodeDTO inputDTO = new NewDecisionMergeNodeDTO(
            activityDiagram.getId(),
            170,
            270);

        // ----------------------------------------
        // Call createDecisionMergeNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createDecisionMergeNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createDependency_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Get client node presentation
        INodePresentation clientNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Action",
            "Action0");
        
        // Get supplier node presentation
        INodePresentation supplierNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
                "Action",
            "Action1");
        
        // Create input DTO
        NewDependencyDTO inputDTO = new NewDependencyDTO(
            activityDiagram.getId(),
            clientNodePresentation.getID(),
            supplierNodePresentation.getID(),
            "Test Dependency");

        // ----------------------------------------
        // Call createDependency()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createDependency,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createFinalNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewFinalNodeDTO inputDTO = new NewFinalNodeDTO(
            activityDiagram.getId(),
            "Test Final Node",
            200,
            300);

        // ----------------------------------------
        // Call createFinalNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createFinalNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createFlow_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Get source node presentation
        INodePresentation sourceNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Action",
            "Action0");
        
        // Get target node presentation
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
                "Action",
            "Action1");
        
        // Create input DTO
        NewFlowDTO inputDTO = new NewFlowDTO(
            activityDiagram.getId(),
            sourceNodePresentation.getID(),
            targetNodePresentation.getID());

        // ----------------------------------------
        // Call createFlow()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createFlow,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createFlowFinalNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewFlowFinalNodeDTO inputDTO = new NewFlowFinalNodeDTO(
            activityDiagram.getId(),
            "Test Flow Final Node",
            210,
            310);

        // ----------------------------------------
        // Call createFlowFinalNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createFlowFinalNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createForkNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewForkNodeDTO inputDTO = new NewForkNodeDTO(
            activityDiagram.getId(),
            220,
            320);

        // ----------------------------------------
        // Call createForkNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createForkNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInitialNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewInitialNodeDTO inputDTO = new NewInitialNodeDTO(
            activityDiagram.getId(),
            "Test Initial Node",
            50,
            100);

        // ----------------------------------------
        // Call createInitialNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInitialNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createJoinNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewJoinNodeDTO inputDTO = new NewJoinNodeDTO(
            activityDiagram.getId(),
            230,
            330);

        // ----------------------------------------
        // Call createJoinNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createJoinNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createObjectNode_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Get class
        IClass class_ = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewObjectNodeDTO inputDTO = new NewObjectNodeDTO(
            activityDiagram.getId(),
            class_.getId(),
            "Test Object Node",
            240,
            340);

        // ----------------------------------------
        // Call createObjectNode()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createObjectNode,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Disabled
    @Test
    void createPartition_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Get super partition
        INodePresentation superPartition = (INodePresentation) TestSupport.instance().getPresentation(
            "Partition",
            "Partition0");
        
        // Get previous partition
        INodePresentation previousPartition = (INodePresentation) TestSupport.instance().getPresentation(
            "Partition",
            "Partition3");    
    
        // Create input DTO
        NewPartitionDTO inputDTO = new NewPartitionDTO(
            activityDiagram.getId(),
            superPartition.getID(),
            previousPartition.getID(),
            "Test Partition",
            true);

        // ----------------------------------------
        // Call createPartition()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createPartition,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createPin_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Get class
        IClass class_ = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Get parent action
        INodePresentation parentAction = (INodePresentation) TestSupport.instance().getPresentation(
            "Action",
            "Action0");
        
        // Create input DTO
        NewPinWithBaseClassAndParentActionDTO inputDTO = new NewPinWithBaseClassAndParentActionDTO(
            activityDiagram.getId(),
            class_.getId(),
            parentAction.getID(),
            "Test Pin",
            true,
            250,
            350);

        // ----------------------------------------
        // Call createPin()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createPin,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createProcess_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewProcessDTO inputDTO = new NewProcessDTO(
            activityDiagram.getId(),
            "Test Process",
            300,
            400);

        // ----------------------------------------
        // Call createProcess()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createProcess,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createSendSignalAction_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        NewSendSignalActionDTO inputDTO = new NewSendSignalActionDTO(
            activityDiagram.getId(),
            "Test Send Signal Action",
            260,
            360);

        // ----------------------------------------
        // Call createSendSignalAction()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createSendSignalAction,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
