package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewIncludeDTO(
    @JsonPropertyDescription("Including usecase identifier") String includingUsecaseId,

    @JsonPropertyDescription("Included usecase identifier") String includedUsecaseId,

    @JsonPropertyDescription("New include name")
    String newIncludeName
) {
}
