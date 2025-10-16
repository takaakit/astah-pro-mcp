package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.IAssociationClass;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssociationClassDTOAssembler {
    
    public static AssociationClassDTO toDTO(@NonNull IAssociationClass associationClass) throws Exception {
        return new AssociationClassDTO(
                ClassDTOAssembler.toDTO(associationClass),
                AssociationDTOAssembler.toDTO(associationClass));
    }
}
