package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERDatatypeDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Length/Precision as default. An empty string in case there is none.")
    String defaultLengthPrecision,

    @JsonPropertyDescription("Description")
    String description,

    @JsonPropertyDescription("Length constraint. An empty string in case there is none.")
    String lengthConstraint,

    @JsonPropertyDescription("Precision constraint. An empty string in case there is none.")
    String precisionConstraint
) {
}
