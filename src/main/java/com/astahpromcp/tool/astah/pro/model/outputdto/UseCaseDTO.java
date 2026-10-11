package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record UseCaseDTO(
    @JsonPropertyDescription("Class info")
    @JsonProperty("class")
    ClassDTO class_,   // For avoiding Java keyword collision

    @JsonPropertyDescription("Includes: Includes in which this use case includes the other.")
    List<NameIdTypeDTO> includes,

    @JsonPropertyDescription("Inverse includes: Includes in which the other use case includes this one.")
    List<NameIdTypeDTO> inverseIncludes,

    @JsonPropertyDescription("Extends: Extends in which this use case extends the other.")
    @JsonProperty("extends")
    List<NameIdTypeDTO> extends_,   // For avoiding Java keyword collision

    @JsonPropertyDescription("Inverse extends: Extends in which the other use case extends this one.")
    List<NameIdTypeDTO> inverseExtends,

    @JsonPropertyDescription("Extension points")
    List<NameIdTypeDTO> extensionPoints
) {
}
