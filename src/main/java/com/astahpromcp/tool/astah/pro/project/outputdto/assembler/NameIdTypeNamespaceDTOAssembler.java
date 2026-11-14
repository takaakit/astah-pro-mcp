package com.astahpromcp.tool.astah.pro.project.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.AstahElementType;
import com.astahpromcp.tool.astah.pro.project.outputdto.NameIdTypeNamespaceDTO;
import com.change_vision.jude.api.inf.model.IClass;
import lombok.NonNull;

public class NameIdTypeNamespaceDTOAssembler {
    public static NameIdTypeNamespaceDTO toDTO(@NonNull IClass astahClass) throws Exception {
        return new NameIdTypeNamespaceDTO(
            astahClass.getName(),
            astahClass.getId(),
            AstahElementType.getCorrespondingType(astahClass).typeName,
            astahClass.getFullNamespace("."));
    }
}
