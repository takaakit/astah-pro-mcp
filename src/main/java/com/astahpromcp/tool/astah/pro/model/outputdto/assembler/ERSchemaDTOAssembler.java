package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IERSchema;
import com.change_vision.jude.api.inf.model.IERDomain;
import com.change_vision.jude.api.inf.model.IERDatatype;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERSchemaDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;

import java.util.ArrayList;
import java.util.List;

public class ERSchemaDTOAssembler {
    public static ERSchemaDTO toDTO(@NonNull IERSchema astahERSchema) throws Exception {
        
        List<NameIdTypeDTO> domainDTOs = new ArrayList<>();
        for (IERDomain astahERDomain : astahERSchema.getDomains()) {
            domainDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERDomain));
        }
        
        List<NameIdTypeDTO> datatypeDTOs = new ArrayList<>();
        for (IERDatatype astahERDatatype : astahERSchema.getDatatypes()) {
            datatypeDTOs.add(NameIdTypeDTOAssembler.toDTO(astahERDatatype));
        }
        
        return new ERSchemaDTO(
            ERPackageDTOAssembler.toDTO(astahERSchema),
            domainDTOs,
            datatypeDTOs);
    }
}
