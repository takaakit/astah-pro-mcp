package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IObjectNode;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ObjectNodeDTO;

public class ObjectNodeDTOAssembler {
    public static ObjectNodeDTO toDTO(@NonNull IObjectNode astahObjectNode) throws Exception {

        NameIdTypeDTO base;
        if (astahObjectNode.getBase() != null) {
            base = NameIdTypeDTOAssembler.toDTO(astahObjectNode.getBase());
        } else {
            base = NameIdTypeDTO.empty();
        }

        return new ObjectNodeDTO(
            ActivityNodeDTOAssembler.toDTO(astahObjectNode),
            base);
    }
}
