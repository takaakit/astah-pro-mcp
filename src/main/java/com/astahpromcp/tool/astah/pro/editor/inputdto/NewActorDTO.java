package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewActorDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,

    @JsonPropertyDescription("New actor name")
    String newActorName
) {
}
