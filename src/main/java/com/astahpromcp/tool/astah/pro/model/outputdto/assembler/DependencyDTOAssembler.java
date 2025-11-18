package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IDependency;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.DependencyDTO;

public class DependencyDTOAssembler {
    public static DependencyDTO toDTO(@NonNull IDependency astahDependency) throws Exception {

        return new DependencyDTO(
            NamedElementDTOAssembler.toDTO(astahDependency),
            NameIdTypeDTOAssembler.toDTO(astahDependency.getClient()),
            NameIdTypeDTOAssembler.toDTO(astahDependency.getSupplier()));
    }
}
