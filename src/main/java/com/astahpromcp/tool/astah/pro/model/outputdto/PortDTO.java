package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PortDTO(
    @JsonPropertyDescription("Attribute information")
    AttributeDTO attribute,

    @JsonPropertyDescription("Is Behavior")
    boolean isBehavior,

    @JsonPropertyDescription("Is Service")
    boolean isService
) {
}
