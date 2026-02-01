package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.AttributeDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.PortDTO;
import com.change_vision.jude.api.inf.model.IPort;
import lombok.NonNull;

public class PortDTOAssembler {
    public static PortDTO toDTO(@NonNull IPort astahPort) throws Exception {
        return new PortDTO(
            AttributeDTOAssembler.toDTO(astahPort),
            astahPort.isBehavior(),
            astahPort.isService());
    }
}
