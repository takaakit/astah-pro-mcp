package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ConstraintDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Constrained elements")
    List<ElementDTO> constrainedElements,

    @JsonPropertyDescription("Specification contents")
    String specificationContents
) {
}
