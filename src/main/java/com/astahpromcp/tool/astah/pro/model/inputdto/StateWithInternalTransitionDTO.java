package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StateWithInternalTransitionDTO(
    @JsonPropertyDescription("Target state ID")
    String targetStateId,

    @JsonPropertyDescription("Event")
    String event,

    @JsonPropertyDescription("Guard")
    String guard,

    @JsonPropertyDescription("Action")
    String action
) {
}
