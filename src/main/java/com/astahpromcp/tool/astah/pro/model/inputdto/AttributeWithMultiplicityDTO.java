package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AttributeWithMultiplicityDTO(
    @JsonPropertyDescription("Target attribute identifier")
    String targetAttributeId,
    
    @JsonPropertyDescription("Lower multiplicity")
    String lowerMultiplicity,
    
    @JsonPropertyDescription("Upper multiplicity")
    String upperMultiplicity
) {
}
