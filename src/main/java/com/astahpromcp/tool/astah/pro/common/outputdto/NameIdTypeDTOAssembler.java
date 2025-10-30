package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.NonNull;

public class NameIdTypeDTOAssembler {
    public static NameIdTypeDTO toDTO(@NonNull INamedElement namedElement) throws Exception {
        
        return new NameIdTypeDTO(
            namedElement.getName(),
            namedElement.getId(),
            NamedElementDTO.Type.getCorrespondingType(namedElement).getTypeName());
    }
}
