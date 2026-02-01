package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.model.IERSubtypeRelationship;
import com.change_vision.jude.api.inf.model.IConstraint;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERAttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDatatypeDTO;

import java.util.ArrayList;
import java.util.List;

public class ERAttributeDTOAssembler {
    public static ERAttributeDTO toDTO(@NonNull IERAttribute astahERAttribute) throws Exception {
        
        NameIdTypeDTO domainDTO;
        if (astahERAttribute.getDomain() != null) {
            domainDTO = NameIdTypeDTOAssembler.toDTO(astahERAttribute.getDomain());
        } else {
            domainDTO = NameIdTypeDTO.empty();
        }
        
        List<NameIdTypeDTO> erIndicesDTOs = new ArrayList<>();
        for (IERIndex astahERIndex : astahERAttribute.getERIndices()) {
            erIndicesDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERIndex));
        }
        
        NameIdTypeDTO referencedPrimaryKeyDTO;
        if (astahERAttribute.getReferencedPrimaryKey() != null) {
            referencedPrimaryKeyDTO = NameIdTypeDTOAssembler.toDTO(astahERAttribute.getReferencedPrimaryKey());
        } else {
            referencedPrimaryKeyDTO = NameIdTypeDTO.empty();
        }
        
        List<NameIdTypeDTO> referencedForeignKeysDTOs = new ArrayList<>();
        for (IERAttribute astahReferencedForeignKey : astahERAttribute.getReferencedForeignKeys()) {
            referencedForeignKeysDTOs.add(NameIdTypeDTOAssembler.toDTO(astahReferencedForeignKey));
        }
        
        NameIdTypeDTO referencedRelationshipDTO;
        if (astahERAttribute.getReferencedRelationship() != null) {
            referencedRelationshipDTO = NameIdTypeDTOAssembler.toDTO(astahERAttribute.getReferencedRelationship());
        } else {
            referencedRelationshipDTO = NameIdTypeDTO.empty();
        }
        
        List<NameIdTypeDTO> referencedSubtypeRelationshipsDTOs = new ArrayList<>();
        for (IERSubtypeRelationship astahERSubtypeRelationship : astahERAttribute.getReferencedSubtypeRelationships()) {
            referencedSubtypeRelationshipsDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERSubtypeRelationship));
        }
        
        NameIdTypeDTO associatedSubtypeRelationshipAsDiscriminatorAttributeDTO;
        if (astahERAttribute.getSubtypeForeignKeyInv() != null) {
            associatedSubtypeRelationshipAsDiscriminatorAttributeDTO = NameIdTypeDTOAssembler.toDTO(astahERAttribute.getSubtypeForeignKeyInv());
        } else {
            associatedSubtypeRelationshipAsDiscriminatorAttributeDTO = NameIdTypeDTO.empty();
        }
        
        return new ERAttributeDTO(
            NamedElementDTOAssembler.toDTO(astahERAttribute),
            ERDatatypeDTOAssembler.toDTO(astahERAttribute.getDatatype()),
            domainDTO,
            erIndicesDTOs,
            referencedPrimaryKeyDTO,
            referencedForeignKeysDTOs,
            referencedRelationshipDTO,
            referencedSubtypeRelationshipsDTOs,
            associatedSubtypeRelationshipAsDiscriminatorAttributeDTO,
            astahERAttribute.getDefaultValue(),
            astahERAttribute.getLengthPrecision(),
            astahERAttribute.getLogicalName(),
            astahERAttribute.getPhysicalName(),
            astahERAttribute.isPrimaryKey(),
            astahERAttribute.isForeignKey(),
            astahERAttribute.isNotNull());
    }
}
