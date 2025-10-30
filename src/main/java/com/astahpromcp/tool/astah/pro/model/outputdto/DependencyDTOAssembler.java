package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IDependency;
import lombok.NonNull;

public class DependencyDTOAssembler {
    public static DependencyDTO toDTO(@NonNull IDependency astahDependency) throws Exception {

        return new DependencyDTO(
            NamedElementDTOAssembler.toDTO(astahDependency),
            NameIdTypeDTOAssembler.toDTO(astahDependency.getClient()),
            NameIdTypeDTOAssembler.toDTO(astahDependency.getSupplier()));
    }
}
