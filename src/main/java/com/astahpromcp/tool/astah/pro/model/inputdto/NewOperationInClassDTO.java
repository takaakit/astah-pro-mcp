package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewOperationInClassDTO(
    @JsonPropertyDescription("Parent class identifier")
    String parentClassId,

    @JsonPropertyDescription("New operation name. '()' is appended to the operation name when it is displayed. Therefore, it is not necessary to add '()' at the end of the operation name.")
    String newOperationName
) {
}
