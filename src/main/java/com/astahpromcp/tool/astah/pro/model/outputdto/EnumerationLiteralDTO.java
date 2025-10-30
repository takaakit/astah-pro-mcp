package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EnumerationLiteralDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElementDTO,

    @JsonPropertyDescription("Enumeration literal value")
    String value
) {
}
