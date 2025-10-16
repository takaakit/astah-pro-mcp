package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewExtensionPointDTO(
    @JsonPropertyDescription("Target usecase identifier")
    String targetUsecaseId,

    @JsonPropertyDescription("New extension point name")
    String newExtensionPointName
) {
}
