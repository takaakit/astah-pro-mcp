package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IERModel;
import com.change_vision.jude.api.inf.model.IERSchema;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERModelDTO;

import java.util.ArrayList;
import java.util.List;

public class ERModelDTOAssembler {
    public static ERModelDTO toDTO(@NonNull IERModel astahERModel) throws Exception {
        
        List<NameIdTypeDTO> schemataDTOs = new ArrayList<>();
        for (IERSchema astahERSchema : astahERModel.getSchemata()) {
            schemataDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERSchema));
        }
        
        return new ERModelDTO(
            PackageDTOAssembler.toDTO(astahERModel),
            schemataDTOs);
    }
}
