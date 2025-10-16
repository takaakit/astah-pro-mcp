package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageDTOAssembler {
    public static MessageDTO toDTO(@NonNull IMessage astahMessage) throws Exception {

        NameIdTypeDTO activator;
        if (astahMessage.getActivator() != null) {
            activator = NameIdTypeDTOAssembler.toDTO(astahMessage.getActivator());
        } else {
            activator = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO successor;
        if (astahMessage.getSuccessor() != null) {
            successor = NameIdTypeDTOAssembler.toDTO(astahMessage.getSuccessor());
        } else {
            successor = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO source;
        if (astahMessage.getSource() != null) {
            source = NameIdTypeDTOAssembler.toDTO(astahMessage.getSource());
        } else {
            source = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO target;
        if (astahMessage.getTarget() != null) {
            target = NameIdTypeDTOAssembler.toDTO(astahMessage.getTarget());
        } else {
            target = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO operation;
        if (astahMessage.getOperation() != null) {
            operation = NameIdTypeDTOAssembler.toDTO(astahMessage.getOperation());
        } else {
            operation = NameIdTypeDTO.empty();
        }

        return new MessageDTO(
            NamedElementDTOAssembler.toDTO(astahMessage),
            astahMessage.getArgument(),
            astahMessage.getGuard(),
            astahMessage.getReturnValue(),
            astahMessage.getReturnValueVariable(),
            astahMessage.isAsynchronous(),
            astahMessage.isReturnMessage(),
            astahMessage.isSynchronous(),
            astahMessage.getIndex(),
            activator,
            successor,
            source,
            target,
            operation);
    }
}
