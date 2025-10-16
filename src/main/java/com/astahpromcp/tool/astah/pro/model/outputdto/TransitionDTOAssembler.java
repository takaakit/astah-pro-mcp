package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ITransition;
import lombok.NonNull;

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
