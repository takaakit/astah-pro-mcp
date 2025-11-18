package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IParameter;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ParameterDTO;

public class ParameterDTOAssembler {
    public static ParameterDTO toDTO(@NonNull IParameter astahParameter) throws Exception {

        NameIdTypeDTO type;
        if (astahParameter.getType() != null) {
            type = NameIdTypeDTOAssembler.toDTO(astahParameter.getType());
        } else {
            type = NameIdTypeDTO.empty();
        }
        
        return new ParameterDTO(
            NamedElementDTOAssembler.toDTO(astahParameter),
            type,
            astahParameter.getTypeExpression());
    }
}
