package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record UseCaseDTO(
    @JsonPropertyDescription("Class info")
    @JsonProperty("class")
    ClassDTO class_,   // For avoiding Java keyword collision

    @JsonPropertyDescription("Includes")
    List<NameIdTypeDTO> includes,

    @JsonPropertyDescription("Inverse includes")
    List<NameIdTypeDTO> inverseIncludes,

    @JsonPropertyDescription("Extends")
    @JsonProperty("extends")
    List<NameIdTypeDTO> extends_,   // For avoiding Java keyword collision

    @JsonPropertyDescription("Inverse extends")
    List<NameIdTypeDTO> inverseExtends,

    @JsonPropertyDescription("Extension points")
    List<NameIdTypeDTO> extensionPoints
) {
}
