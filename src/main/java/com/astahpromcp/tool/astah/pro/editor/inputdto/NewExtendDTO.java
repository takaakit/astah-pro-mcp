package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewExtendDTO(
    @JsonPropertyDescription("Usecase identifier")
    String usecaseId,

    @JsonPropertyDescription("Extended usecase identifier")
    String extendedUsecaseId,

    @JsonPropertyDescription("New extend name")
    String newExtendName
) {
}
