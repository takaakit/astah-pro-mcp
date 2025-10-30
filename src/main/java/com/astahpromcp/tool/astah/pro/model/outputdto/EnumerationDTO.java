package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record EnumerationDTO(
    @JsonPropertyDescription("Class information")
    ClassDTO classDTO,

    @JsonPropertyDescription("Enumeration literals")
    List<EnumerationLiteralDTO> enumerationLiterals
) {
}
