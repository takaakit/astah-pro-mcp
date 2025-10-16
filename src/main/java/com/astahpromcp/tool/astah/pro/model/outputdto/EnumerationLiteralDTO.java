package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record EnumerationLiteralDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElementDTO,

    @JsonPropertyDescription("Enumeration literal value")
    String value
) {
}
