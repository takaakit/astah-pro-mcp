package com.astahpromcp.tool.astah.pro.common.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record IdDTO(
    @JsonPropertyDescription("Identifier")
    String id
) {
}
