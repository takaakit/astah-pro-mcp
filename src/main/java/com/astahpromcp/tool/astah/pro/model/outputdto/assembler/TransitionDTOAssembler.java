package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ITransition;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.TransitionDTO;

public class TransitionDTOAssembler {
    public static TransitionDTO toDTO(@NonNull ITransition astahTransition) throws Exception {
        return new TransitionDTO(
            NamedElementDTOAssembler.toDTO(astahTransition),
            NameIdTypeDTOAssembler.toDTO(astahTransition.getSource()),
            NameIdTypeDTOAssembler.toDTO(astahTransition.getTarget()),
            astahTransition.getAction(),
            astahTransition.getEvent(),
            astahTransition.getGuard());
    }
}
