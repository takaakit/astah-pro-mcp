package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.MessageDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.MessageDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IMessage.html
@Slf4j
public class MessageTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public MessageTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_msg_info",
                "Return model element information about the specified message (specified by ID).",
                this::getInfo,
                IdDTO.class,
                MessageDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_arg_of_msg",
                "Set the argument of the specified message (specified by ID), and return the model element of the message after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                this::setArgument,
                MessageWithArgumentDTO.class,
                MessageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_guard_of_msg",
                "Set the guard of the specified message (specified by ID), and return the model element of the message after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                this::setGuard,
                MessageWithGuardDTO.class,
                MessageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_return_val_of_msg",
                "Set the return value of the specified message (specified by ID), and return the model element of the message after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                this::setReturnValue,
                MessageWithReturnValueDTO.class,
                MessageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_return_val_variable_of_msg",
                "Set the return value variable of the specified message (specified by ID), and return the model element of the message after it is set. The message notation is as follows: 1: returnValueVariable = messageName(argument) : returnValue",
                this::setReturnValueVariable,
                MessageWithReturnValueVariableDTO.class,
                MessageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_async_of_msg",
                "Set the asynchronous of the specified message (specified by ID), and return the model element of the message after it is set.",
                this::setAsynchronous,
                MessageWithAsynchronousDTO.class,
                MessageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_ope_of_msg",
                "Set the operation of the specified message (specified by ID), and return the model element of the message after it is set. Note that, in order to set an operation to a message, the receiver of the message must be a lifeline whose base class is set.",
                this::setOperation,
                MessageWithOperationDTO.class,
                MessageDTO.class)
        );
    }

    private MessageDTO getInfo(IdDTO param) throws Exception {
        IMessage astahMessage = astahProToolSupport.getMessage(param.id());

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    private MessageDTO setArgument(MessageWithArgumentDTO param) throws Exception {
        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());
        requireNotReturnMessage(astahMessage, "argument");

        txnAstah.run( () -> {
            astahMessage.setArgument(param.argument());
        });

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    private MessageDTO setGuard(MessageWithGuardDTO param) throws Exception {
        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());
        requireNotReturnMessage(astahMessage, "guard");

        txnAstah.run( () -> {
            astahMessage.setGuard(param.guard());
        });

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    private MessageDTO setReturnValue(MessageWithReturnValueDTO param) throws Exception {
        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());
        requireNotReturnMessage(astahMessage, "return value");

        txnAstah.run( () -> {
            astahMessage.setReturnValue(param.returnValue());
        });

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    private MessageDTO setReturnValueVariable(MessageWithReturnValueVariableDTO param) throws Exception {
        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());
        requireNotReturnMessage(astahMessage, "return value variable");

        txnAstah.run( () -> {
            astahMessage.setReturnValueVariable(param.returnValueVariable());
        });

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    private MessageDTO setAsynchronous(MessageWithAsynchronousDTO param) throws Exception {
        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());
        requireNotReturnMessage(astahMessage, "asynchrony");

        txnAstah.run( () -> {
            astahMessage.setAsynchronous(param.isAsynchronous());
        });

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    private MessageDTO setOperation(MessageWithOperationDTO param) throws Exception {
        IMessage astahMessage = astahProToolSupport.getMessage(param.targetMessageId());
        IOperation astahOperation = astahProToolSupport.getOperation(param.operationId());
        requireNotReturnMessage(astahMessage, "operation");

        txnAstah.run( () -> {
            try {
                astahMessage.setOperation(astahOperation);
            } catch (InvalidEditingException e) {
                if (!InvalidEditingException.ILLEGALMODELTYPE_ERROR_KEY.equals(e.getKey())) {
                    throw e;
                } else {
                    throw new IllegalArgumentException(String.format(
                        "Message '%s' (ID: %s) is not received by a lifeline (for example, it is a lost message), so no operation can be set on it. An operation can be set only on a message whose receiver is a lifeline.",
                        astahMessage.getName(), astahMessage.getId()));
                }
            }
        });

        return MessageDTOAssembler.toDTO(astahMessage);
    }

    // Astah rejects every setter of a return message with "Please check the model type.", which tells neither the cause nor what to specify instead.
    private static void requireNotReturnMessage(IMessage astahMessage, String property) {
        if (!astahMessage.isReturnMessage()) {
            return;
        }

        IMessage callMessage = astahMessage.getActivator();
        String instead = (callMessage == null) ? "" : String.format(
            " To set it on the call, specify the message it returns from instead: '%s' (ID: %s).",
            callMessage.getName(), callMessage.getId());

        throw new IllegalArgumentException(String.format(
            "Message '%s' (ID: %s) is a return message, and Astah does not allow the %s of a return message to be set.%s To change the label shown on the return message, set its name with set_name.",
            astahMessage.getName(), astahMessage.getId(), property, instead));
    }
}
