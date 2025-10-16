package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record UseCaseDTO(
    @JsonPropertyDescription("Class information")
    ClassDTO clazz,

    @JsonPropertyDescription("Includes")
    List<NameIdTypeDTO> includes,

    @JsonPropertyDescription("Inverse includes")
    List<NameIdTypeDTO> inverseIncludes,

    @JsonPropertyDescription("Extends")
    List<NameIdTypeDTO> extends_,

    @JsonPropertyDescription("Inverse extends")
    List<NameIdTypeDTO> inverseExtends,

    @JsonPropertyDescription("Extension points")
    List<NameIdTypeDTO> extensionPoints
) {
}
