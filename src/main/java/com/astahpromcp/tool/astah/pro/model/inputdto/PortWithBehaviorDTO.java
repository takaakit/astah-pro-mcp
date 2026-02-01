package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PortWithBehaviorDTO(
    @JsonPropertyDescription("Target port identifier")
    String targetPortId,

    @JsonPropertyDescription("Is Behavior. If true, it is behavior, otherwise, it is not behavior.")
    boolean isBehavior
) {
}
