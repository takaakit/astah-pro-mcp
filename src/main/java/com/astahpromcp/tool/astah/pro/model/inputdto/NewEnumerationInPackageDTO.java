package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewEnumerationInPackageDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,

    @JsonPropertyDescription("New enumeration name")
    String newEnumerationName
) {
}
