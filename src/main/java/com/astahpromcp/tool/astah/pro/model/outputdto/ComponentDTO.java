package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ComponentDTO(
    @JsonPropertyDescription("Class information")
    ClassDTO clazz
) {
}
