package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.model.IERSubtypeRelationship;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.model.IERAttribute;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.EREntityDTO;

import java.util.ArrayList;
import java.util.List;

public class EREntityDTOAssembler {
    public static EREntityDTO toDTO(@NonNull IEREntity astahEREntity) throws Exception {
        
        List<NameIdTypeDTO> parentRelationshipsDTOs = new ArrayList<>();
        for (IERRelationship astahERRelationship : astahEREntity.getParentRelationships()) {
            parentRelationshipsDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERRelationship));
        }
        
        List<NameIdTypeDTO> parentSubtypeRelationshipsDTOs = new ArrayList<>();
        for (IERSubtypeRelationship astahERSubtypeRelationship : astahEREntity.getParentSubtypeRelationships()) {
            parentSubtypeRelationshipsDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERSubtypeRelationship));
        }
        
        List<NameIdTypeDTO> childRelationshipsDTOs = new ArrayList<>();
        for (IERRelationship astahERRelationship : astahEREntity.getChildrenRelationships()) {
            childRelationshipsDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERRelationship));
        }
        
        List<NameIdTypeDTO> childSubtypeRelationshipsDTOs = new ArrayList<>();
        for (IERSubtypeRelationship astahERSubtypeRelationship : astahEREntity.getChildrenSubtypeRelationships()) {
            childSubtypeRelationshipsDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERSubtypeRelationship));
        }
        
        List<NameIdTypeDTO> erIndicesDTOs = new ArrayList<>();
        for (IERIndex astahERIndex : astahEREntity.getERIndices()) {
            erIndicesDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERIndex));
        }
        
        List<NameIdTypeDTO> primaryKeysDTOs = new ArrayList<>();
        for (IERAttribute astahERAttribute : astahEREntity.getPrimaryKeys()) {
            primaryKeysDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERAttribute));
        }
        
        List<NameIdTypeDTO> foreignKeysDTOs = new ArrayList<>();
        for (IERAttribute astahERAttribute : astahEREntity.getForeignKeys()) {
            foreignKeysDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERAttribute));
        }
        
        List<NameIdTypeDTO> nonPrimaryKeysDTOs = new ArrayList<>();
        for (IERAttribute astahERAttribute : astahEREntity.getNonPrimaryKeys()) {
            nonPrimaryKeysDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERAttribute));
        }
        
        return new EREntityDTO(
            NamedElementDTOAssembler.toDTO(astahEREntity),
            parentRelationshipsDTOs,
            parentSubtypeRelationshipsDTOs,
            childRelationshipsDTOs,
            childSubtypeRelationshipsDTOs,
            erIndicesDTOs,
            primaryKeysDTOs,
            foreignKeysDTOs,
            nonPrimaryKeysDTOs,
            astahEREntity.getLogicalName(),
            astahEREntity.getPhysicalName(),
            astahEREntity.getType());
    }
}
