package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ObjectNodeDTO(
    @JsonPropertyDescription("Activity node information")
    ActivityNodeDTO activityNode,

    @JsonPropertyDescription("Base class")
    NameIdTypeDTO baseClass
) {
}
