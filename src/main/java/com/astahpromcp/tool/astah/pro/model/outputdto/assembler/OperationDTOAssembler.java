package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.model.IParameter;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ParameterDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OperationDTOAssembler {
    public static OperationDTO toDTO(@NonNull IOperation astahOperation) throws Exception {
        
        List<ParameterDTO> parameterDTOs = new ArrayList<>();
        for (IParameter parameter : astahOperation.getParameters()) {
            parameterDTOs.add(ParameterDTOAssembler.toDTO(parameter));
        }

        NameIdTypeDTO returnType;
        if (astahOperation.getReturnType() != null) {
            returnType = NameIdTypeDTOAssembler.toDTO(astahOperation.getReturnType());
        } else {
            returnType = NameIdTypeDTO.empty();
        }

        List<String> preconditions = new ArrayList<>();
        for (String preCondition : astahOperation.getPreConditions()) {
            preconditions.add(preCondition);
        }

        List<String> postconditions = new ArrayList<>();
        for (String postCondition : astahOperation.getPostConditions()) {
            postconditions.add(postCondition);
        }

        return new OperationDTO(
            NamedElementDTOAssembler.toDTO(astahOperation),
            astahOperation.isAbstract(),
            astahOperation.isLeaf(),
            astahOperation.isStatic(),
            parameterDTOs,
            returnType,
            Objects.requireNonNullElse(astahOperation.getReturnTypeExpression(), ""),
            preconditions,
            postconditions,
            Objects.requireNonNullElse(astahOperation.getBodyCondition(), ""));
    }
}
