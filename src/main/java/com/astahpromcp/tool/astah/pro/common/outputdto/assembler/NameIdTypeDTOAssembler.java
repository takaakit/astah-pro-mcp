package com.astahpromcp.tool.astah.pro.common.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;

public class NameIdTypeDTOAssembler {

    public static final String PRIMITIVE_TYPE_NAME = "PrimitiveType";

    public static NameIdTypeDTO toDTO(@NonNull INamedElement namedElement) throws Exception {

        return new NameIdTypeDTO(
            namedElement.getName(),
            namedElement.getId(),
            NamedElementDTO.Type.getCorrespondingType(namedElement).typeName);
    }

    public static NameIdTypeDTO toPrimitiveTypeDTO(@NonNull INamedElement primitiveType) throws Exception {

        return new NameIdTypeDTO(
            primitiveType.getName(),
            primitiveType.getId(),
            PRIMITIVE_TYPE_NAME);
    }
}
