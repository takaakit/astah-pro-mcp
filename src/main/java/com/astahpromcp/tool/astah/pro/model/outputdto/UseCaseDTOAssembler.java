package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IExtend;
import com.change_vision.jude.api.inf.model.IExtentionPoint;
import com.change_vision.jude.api.inf.model.IInclude;
import com.change_vision.jude.api.inf.model.IUseCase;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class UseCaseDTOAssembler {
    public static UseCaseDTO toDTO(@NonNull IUseCase astahUseCase) throws Exception {

        // Includes
        List<NameIdTypeDTO> includes = new ArrayList<>();
        for (IInclude include : astahUseCase.getIncludes()) {
            includes.add(NameIdTypeDTOAssembler.toDTO(include));
        }
        
        // Inverse includes
        List<NameIdTypeDTO> inverseIncludes = new ArrayList<>();
        for (IInclude inverseInclude : astahUseCase.getAdditionInvs()) {
            inverseIncludes.add(NameIdTypeDTOAssembler.toDTO(inverseInclude));
        }
        
        // Extends
        List<NameIdTypeDTO> extends_ = new ArrayList<>();
        for (IExtend extend : astahUseCase.getExtends()) {
            extends_.add(NameIdTypeDTOAssembler.toDTO(extend));
        }
        
        // Inverse extends
        List<NameIdTypeDTO> inverseExtends = new ArrayList<>();
        for (IExtend inverseExtend : astahUseCase.getExtendedCaseInvs()) {
            inverseExtends.add(NameIdTypeDTOAssembler.toDTO(inverseExtend));
        }
        
        // Extension points
        List<NameIdTypeDTO> extensionPoints = new ArrayList<>();
        for (IExtentionPoint extensionPoint : astahUseCase.getExtensionPoints()) {
            extensionPoints.add(NameIdTypeDTOAssembler.toDTO(extensionPoint));
        }
        
        return new UseCaseDTO(
            ClassDTOAssembler.toDTO(astahUseCase),
            includes,
            inverseIncludes,
            extends_,
            inverseExtends,
            extensionPoints);
    }
}
