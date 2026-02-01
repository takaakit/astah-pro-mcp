package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDatatypeWithDescriptionDTO(
    @JsonPropertyDescription("Target ER datatype identifier")
    String targetERDatatypeId,

    @JsonPropertyDescription("Description")
    String description
) {
}
