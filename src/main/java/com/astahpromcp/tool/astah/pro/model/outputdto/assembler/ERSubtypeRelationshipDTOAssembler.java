package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERSubtypeRelationship;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.model.IERAttribute;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERSubtypeRelationshipDTO;

import java.util.ArrayList;
import java.util.List;

public class ERSubtypeRelationshipDTOAssembler {
    public static ERSubtypeRelationshipDTO toDTO(@NonNull IERSubtypeRelationship astahERSubtypeRelationship) throws Exception {
        
        NameIdTypeDTO parentEntityDTO;
        if (astahERSubtypeRelationship.getParent() != null) {
            parentEntityDTO = NameIdTypeDTOAssembler.toDTO(astahERSubtypeRelationship.getParent());
        } else {
            parentEntityDTO = NameIdTypeDTO.empty();
        }
        
        NameIdTypeDTO childEntityDTO;
        if (astahERSubtypeRelationship.getChild() != null) {
            childEntityDTO = NameIdTypeDTOAssembler.toDTO(astahERSubtypeRelationship.getChild());
        } else {
            childEntityDTO = NameIdTypeDTO.empty();
        }
        
        NameIdTypeDTO identifierAttributeDTO;
        if (astahERSubtypeRelationship.getDiscriminatorAttribute() != null) {
            identifierAttributeDTO = NameIdTypeDTOAssembler.toDTO(astahERSubtypeRelationship.getDiscriminatorAttribute());
        } else {
            identifierAttributeDTO = NameIdTypeDTO.empty();
        }
        
        List<NameIdTypeDTO> foreignKeysDTOs = new ArrayList<>();
        for (IERAttribute astahERAttribute : astahERSubtypeRelationship.getForeignKeys()) {
            foreignKeysDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERAttribute));
        }
        
        return new ERSubtypeRelationshipDTO(
            NamedElementDTOAssembler.toDTO(astahERSubtypeRelationship),
            parentEntityDTO,
            childEntityDTO,
            identifierAttributeDTO,
            foreignKeysDTOs,
            astahERSubtypeRelationship.getLogicalName(),
            astahERSubtypeRelationship.getPhysicalName(),
            astahERSubtypeRelationship.isConclusive());
    }
}
