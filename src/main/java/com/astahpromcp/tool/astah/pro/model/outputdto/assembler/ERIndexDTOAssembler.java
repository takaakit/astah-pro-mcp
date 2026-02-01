package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.model.IEREntity;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERIndexDTO;

import java.util.ArrayList;
import java.util.List;

public class ERIndexDTOAssembler {
    public static ERIndexDTO toDTO(@NonNull IERIndex astahERIndex) throws Exception {
        
        List<NameIdTypeDTO> attributesDTOs = new ArrayList<>();
        for (IERAttribute astahERAttribute : astahERIndex.getERAttributes()) {
            attributesDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERAttribute));
        }
        
        List<NameIdTypeDTO> erRelationshipsDTOs = new ArrayList<>();
        for (IERRelationship astahERRelationship : astahERIndex.getERRelationships()) {
            erRelationshipsDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERRelationship));
        }
        
        NameIdTypeDTO parentEREntityDTO;
        if (astahERIndex.getParentEREntity() != null) {
            parentEREntityDTO = NameIdTypeDTOAssembler.toDTO(astahERIndex.getParentEREntity());
        } else {
            parentEREntityDTO = NameIdTypeDTO.empty();
        }
        
        return new ERIndexDTO(
            NamedElementDTOAssembler.toDTO(astahERIndex),
            attributesDTOs,
            erRelationshipsDTOs,
            parentEREntityDTO,
            astahERIndex.getKind(),
            astahERIndex.isKey(),
            astahERIndex.isUnique());
    }
}
