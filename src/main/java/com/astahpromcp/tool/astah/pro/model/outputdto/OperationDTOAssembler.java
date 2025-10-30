package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.model.IParameter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class OperationDTOAssembler {
    public static OperationDTO toDTO(@NonNull IOperation astahOperation) throws Exception {
        
        List<ParameterDTO> parameterDTOs = new ArrayList<>();
        for (IParameter parameter : astahOperation.getParameters()) {

            ParameterDTO parameterDTO = new ParameterDTO(
                NamedElementDTOAssembler.toDTO(parameter),
                NameIdTypeDTOAssembler.toDTO(parameter.getType()),
                parameter.getTypeExpression());

            parameterDTOs.add(parameterDTO);
        }

        NameIdTypeDTO returnType;
        if (astahOperation.getReturnType() != null) {
            returnType = NameIdTypeDTOAssembler.toDTO(astahOperation.getReturnType());
        } else {
            returnType = NameIdTypeDTO.empty();
        }

        return new OperationDTO(
            NamedElementDTOAssembler.toDTO(astahOperation),
            astahOperation.isAbstract(),
            astahOperation.isLeaf(),
            astahOperation.isStatic(),
            parameterDTOs,
            returnType,
            astahOperation.getReturnTypeExpression());
    }
}
