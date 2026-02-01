package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERDomain;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDomainDTO;

import java.util.ArrayList;
import java.util.List;

public class ERDomainDTOAssembler {
    public static ERDomainDTO toDTO(@NonNull IERDomain astahERDomain) throws Exception {
        
        List<NameIdTypeDTO> childERDomainsDTOs = new ArrayList<>();
        for (IERDomain astahChildDomain : astahERDomain.getChildren()) {
            childERDomainsDTOs.add(NameIdTypeDTOAssembler.toDTO(astahChildDomain));
        }
        
        return new ERDomainDTO(
            NamedElementDTOAssembler.toDTO(astahERDomain),
            childERDomainsDTOs,
            astahERDomain.getDatatypeName(),
            astahERDomain.getDefaultValue(),
            astahERDomain.getLengthPrecision(),
            astahERDomain.getLogicalName(),
            astahERDomain.getPhysicalName(),
            astahERDomain.isNotNull());
    }
}
