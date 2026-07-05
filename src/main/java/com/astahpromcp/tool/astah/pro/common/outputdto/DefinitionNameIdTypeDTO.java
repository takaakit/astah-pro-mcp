package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DefinitionNameIdTypeDTO(
    @JsonPropertyDescription("Definition")
    String definition,

    @JsonPropertyDescription("Name")
    String name,

    @JsonPropertyDescription("Identifier")
    String id,

    @JsonPropertyDescription("Type")
    String type
) {
    public static DefinitionNameIdTypeDTO empty() {
        return new DefinitionNameIdTypeDTO(
            "",
            "",
            "",
            "");
    }
}
