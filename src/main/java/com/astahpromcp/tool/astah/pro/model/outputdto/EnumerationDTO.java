package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record EnumerationDTO(
    @JsonPropertyDescription("Class information")
    ClassDTO classDTO,

    @JsonPropertyDescription("Enumeration literals")
    List<EnumerationLiteralDTO> enumerationLiterals
) {
}
