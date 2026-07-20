package com.astahpromcp.tool.astah.pro.project.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SearchDTO(
    @JsonPropertyDescription("Search string. Specify an empty string to target all.")
    String searchString
) {
}
