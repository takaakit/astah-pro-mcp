package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record BooleanDTO(
    @JsonPropertyDescription("Boolean value")
    boolean value
) {
}
