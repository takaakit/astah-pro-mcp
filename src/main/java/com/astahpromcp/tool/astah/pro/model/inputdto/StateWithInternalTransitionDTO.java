package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StateWithInternalTransitionDTO(
    @JsonPropertyDescription("Target state ID")
    String targetStateId,

    @JsonPropertyDescription("Event (trigger) of the internal transition. It must not be empty.")
    String event,

    @JsonPropertyDescription("Guard condition. '[]' is appended to the guard condition when it is displayed. Therefore, it is not necessary to add '[]' to the guard condition. Set an empty string for no guard.")
    String guard,

    @JsonPropertyDescription("Action. '/' is prepended to the action when it is displayed. Therefore, it is not necessary to add '/' to the action. Set an empty string for no action.")
    String action
) {
}
