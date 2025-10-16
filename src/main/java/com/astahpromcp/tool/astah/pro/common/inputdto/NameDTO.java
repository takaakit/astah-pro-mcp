package com.astahpromcp.tool.astah.pro.common.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NameDTO(
    @JsonPropertyDescription("Name")
    String name
) {
}
