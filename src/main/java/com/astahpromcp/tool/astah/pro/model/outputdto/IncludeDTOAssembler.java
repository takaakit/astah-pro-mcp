package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IInclude;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IncludeDTOAssembler {
    public static IncludeDTO toDTO(@NonNull IInclude astahInclude) throws Exception {
        // Named element information
        NamedElementDTO namedElement = NamedElementDTOAssembler.toDTO(astahInclude);
        
        // Including UseCase
        NameIdTypeDTO includingUseCase;
        if (astahInclude.getIncludingCase() != null) {
            includingUseCase = NameIdTypeDTOAssembler.toDTO(astahInclude.getIncludingCase());
        } else {
            includingUseCase = NameIdTypeDTO.empty();
        }
        
        // Included UseCase
        NameIdTypeDTO includedUseCase;
        if (astahInclude.getAddition() != null) {
            includedUseCase = NameIdTypeDTOAssembler.toDTO(astahInclude.getAddition());
        } else {
            includedUseCase = NameIdTypeDTO.empty();
        }
        
        return new IncludeDTO(
            namedElement,
            includingUseCase,
            includedUseCase);
    }
}
