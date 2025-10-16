package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IFlow;
import lombok.NonNull;

public class FlowDTOAssembler {
    public static FlowDTO toDTO(@NonNull IFlow astahFlow) throws Exception {
        return new FlowDTO(
            NamedElementDTOAssembler.toDTO(astahFlow),
            NameIdTypeDTOAssembler.toDTO(astahFlow.getSource()),
            NameIdTypeDTOAssembler.toDTO(astahFlow.getTarget()),
            astahFlow.getAction(),
            astahFlow.getGuard());
    }
}
