package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NameIdTypeDefinitionDTO(
    @JsonPropertyDescription("Name")
    String name,

    @JsonPropertyDescription("Identifier")
    String id,

    @JsonPropertyDescription("Type")
    String type,

    @JsonPropertyDescription("Definition")
    String definition
) {
}
