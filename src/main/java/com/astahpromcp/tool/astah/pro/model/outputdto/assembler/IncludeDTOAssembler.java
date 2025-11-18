package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IInclude;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.IncludeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;

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
