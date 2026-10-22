package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.MessageDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class MessageToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private MessageTool tool;
    private Method getInfo;
    private Method setArgument;
    private Method setGuard;
    private Method setReturnValue;
    private Method setReturnValueVariable;
    private Method setAsynchronous;
    private Method setOperation;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/MessageToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new MessageTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "getInfo",
            IdDTO.class);

        // setArgument() method
        setArgument = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setArgument",
            MessageWithArgumentDTO.class);

        // setGuard() method
        setGuard = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setGuard",
            MessageWithGuardDTO.class);

        // setReturnValue() method
        setReturnValue = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setReturnValue",
            MessageWithReturnValueDTO.class);

        // setReturnValueVariable() method
        setReturnValueVariable = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setReturnValueVariable",
            MessageWithReturnValueVariableDTO.class);

        // setAsynchronous() method
        setAsynchronous = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setAsynchronous",
            MessageWithAsynchronousDTO.class);

        // setOperation() method
        setOperation = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setOperation",
            MessageWithOperationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(message.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setArgument_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Create input DTO
        MessageWithArgumentDTO inputDTO = new MessageWithArgumentDTO(
            message.getId(),
            "testArgument");

        // Check argument before setting
        assertNotEquals("testArgument", message.getArgument());

        // ----------------------------------------
        // Call setArgument()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setArgument,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check argument after setting
        assertEquals("testArgument", message.getArgument());
    }

    @Test
    void setGuard_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Create input DTO
        MessageWithGuardDTO inputDTO = new MessageWithGuardDTO(
            message.getId(),
            "testGuard");

        // Check guard before setting
        assertNotEquals("testGuard", message.getGuard());

        // ----------------------------------------
        // Call setGuard()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check guard after setting
        assertEquals("testGuard", message.getGuard());
    }

    @Test
    void setReturnValue_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Create input DTO
        MessageWithReturnValueDTO inputDTO = new MessageWithReturnValueDTO(
            message.getId(),
            "testReturnValue");

        // Check return value before setting
        assertNotEquals("testReturnValue", message.getReturnValue());

        // ----------------------------------------
        // Call setReturnValue()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setReturnValue,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check return value after setting
        assertEquals("testReturnValue", message.getReturnValue());
    }

    @Test
    void setReturnValueVariable_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Create input DTO
        MessageWithReturnValueVariableDTO inputDTO = new MessageWithReturnValueVariableDTO(
            message.getId(),
            "testReturnValueVariable");

        // Check return value variable before setting
        assertNotEquals("testReturnValueVariable", message.getReturnValueVariable());

        // ----------------------------------------
        // Call setReturnValueVariable()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setReturnValueVariable,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check return value variable after setting
        assertEquals("testReturnValueVariable", message.getReturnValueVariable());
    }

    @Test
    void setAsynchronous_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Create input DTO
        MessageWithAsynchronousDTO inputDTO = new MessageWithAsynchronousDTO(
            message.getId(),
            true);

        // Check asynchronous before setting
        assertNotEquals(true, message.isAsynchronous());

        // ----------------------------------------
        // Call setAsynchronous()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setAsynchronous,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check asynchronous after setting
        assertTrue(message.isAsynchronous());
    }

    @Test
    void setOperation_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        MessageWithOperationDTO inputDTO = new MessageWithOperationDTO(
            message.getId(),
            operation.getId());

        // Check operation after setting
        assertNull(message.getOperation());

        // ----------------------------------------
        // Call setOperation()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setOperation,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check operation after setting
        assertNotNull(message.getOperation());
    }

    @Test
    void setArgument_ng_returnMessage() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // A return message to be specified by mistake
        IMessage returnMessage = createReturnMessageOf(message);

        assertRejectedAsReturnMessage(
            setArgument,
            new MessageWithArgumentDTO(returnMessage.getId(), "testArgument"),
            "argument",
            message,
            returnMessage::getArgument);
    }

    @Test
    void setGuard_ng_returnMessage() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // A return message to be specified by mistake
        IMessage returnMessage = createReturnMessageOf(message);

        assertRejectedAsReturnMessage(
            setGuard,
            new MessageWithGuardDTO(returnMessage.getId(), "testGuard"),
            "guard",
            message,
            returnMessage::getGuard);
    }

    @Test
    void setReturnValue_ng_returnMessage() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // A return message to be specified by mistake
        IMessage returnMessage = createReturnMessageOf(message);

        assertRejectedAsReturnMessage(
            setReturnValue,
            new MessageWithReturnValueDTO(returnMessage.getId(), "testReturnValue"),
            "return value",
            message,
            returnMessage::getReturnValue);
    }

    @Test
    void setReturnValueVariable_ng_returnMessage() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // A return message to be specified by mistake
        IMessage returnMessage = createReturnMessageOf(message);

        assertRejectedAsReturnMessage(
            setReturnValueVariable,
            new MessageWithReturnValueVariableDTO(returnMessage.getId(), "testReturnValueVariable"),
            "return value variable",
            message,
            returnMessage::getReturnValueVariable);
    }

    @Test
    void setAsynchronous_ng_returnMessage() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // A return message to be specified by mistake
        IMessage returnMessage = createReturnMessageOf(message);

        assertRejectedAsReturnMessage(
            setAsynchronous,
            new MessageWithAsynchronousDTO(returnMessage.getId(), !returnMessage.isAsynchronous()),
            "asynchrony",
            message,
            returnMessage::isAsynchronous);
    }

    @Test
    void setOperation_ng_returnMessage() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // A return message to be specified by mistake
        IMessage returnMessage = createReturnMessageOf(message);

        assertRejectedAsReturnMessage(
            setOperation,
            new MessageWithOperationDTO(returnMessage.getId(), operation.getId()),
            "operation",
            message,
            returnMessage::getOperation);
    }

    @Test
    void setOperation_ng_lostMessage() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElementByClassAndName(
            IMessage.class,
            "");

        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // A lost message, which has no lifeline as its receiver
        IMessage lostMessage = createLostMessageFrom(message);

        // ----------------------------------------
        // Call setOperation()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                setOperation,
                tool,
                new MessageWithOperationDTO(lostMessage.getId(), operation.getId()),
                MessageDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains(
            "Message 'Test Lost Message' (ID: " + lostMessage.getId() + ") is not received by a lifeline"));

        // Check that no operation was set
        assertNull(lostMessage.getOperation());
    }

    private void assertRejectedAsReturnMessage(
            Method toolMethod,
            Object inputDTO,
            String property,
            IMessage callMessage,
            Supplier<Object> currentValue) {

        Object valueBefore = currentValue.get();

        // ----------------------------------------
        // Call the tool method
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                toolMethod,
                tool,
                inputDTO,
                MessageDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        String errorMessage = exception.getCause().getMessage();
        assertTrue(errorMessage.contains("Message 'Test Return Message' "));
        assertTrue(errorMessage.contains("is a return message, and Astah does not allow the " + property + " of a return message to be set."));
        assertTrue(errorMessage.contains("specify the message it returns from instead: '" + callMessage.getName() + "' (ID: " + callMessage.getId() + ")."));
        assertTrue(errorMessage.contains("set its name with set_name."));

        // Check that the return message was left as it was
        assertEquals(valueBefore, currentValue.get());
    }

    private IMessage createReturnMessageOf(IMessage callMessage) throws Exception {
        ILinkPresentation callMessagePresentation = (ILinkPresentation) callMessage.getPresentations()[0];

        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        sequenceDiagramEditor.setDiagram(callMessagePresentation.getDiagram());

        ILinkPresentation returnMessagePresentation = transactionSupport.call( () -> {
            return sequenceDiagramEditor.createReturnMessage(
                "Test Return Message",
                callMessagePresentation);
        });

        IMessage returnMessage = (IMessage) returnMessagePresentation.getModel();
        assertTrue(returnMessage.isReturnMessage());
        assertEquals(callMessage, returnMessage.getActivator());

        return returnMessage;
    }

    private IMessage createLostMessageFrom(IMessage callMessage) throws Exception {
        ILinkPresentation callMessagePresentation = (ILinkPresentation) callMessage.getPresentations()[0];
        INodePresentation sender = callMessagePresentation.getSource();
        Point2D callMessageStart = callMessagePresentation.getPoints()[0];

        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        sequenceDiagramEditor.setDiagram(callMessagePresentation.getDiagram());

        ILinkPresentation lostMessagePresentation = transactionSupport.call( () -> {
            return sequenceDiagramEditor.createLostMessage(
                "Test Lost Message",
                sender,
                new Point2D.Double(callMessageStart.getX() + 150, callMessageStart.getY() + 100));
        });

        IMessage lostMessage = (IMessage) lostMessagePresentation.getModel();
        assertNull(lostMessage.getTarget());

        return lostMessage;
    }
}
