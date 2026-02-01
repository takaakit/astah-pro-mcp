package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERAttributeWithPrimaryKeyDTO(
    @JsonPropertyDescription("Target ER attribute identifier")
    String targetERAttributeId,

    @JsonPropertyDescription("Is primary key. true to set a primary key, false to unset a primary key.")
    boolean isPrimaryKey
) {
}
