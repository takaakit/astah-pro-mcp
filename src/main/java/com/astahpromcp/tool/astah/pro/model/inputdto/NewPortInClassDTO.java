package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPortInClassDTO(
    @JsonPropertyDescription("Parent class identifier")
    String parentClassId,

    @JsonPropertyDescription("New port name")
    String newPortName
) {
}
