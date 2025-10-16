package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NameIdTypeDTO(
    @JsonPropertyDescription("Name")
    String name,

    @JsonPropertyDescription("Identifier")
    String id,

    @JsonPropertyDescription("Type")
    String type
) {
    public static NameIdTypeDTO empty() {
        return new NameIdTypeDTO(
            "",
            "",
            "");
    }
}
