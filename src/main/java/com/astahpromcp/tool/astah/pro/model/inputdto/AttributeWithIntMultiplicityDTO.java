package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AttributeWithIntMultiplicityDTO(
    @JsonPropertyDescription("Target attribute identifier")
    String targetAttributeId,
    
    @JsonPropertyDescription("Lower integer value")
    int lowerIntValue,
    
    @JsonPropertyDescription("Upper integer value")
    int upperIntValue
) {
}
