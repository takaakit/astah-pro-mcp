package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AssociationClassDTO(
    @JsonPropertyDescription("Class information")
    ClassDTO class_,

    @JsonPropertyDescription("Association information")
    AssociationDTO association
) {
}
