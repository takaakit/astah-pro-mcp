package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewUseCaseDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,

    @JsonPropertyDescription("New usecase name")
    String newUsecaseName
) {
}
