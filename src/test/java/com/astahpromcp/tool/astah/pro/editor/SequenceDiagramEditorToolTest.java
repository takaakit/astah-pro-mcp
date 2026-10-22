package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.CombinedFragmentKind;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.SequenceDiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SequenceDiagramEditorToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
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
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/editor/SequenceDiagramEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createThumbnailContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new SequenceDiagramEditorTool(
            projectAccessor,
            transactionSupport,
            sequenceDiagramEditor,
            astahProToolSupport,
            imageCaptureSupport);

        // createSequenceDiagram() method
        createSequenceDiagram = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createSequenceDiagram",
            NewSequenceDiagramInPackageDTO.class);

        // createCombinedFragment() method
        createCombinedFragment = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createCombinedFragment",
            NewCombinedFragmentDTO.class);

        // createMessage() method
        createMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createMessage",
            NewMessageDTO.class);

        // createCreateMessage() method
        createCreateMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createCreateMessage",
            NewCreateMessageDTO.class);

        // createDestroyMessage() method
        createDestroyMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createDestroyMessage",
            NewDestroyMessageDTO.class);

        // createReturnMessage() method
        createReturnMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createReturnMessage",
            NewReturnMessageDTO.class);

        // createLostMessage() method
        createLostMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createLostMessage",
            NewLostMessageDTO.class);

        // createFoundMessage() method
        createFoundMessage = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createFoundMessage",
            NewFoundMessageDTO.class);

        // createInteractionUse() method
        createInteractionUse = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createInteractionUse",
            NewInteractionUseDTO.class);

        // createLifeline() method
        createLifeline = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createLifeline",
            NewLifelineDTO.class);

        // createTermination() method
        createTermination = TestSupport.getAccessibleMethod(
            SequenceDiagramEditorTool.class,
            "createTermination",
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
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
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
        SequenceDiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Create input DTO
        NewCombinedFragmentDTO inputDTO = new NewCombinedFragmentDTO(
            sequenceDiagram.getId(),
            "Test Combined Fragment",
            CombinedFragmentKind.ALT,
            100,
            200,
            150,
            100
        );

        // ----------------------------------------
        // Call createCombinedFragment()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createCreateMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createCreateMessage_ng_receiverIsSender() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get lifeline
        INodePresentation lifelineNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // Create input DTO
        NewCreateMessageDTO inputDTO = new NewCreateMessageDTO(
            sequenceDiagram.getId(),
            "Test Create Message",
            lifelineNode.getID(),
            lifelineNode.getID(),
            120
        );

        // ----------------------------------------
        // Call createCreateMessage()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createCreateMessage,
                tool,
                inputDTO,
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Failed to create the create message"));

        // Check that nothing was left on the diagram
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }

    @Test
    void createDestroyMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get target message
        ILinkPresentation targetMessage = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createReturnMessage,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Test Return Message", outputDTO.presentation().label());
    }

    @Test
    void createReturnMessage_ng_lifelinesCoveredByCombinedFragment() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get target message
        ILinkPresentation targetMessage = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Message",
            "Message0()");

        // A combined fragment that covers both lifelines of the target message, foo and bar
        TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createCombinedFragment,
            tool,
            new NewCombinedFragmentDTO(
                sequenceDiagram.getId(),
                "Test Combined Fragment",
                CombinedFragmentKind.ALT,
                100,
                200,
                420,
                100),
            NodePresentationDTO.class);

        // Create input DTO
        NewReturnMessageDTO inputDTO = new NewReturnMessageDTO(
            sequenceDiagram.getId(),
            targetMessage.getID(),
            "Test Return Message");

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createReturnMessage,
                tool,
                inputDTO,
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(InvalidEditingException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("covers lifeline 'foo', which the return message goes back to"));
    }

    @Test
    void createReturnMessage_ok_nestedCall() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get outer message
        ILinkPresentation outerMessage = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Message",
            "Message0()");

        // Get lifeline
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "bar : Bar");

        // A nested call into bar, sent from the activation of foo while the activation of bar from the outer message is still open
        LinkPresentationDTO innerMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createMessage,
            tool,
            new NewMessageDTO(sequenceDiagram.getId(), "Inner Message", outerMessage.getSourceEnd().getID(), receiverNode.getID(), 150),
            LinkPresentationDTO.class);
        ILinkPresentation innerMessage = (ILinkPresentation) TestSupport.instance().getPresentationById(innerMessageDTO.presentation().id());

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        LinkPresentationDTO outerReturnMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createReturnMessage,
            tool,
            new NewReturnMessageDTO(sequenceDiagram.getId(), outerMessage.getID(), "Outer Return Message"),
            LinkPresentationDTO.class);
        LinkPresentationDTO innerReturnMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createReturnMessage,
            tool,
            new NewReturnMessageDTO(sequenceDiagram.getId(), innerMessage.getID(), "Inner Return Message"),
            LinkPresentationDTO.class);

        // Check that each return message returns from its own message
        IMessage outerReturnMessage = (IMessage) TestSupport.instance().getPresentationById(outerReturnMessageDTO.presentation().id()).getModel();
        IMessage innerReturnMessage = (IMessage) TestSupport.instance().getPresentationById(innerReturnMessageDTO.presentation().id()).getModel();
        assertEquals(outerMessage.getModel(), outerReturnMessage.getActivator());
        assertEquals(innerMessage.getModel(), innerReturnMessage.getActivator());
    }

    @Test
    void createReturnMessage_ng_alreadyHasReturnMessage() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get message to a lifeline
        ILinkPresentation lifelineMessage = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Message",
            "Message0()");

        // Get lifeline and frame
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");
        INodePresentation frameNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Frame",
            "sd Sequence Diagram0");

        // A message to a gate, whose receiver end is the frame rather than an activation
        LinkPresentationDTO gateMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createMessage,
            tool,
            new NewMessageDTO(sequenceDiagram.getId(), "Gate Message", senderNode.getID(), frameNode.getID(), 300),
            LinkPresentationDTO.class);

        for (String targetMessageId : new String[] { lifelineMessage.getID(), gateMessageDTO.presentation().id() }) {
            // The first return message
            LinkPresentationDTO firstReturnMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createReturnMessage,
                tool,
                new NewReturnMessageDTO(sequenceDiagram.getId(), targetMessageId, "First Return Message"),
                LinkPresentationDTO.class);

            int presentationCountBefore = sequenceDiagram.getPresentations().length;

            // ----------------------------------------
            // Call createReturnMessage()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    createReturnMessage,
                    tool,
                    new NewReturnMessageDTO(sequenceDiagram.getId(), targetMessageId, "Second Return Message"),
                    LinkPresentationDTO.class);
            });

            // Check exception
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("already has a return message (ID: " + firstReturnMessageDTO.presentation().id() + ")"));

            // Check that no second return message was drawn
            assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
        }
    }

    @Test
    void createReturnMessage_ng_returnMessage() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get message
        ILinkPresentation message = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Message",
            "Message0()");

        // A return message to be specified by mistake
        LinkPresentationDTO returnMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createReturnMessage,
            tool,
            new NewReturnMessageDTO(sequenceDiagram.getId(), message.getID(), "Test Return Message"),
            LinkPresentationDTO.class);

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createReturnMessage,
                tool,
                new NewReturnMessageDTO(sequenceDiagram.getId(), returnMessageDTO.presentation().id(), "Return Of Return Message"),
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Message 'Test Return Message' is itself a return message"));

        // Check that nothing was drawn on the diagram
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }

    @Test
    void createReturnMessage_ng_asynchronousMessage() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "bar : Bar");

        // An asynchronous message
        LinkPresentationDTO asynchronousMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createMessage,
            tool,
            new NewMessageDTO(sequenceDiagram.getId(), "Asynchronous Message", senderNode.getID(), receiverNode.getID(), 300),
            LinkPresentationDTO.class);
        IMessage asynchronousMessage = (IMessage) TestSupport.instance().getPresentationById(asynchronousMessageDTO.presentation().id()).getModel();
        transactionSupport.run(() -> asynchronousMessage.setAsynchronous(true));

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createReturnMessage,
                tool,
                new NewReturnMessageDTO(sequenceDiagram.getId(), asynchronousMessageDTO.presentation().id(), "Test Return Message"),
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Message 'Asynchronous Message' is asynchronous"));

        // Check that nothing was drawn on the diagram
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }

    @Test
    void createReturnMessage_ng_foundMessage() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get receiver node
        INodePresentation receiverNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "bar : Bar");

        // A found message, which Astah reports as synchronous
        LinkPresentationDTO foundMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createFoundMessage,
            tool,
            new NewFoundMessageDTO(sequenceDiagram.getId(), "Test Found Message", 50, 300, receiverNode.getID()),
            LinkPresentationDTO.class);

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createReturnMessage,
                tool,
                new NewReturnMessageDTO(sequenceDiagram.getId(), foundMessageDTO.presentation().id(), "Test Return Message"),
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Message 'Test Found Message' is a found message"));

        // Check that nothing was drawn on the diagram
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }

    @Test
    void createReturnMessage_ng_lostMessage() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get sender node
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");

        // A lost message, which Astah reports as synchronous
        LinkPresentationDTO lostMessageDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createLostMessage,
            tool,
            new NewLostMessageDTO(sequenceDiagram.getId(), "Test Lost Message", senderNode.getID(), 300, 300),
            LinkPresentationDTO.class);

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createReturnMessage,
                tool,
                new NewReturnMessageDTO(sequenceDiagram.getId(), lostMessageDTO.presentation().id(), "Test Return Message"),
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Message 'Test Lost Message' is a lost message"));

        // Check that nothing was drawn on the diagram
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }

    @Test
    void createReturnMessage_ng_notOnTargetDiagram() throws Exception {
        // Get message on Sequence Diagram0
        ILinkPresentation message = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Message",
            "Message0()");

        // Another sequence diagram, whose ID an agent may pass by mistake
        ISequenceDiagram anotherSequenceDiagram = transactionSupport.call(() ->
            projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor().createSequenceDiagram(
                projectAccessor.getProject(),
                "Another Sequence Diagram"));

        int presentationCountBefore = anotherSequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createReturnMessage()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createReturnMessage,
                tool,
                new NewReturnMessageDTO(anotherSequenceDiagram.getId(), message.getID(), "Test Return Message"),
                LinkPresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The target message is not on the target sequence diagram: " + message.getID()));

        // Check that nothing was drawn on the other sequence diagram
        assertEquals(presentationCountBefore, anotherSequenceDiagram.getPresentations().length);
    }

    @Test
    void createLostMessage_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get target sender node
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get target receiver node
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createLifeline,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createLifeline_ng_notSequenceDiagram() throws Exception {
        // Create a class diagram, whose ID an agent may pass by mistake
        IClassDiagram classDiagram = transactionSupport.call(() ->
            projectAccessor.getDiagramEditorFactory().getClassDiagramEditor().createClassDiagram(
                projectAccessor.getProject(),
                "Not Sequence Diagram"));

        int presentationCountBefore = classDiagram.getPresentations().length;

        // Create input DTO
        NewLifelineDTO inputDTO = new NewLifelineDTO(
            classDiagram.getId(),
            "Test Lifeline",
            100
        );

        // ----------------------------------------
        // Call createLifeline()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createLifeline,
                tool,
                inputDTO,
                NodePresentationDTO.class);
        });

        // Check exception
        assertTrue(exception.getCause().getMessage().contains("Failed to get sequence diagram"));
        assertTrue(exception.getCause().getMessage().contains(classDiagram.getId()));

        // Check that nothing was drawn on the class diagram
        assertEquals(presentationCountBefore, classDiagram.getPresentations().length);
    }

    @Test
    void createTermination_ok() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get target node
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createTermination,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTermination_ng_notLifeline() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get message
        ILinkPresentation message = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Message",
            "Message0()");

        // The activation of foo, which an agent may pass by mistake
        String activationId = message.getSourceEnd().getID();

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createTermination()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createTermination,
                tool,
                new NewTerminationDTO(sequenceDiagram.getId(), activationId),
                NodePresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Termination target node must be one of the following node presentation types: Lifeline."));

        // Check that nothing was drawn on the diagram
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }

    @Test
    void createTermination_ng_notOnTargetDiagram() throws Exception {
        // Get lifeline on Sequence Diagram0
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");

        // Another sequence diagram, whose ID an agent may pass by mistake
        ISequenceDiagram anotherSequenceDiagram = transactionSupport.call(() ->
            projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor().createSequenceDiagram(
                projectAccessor.getProject(),
                "Another Sequence Diagram"));

        int presentationCountBefore = anotherSequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createTermination()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createTermination,
                tool,
                new NewTerminationDTO(anotherSequenceDiagram.getId(), targetNode.getID()),
                NodePresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The target lifeline is not on the target sequence diagram: " + targetNode.getID()));

        // Check that nothing was drawn on the other sequence diagram
        assertEquals(presentationCountBefore, anotherSequenceDiagram.getPresentations().length);
    }

    @Test
    void createTermination_ng_alreadyTerminated() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get target node
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");

        // The first termination
        TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createTermination,
            tool,
            new NewTerminationDTO(sequenceDiagram.getId(), targetNode.getID()),
            NodePresentationDTO.class);

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createTermination()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createTermination,
                tool,
                new NewTerminationDTO(sequenceDiagram.getId(), targetNode.getID()),
                NodePresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The lifeline 'foo : Foo' already ends with a termination (X)"));

        // Check that no second termination was drawn
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }

    @Test
    void createTermination_ng_destroyedByDestroyMessage() throws Exception {
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Get lifelines
        INodePresentation senderNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "foo : Foo");
        INodePresentation targetNode = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Lifeline",
            "bar : Bar");

        // A destroy message, which ends bar with a termination
        TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            createDestroyMessage,
            tool,
            new NewDestroyMessageDTO(sequenceDiagram.getId(), "Test Destroy Message", senderNode.getID(), targetNode.getID(), 140),
            LinkPresentationDTO.class);

        int presentationCountBefore = sequenceDiagram.getPresentations().length;

        // ----------------------------------------
        // Call createTermination()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                createTermination,
                tool,
                new NewTerminationDTO(sequenceDiagram.getId(), targetNode.getID()),
                NodePresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The lifeline 'bar : Bar' already ends with a termination (X)"));

        // Check that no second termination was drawn
        assertEquals(presentationCountBefore, sequenceDiagram.getPresentations().length);
    }
}
