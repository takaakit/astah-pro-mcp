package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PackageDTOAssembler {
    public static PackageDTO toDTO(@NonNull IPackage astahPackage) throws Exception {
                
        List<NameIdTypeDTO> ownedElementNameIdTypes = new ArrayList<>();
        for (INamedElement ownedElement : astahPackage.getOwnedElements()) {
            ownedElementNameIdTypes.add(NameIdTypeDTOAssembler.toDTO(ownedElement));
        }
        
        return new PackageDTO(
                NamedElementDTOAssembler.toDTO(astahPackage),
                ownedElementNameIdTypes);
    }
}
