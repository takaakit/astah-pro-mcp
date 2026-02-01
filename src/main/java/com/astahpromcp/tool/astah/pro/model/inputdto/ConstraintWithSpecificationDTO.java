package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ConstraintWithSpecificationDTO(
    @JsonPropertyDescription("Target constraint identifier")
    String id,

    @JsonPropertyDescription("Specification contents")
    String specificationContents
) {
}
