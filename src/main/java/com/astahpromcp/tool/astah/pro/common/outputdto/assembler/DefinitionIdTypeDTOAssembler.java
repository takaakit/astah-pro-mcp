package com.astahpromcp.tool.astah.pro.common.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.common.outputdto.DefinitionIdTypeDTO;

public class DefinitionIdTypeDTOAssembler {
    public static DefinitionIdTypeDTO toDTO(@NonNull INamedElement namedElement) throws Exception {

        return new DefinitionIdTypeDTO(
            namedElement.getDefinition(),
            namedElement.getId(),
            NamedElementDTO.Type.getCorrespondingType(namedElement).typeName);
    }
}
