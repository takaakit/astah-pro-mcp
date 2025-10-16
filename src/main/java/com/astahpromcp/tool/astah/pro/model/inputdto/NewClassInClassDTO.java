package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewClassInClassDTO(
    @JsonPropertyDescription("Parent class identifier")
    String parentClassId,

    @JsonPropertyDescription("New class name")
    String newClassName
) {
}
