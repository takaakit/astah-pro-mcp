package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ParameterWithTypeDTO(
    @JsonPropertyDescription("Target parameter identifier")
    String targetParameterId,

    @JsonPropertyDescription("Parameter type identifier")
    String parameterTypeId
) {
}
