package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewERDatatypeInERModelDTO(
    @JsonPropertyDescription("Target ER model identifier")
    String targetERModelId,

    @JsonPropertyDescription("New ER datatype name")
    String newERDatatypeName
) {
}
