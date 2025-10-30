package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.change_vision.jude.api.inf.model.IClass;
import lombok.NonNull;

public class NameIdTypeNamespaceDTOAssembler {
    public static NameIdTypeNamespaceDTO toDTO(@NonNull IClass astahClass) throws Exception {
        return new NameIdTypeNamespaceDTO(
            astahClass.getName(),
            astahClass.getId(),
            NamedElementDTO.Type.getCorrespondingType(astahClass).getTypeName(),
            astahClass.getFullNamespace("."));
    }
}
