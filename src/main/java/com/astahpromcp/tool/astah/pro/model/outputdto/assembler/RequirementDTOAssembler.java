package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IRequirement;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.RequirementDTO;

public class RequirementDTOAssembler {
    public static RequirementDTO toDTO(@NonNull IRequirement astahRequirement) throws Exception {
        return new RequirementDTO(
            ClassDTOAssembler.toDTO(astahRequirement),
            astahRequirement.getRequirementID(),
            astahRequirement.getRequirementText());
    }
}
