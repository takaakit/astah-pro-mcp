package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TransitionDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Source vertex")
    NameIdTypeDTO sourceVertex,

    @JsonPropertyDescription("Target vertex")
    NameIdTypeDTO targetVertex,

    @JsonPropertyDescription("Action")
    String action,

    @JsonPropertyDescription("Event")
    String event,

    @JsonPropertyDescription("Guard condition")
    String guard
) {
}
