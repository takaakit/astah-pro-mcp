package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DefinitionIdTypeDTO(
    @JsonPropertyDescription("Definition")
    String definition,

    @JsonPropertyDescription("Identifier")
    String id,

    @JsonPropertyDescription("Type")
    String type
) {
    public static DefinitionIdTypeDTO empty() {
        return new DefinitionIdTypeDTO(
            "",
            "",
            "");
    }
}
