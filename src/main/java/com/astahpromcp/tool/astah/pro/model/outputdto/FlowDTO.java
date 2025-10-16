package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record FlowDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Source activity node")
    NameIdTypeDTO sourceActivityNode,

    @JsonPropertyDescription("Target activity node")
    NameIdTypeDTO targetActivityNode,

    @JsonPropertyDescription("Action")
    String action,

    @JsonPropertyDescription("Guard condition")
    String guard
) {
}
