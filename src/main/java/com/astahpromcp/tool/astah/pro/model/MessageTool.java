package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.MessageDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.MessageDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IMessage.html
@Slf4j
public class MessageTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public MessageTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create message tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_msg_info",
                        "Return detailed information about the specified message (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        MessageDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_arg_of_msg",
                        "Set the argument of the specified message (specified by ID), and return the message information after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                        this::setArgument,
                        MessageWithArgumentDTO.class,
                        MessageDTO.class),

                ToolSupport.definition(
                        "set_guard_of_msg",
                        "Set the guard of the specified message (specified by ID), and return the message information after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                        this::setGuard,
                        MessageWithGuardDTO.class,
                        MessageDTO.class),

                ToolSupport.definition(
                        "set_return_val_of_msg",
                        "Set the return value of the specified message (specified by ID), and return the message information after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                        this::setReturnValue,
                        MessageWithReturnValueDTO.class,
                        MessageDTO.class),

                ToolSupport.definition(
                        "set_return_val_variable_of_msg",
                        "Set the return value variable of the specified message (specified by ID), and return the message information after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                        this::setReturnValueVariable,
                        MessageWithReturnValueVariableDTO.class,
                        MessageDTO.class),

                ToolSupport.definition(
                        "set_async_of_msg",
                        "Set the asynchronous of the specified message (specified by ID), and return the message information after it is set.",
                        this::setAsynchronous,
                        MessageWithAsynchronousDTO.class,
                        MessageDTO.class),

                ToolSupport.definition(
                        "set_ope_of_msg",
                        "Set the operation of the specified message (specified by ID), and return the message information after it is set.",
                        this::setOperation,
                        MessageWithOperationDTO.class,
                        MessageDTO.class)
        );
    }

    private MessageDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get message information: {}", param);

        IMessage astahMessage = astahProToolSupport.getMessage(param.id());

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    private MessageDTO setArgument(McpSyncServerExchange exchange, MessageWithArgumentDTO param) throws Exception {
        log.debug("Set argument of message: {}", param);

        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());

        try {
            transactionManager.beginTransaction();
            astahMessage.setArgument(param.argument());
            transactionManager.endTransaction();

            return MessageDTOAssembler.toDTO(astahMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private MessageDTO setGuard(McpSyncServerExchange exchange, MessageWithGuardDTO param) throws Exception {
        log.debug("Set guard of message: {}", param);

        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());

        try {
            transactionManager.beginTransaction();
            astahMessage.setGuard(param.guard());
            transactionManager.endTransaction();

            return MessageDTOAssembler.toDTO(astahMessage);
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private MessageDTO setReturnValue(McpSyncServerExchange exchange, MessageWithReturnValueDTO param) throws Exception {
        log.debug("Set return value of message: {}", param);

        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());

        try {
            transactionManager.beginTransaction();
            astahMessage.setReturnValue(param.returnValue());
            transactionManager.endTransaction();

            return MessageDTOAssembler.toDTO(astahMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private MessageDTO setReturnValueVariable(McpSyncServerExchange exchange, MessageWithReturnValueVariableDTO param) throws Exception {
        log.debug("Set return value variable of message: {}", param);

        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());

        try {
            transactionManager.beginTransaction();
            astahMessage.setReturnValueVariable(param.returnValueVariable());
            transactionManager.endTransaction();

            return MessageDTOAssembler.toDTO(astahMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private MessageDTO setAsynchronous(McpSyncServerExchange exchange, MessageWithAsynchronousDTO param) throws Exception {
        log.debug("Set asynchronous of message: {}", param);

        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());

        try {
            transactionManager.beginTransaction();
            astahMessage.setAsynchronous(param.isAsynchronous());
            transactionManager.endTransaction();

            return MessageDTOAssembler.toDTO(astahMessage);
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private MessageDTO setOperation(McpSyncServerExchange exchange, MessageWithOperationDTO param) throws Exception {
        log.debug("Set operation of message: {}", param);

        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());
        IOperation astahOperation = astahProToolSupport.getOperation(param.operationId());

        try {
            transactionManager.beginTransaction();
            astahMessage.setOperation(astahOperation);
            transactionManager.endTransaction();

            return MessageDTOAssembler.toDTO(astahMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
