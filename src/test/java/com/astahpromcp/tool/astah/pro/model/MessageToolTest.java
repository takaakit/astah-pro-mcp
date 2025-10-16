package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.MessageDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class MessageToolTest {

    private ProjectAccessor projectAccessor;
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
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/MessageToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new MessageTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setArgument() method
        setArgument = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setArgument",
            McpSyncServerExchange.class,
            MessageWithArgumentDTO.class);

        // setGuard() method
        setGuard = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setGuard",
            McpSyncServerExchange.class,
            MessageWithGuardDTO.class);

        // setReturnValue() method
        setReturnValue = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setReturnValue",
            McpSyncServerExchange.class,
            MessageWithReturnValueDTO.class);

        // setReturnValueVariable() method
        setReturnValueVariable = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setReturnValueVariable",
            McpSyncServerExchange.class,
            MessageWithReturnValueVariableDTO.class);

        // setAsynchronous() method
        setAsynchronous = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setAsynchronous",
            McpSyncServerExchange.class,
            MessageWithAsynchronousDTO.class);

        // setOperation() method
        setOperation = TestSupport.getAccessibleMethod(
            MessageTool.class,
            "setOperation",
            McpSyncServerExchange.class,
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
        IMessage message = (IMessage) TestSupport.instance().getNamedElement(
            IMessage.class,
            "");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(message.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IMessage message = (IMessage) TestSupport.instance().getNamedElement(
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
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IMessage message = (IMessage) TestSupport.instance().getNamedElement(
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
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IMessage message = (IMessage) TestSupport.instance().getNamedElement(
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
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IMessage message = (IMessage) TestSupport.instance().getNamedElement(
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
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IMessage message = (IMessage) TestSupport.instance().getNamedElement(
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
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAsynchronous,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check asynchronous after setting
        assertEquals(true, message.isAsynchronous());
    }

    @Test
    void setOperation_ok() throws Exception {
        // Get message
        IMessage message = (IMessage) TestSupport.instance().getNamedElement(
            IMessage.class,
            "");
        
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElement(
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
        MessageDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setOperation,
            tool,
            inputDTO,
            MessageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check operation after setting
        assertNotNull(message.getOperation());
    }
}
