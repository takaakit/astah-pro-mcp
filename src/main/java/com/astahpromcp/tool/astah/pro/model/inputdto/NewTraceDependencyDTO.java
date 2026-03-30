package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTraceDependencyDTO(
    @JsonPropertyDescription("Source named element identifier")
    String sourceNamedElementId,
    
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId
) {
}
