package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewInterfaceInClassDTO(
    @JsonPropertyDescription("Parent class identifier")
    String parentClassId,

    @JsonPropertyDescription("New interface name")
    String newInterfaceName
) {
}
