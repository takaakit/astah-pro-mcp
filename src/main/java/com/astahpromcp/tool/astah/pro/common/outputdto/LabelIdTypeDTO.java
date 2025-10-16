package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LabelIdTypeDTO(
    @JsonPropertyDescription("Label")
    String label,
    
    @JsonPropertyDescription("Identifier")
    String id,

    @JsonPropertyDescription("Type")
    String type
) {
}
