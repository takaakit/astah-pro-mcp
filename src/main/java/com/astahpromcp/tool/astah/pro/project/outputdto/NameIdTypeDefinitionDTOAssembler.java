package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.project.outputdto.NameIdTypeDefinitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.NonNull;

public class NameIdTypeDefinitionDTOAssembler {
    public static NameIdTypeDefinitionDTO toDTO(@NonNull INamedElement namedElement) throws Exception {
        return new NameIdTypeDefinitionDTO(
            namedElement.getName(),
            namedElement.getId(),
            NamedElementDTO.Type.getCorrespondingType(namedElement).getTypeName(),
            namedElement.getDefinition());
    }
}
