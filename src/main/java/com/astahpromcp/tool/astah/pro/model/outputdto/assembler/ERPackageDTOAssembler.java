package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERPackage;
import com.change_vision.jude.api.inf.model.IEREntity;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERPackageDTO;

import java.util.ArrayList;
import java.util.List;

public class ERPackageDTOAssembler {
    public static ERPackageDTO toDTO(@NonNull IERPackage astahERPackage) throws Exception {
        
        List<NameIdTypeDTO> entitiesDTOs = new ArrayList<>();
        for (IEREntity astahEREntity : astahERPackage.getEntities()) {
            entitiesDTOs.add(NameIdTypeDTOAssembler.toDTO(astahEREntity));
        }
        
        return new ERPackageDTO(
            PackageDTOAssembler.toDTO(astahERPackage),
            entitiesDTOs);
    }
}
