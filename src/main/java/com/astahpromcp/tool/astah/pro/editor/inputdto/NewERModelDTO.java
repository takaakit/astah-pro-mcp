package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewERModelDTO(
    @JsonPropertyDescription("New ER model name")
    String newERModelName
) {
}
