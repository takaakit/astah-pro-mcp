package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.IRequirement;
import lombok.NonNull;

public class RequirementDTOAssembler {
    public static RequirementDTO toDTO(@NonNull IRequirement astahRequirement) throws Exception {
        return new RequirementDTO(
            ClassDTOAssembler.toDTO(astahRequirement),
            astahRequirement.getRequirementID(),
            astahRequirement.getRequirementText());
    }
}
