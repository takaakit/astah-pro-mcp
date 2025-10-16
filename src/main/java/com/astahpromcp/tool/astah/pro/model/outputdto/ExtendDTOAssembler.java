package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IExtend;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtendDTOAssembler {
    public static ExtendDTO toDTO(@NonNull IExtend astahExtend) throws Exception {
        // Named element information
        NamedElementDTO namedElement = NamedElementDTOAssembler.toDTO(astahExtend);
        
        // Extended UseCase
        NameIdTypeDTO extendedUseCase;
        if (astahExtend.getExtendedCase() != null) {
            extendedUseCase = NameIdTypeDTOAssembler.toDTO(astahExtend.getExtendedCase());
        } else {
            extendedUseCase = NameIdTypeDTO.empty();
        }
        
        // Extending UseCase
        NameIdTypeDTO extendingUseCase;
        if (astahExtend.getExtension() != null) {
            extendingUseCase = NameIdTypeDTOAssembler.toDTO(astahExtend.getExtension());
        } else {
            extendingUseCase = NameIdTypeDTO.empty();
        }
        
        return new ExtendDTO(
            namedElement,
            extendedUseCase,
            extendingUseCase);
    }
}
