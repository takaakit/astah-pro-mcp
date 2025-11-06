package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.CombinedFragmentKind;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.SequenceDiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SequenceDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private SequenceDiagramEditorTool tool;
    private Method createCombinedFragment;
    private Method createSequenceDiagram;
    private Method createMessage;
    private Method createCreateMessage;
    private Method createDestroyMessage;
    private Method createReturnMessage;
    private Method createLostMessage;
    private Method createFoundMessage;
    private Method createInteractionUse;
    private Method createLifeline;
    private Method createTermination;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/editor/SequenceDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();

        // Tool
        tool = new SequenceDiagramEditorTool(
            projectAccessor,
            transactionManager,
            sequenceDiagramEditor,
            astahProToolSupport,
            true);

        // createSequenceDiagram() method
        createSequenceDiagram = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createSequenceDiagram",
            McpSyncServerExchange.class,
            NewSequenceDiagramInPackageDTO.class);

        // createCombinedFragment() method
        createCombinedFragment = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createCombinedFragment",
            McpSyncServerExchange.class,
            NewCombinedFragmentDTO.class);

        // createMessage() method
        createMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createMessage",
            McpSyncServerExchange.class,
            NewMessageDTO.class);

        // createCreateMessage() method
        createCreateMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createCreateMessage",
            McpSyncServerExchange.class,
            NewCreateMessageDTO.class);

        // createDestroyMessage() method
        createDestroyMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createDestroyMessage",
            McpSyncServerExchange.class,
            NewDestroyMessageDTO.class);

        // createReturnMessage() method
        createReturnMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createReturnMessage",
            McpSyncServerExchange.class,
            NewReturnMessageDTO.class);

        // createLostMessage() method
        createLostMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createLostMessage",
            McpSyncServerExchange.class,
            NewLostMessageDTO.class);

        // createFoundMessage() method
        createFoundMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createFoundMessage",
            McpSyncServerExchange.class,
            NewFoundMessageDTO.class);

        // createInteractionUse() method
        createInteractionUse = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createInteractionUse",
            McpSyncServerExchange.class,
            NewInteractionUseDTO.class);

        // createLifeline() method
        createLifeline = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createLifeline",
            McpSyncServerExchange.class,
            NewLifelineDTO.class);

        // createTermination() method
        createTermination = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createTermination",
            McpSyncServerExchange.class,
            NewTerminationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createSequenceDiagram_ok() throws Exception {
        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewSequenceDiagramInPackageDTO inputDTO = new NewSequenceDiagramInPackageDTO(
            astahPackage.getId(),
            "Test Sequence Diagram"
        );

        // ----------------------------------------
        // Call createSequenceDiagram()
        // ----------------------------------------
        SequenceDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createSequenceDiagram,
            tool,
            inputDTO,
            SequenceDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createCombinedFragment_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Create input DTO
        NewCombinedFragmentDTO inputDTO = new NewCombinedFragmentDTO(
            sequenceDiagram.getId(),
            "Test Combined Fragment",
            CombinedFragmentKind.alt,
            100,
            200,
            150,
            100
        );

        // ----------------------------------------
        // Call createCombinedFragment()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createCombinedFragment,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "foo : Foo");
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "bar : Bar");
        
        // Create input DTO
        NewMessageDTO inputDTO = new NewMessageDTO(
            sequenceDiagram.getId(),
            "Test Message",
            senderNode.getID(),
            receiverNode.getID(),
            100
        );

        // ----------------------------------------
        // Call createMessage()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createCreateMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "foo : Foo");
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "bar : Bar");
        
        // Create input DTO
        NewCreateMessageDTO inputDTO = new NewCreateMessageDTO(
            sequenceDiagram.getId(),
            "Test Create Message",
            senderNode.getID(),
            receiverNode.getID(),
            120
        );

        // ----------------------------------------
        // Call createCreateMessage()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createCreateMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createDestroyMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "foo : Foo");
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "bar : Bar");
        
        // Create input DTO
        NewDestroyMessageDTO inputDTO = new NewDestroyMessageDTO(
            sequenceDiagram.getId(),
            "Test Destroy Message",
            senderNode.getID(),
            receiverNode.getID(),
            140
        );

        // ----------------------------------------
        // Call createDestroyMessage()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createDestroyMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createReturnMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get target message
        ILinkPresentation targetMessage = (ILinkPresentation) TestSupport.instance().getPresentation(
            "Message",
            "Message0()");
        
        // Create input DTO
        NewReturnMessageDTO inputDTO = new NewReturnMessageDTO(
            sequenceDiagram.getId(),
            targetMessage.getID(),
            "Test Return Message");

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createReturnMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createLostMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Get target sender node
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "foo : Foo");
        
        // Create input DTO
        NewLostMessageDTO inputDTO = new NewLostMessageDTO(
            sequenceDiagram.getId(),
            "Test Lost Message",
            targetNode.getID(),
            200,
            150
        );

        // ----------------------------------------
        // Call createLostMessage()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createLostMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createFoundMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Get target receiver node
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "bar : Bar");
        
        // Create input DTO
        NewFoundMessageDTO inputDTO = new NewFoundMessageDTO(
            sequenceDiagram.getId(),
            "Test Found Message",
            50,
            100,
            targetNode.getID()
        );

        // ----------------------------------------
        // Call createFoundMessage()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createFoundMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInteractionUse_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Create input DTO
        NewInteractionUseDTO inputDTO = new NewInteractionUseDTO(
            sequenceDiagram.getId(),
            "Test Interaction Use",
            100,
            200,
            150,
            100
        );

        // ----------------------------------------
        // Call createInteractionUse()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInteractionUse,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createLifeline_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Create input DTO
        NewLifelineDTO inputDTO = new NewLifelineDTO(
            sequenceDiagram.getId(),
            "Test Lifeline",
            100
        );

        // ----------------------------------------
        // Call createLifeline()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createLifeline,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTermination_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram0");
        
        // Get target node
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentation(
            "Lifeline",
            "foo : Foo");
        
        // Create input DTO
        NewTerminationDTO inputDTO = new NewTerminationDTO(
            sequenceDiagram.getId(),
            targetNode.getID()
        );

        // ----------------------------------------
        // Call createTermination()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTermination,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
