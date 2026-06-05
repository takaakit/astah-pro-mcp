package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewExtendDTO(
    @JsonPropertyDescription("Extending usecase identifier")
    String extendingUsecaseId,

    @JsonPropertyDescription("Extended usecase identifier")
    String extendedUsecaseId,

    @JsonPropertyDescription("New extend name")
    String newExtendName
) {
}
