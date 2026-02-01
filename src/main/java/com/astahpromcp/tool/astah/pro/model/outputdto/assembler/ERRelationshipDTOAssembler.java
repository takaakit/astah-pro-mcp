package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.model.IERAttribute;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERRelationshipDTO;

import java.util.ArrayList;
import java.util.List;

public class ERRelationshipDTOAssembler {
    public static ERRelationshipDTO toDTO(@NonNull IERRelationship astahERRelationship) throws Exception {
        
        NameIdTypeDTO parentEntityDTO;
        if (astahERRelationship.getParent() != null) {
            parentEntityDTO = NameIdTypeDTOAssembler.toDTO(astahERRelationship.getParent());
        } else {
            parentEntityDTO = NameIdTypeDTO.empty();
        }
        
        NameIdTypeDTO childEntityDTO;
        if (astahERRelationship.getChild() != null) {
            childEntityDTO = NameIdTypeDTOAssembler.toDTO(astahERRelationship.getChild());
        } else {
            childEntityDTO = NameIdTypeDTO.empty();
        }
        
        NameIdTypeDTO erIndexDTO;
        if (astahERRelationship.getERIndex() != null) {
            erIndexDTO = NameIdTypeDTOAssembler.toDTO(astahERRelationship.getERIndex());
        } else {
            erIndexDTO = NameIdTypeDTO.empty();
        }
        
        List<NameIdTypeDTO> foreignKeysDTOs = new ArrayList<>();
        for (IERAttribute astahERAttribute : astahERRelationship.getForeignKeys()) {
            foreignKeysDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERAttribute));
        }

        String verbPhraseChild;
        if (astahERRelationship.getVerbPhraseChild() != null) {
            verbPhraseChild = astahERRelationship.getVerbPhraseChild();
        } else {
            verbPhraseChild = "";
        }

        String verbPhraseParent;
        if (astahERRelationship.getVerbPhraseParent() != null) {
            verbPhraseParent = astahERRelationship.getVerbPhraseParent();
        } else {
            verbPhraseParent = "";
        }
        
        return new ERRelationshipDTO(
            NamedElementDTOAssembler.toDTO(astahERRelationship),
            parentEntityDTO,
            childEntityDTO,
            erIndexDTO,
            foreignKeysDTOs,
            astahERRelationship.getCardinality(),
            astahERRelationship.getLogicalName(),
            astahERRelationship.getPhysicalName(),
            verbPhraseChild,
            verbPhraseParent,
            astahERRelationship.isIdentifying(),
            astahERRelationship.isNonIdentifying(),
            astahERRelationship.isMultiToMulti(),
            astahERRelationship.isParentRequired());
    }
}
