package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AssociationClassDTO(
    @JsonPropertyDescription("Class information")
    @JsonProperty("class")
    ClassDTO class_,    // For avoiding Java keyword collision

    @JsonPropertyDescription("Association information")
    AssociationDTO association
) {
}
