package com.astahpromcp.tool.astah.pro.project.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SearchDTO(
    @JsonPropertyDescription("Search string")
    String searchString
) {
}
