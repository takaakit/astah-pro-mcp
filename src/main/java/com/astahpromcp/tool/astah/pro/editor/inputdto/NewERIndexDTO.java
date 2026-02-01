package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NewERIndexDTO(
    @JsonPropertyDescription("New ER index name")
    String newERIndexName,

    @JsonPropertyDescription("Parent ER entity identifier")
    String parentEREntityId,

    @JsonPropertyDescription("Unique flag for ER index")
    boolean unique,

    @JsonPropertyDescription("Key flag for ER index")
    boolean key,

    @JsonPropertyDescription("Target ER attribute identifiers")
    List<String> targetERAttributeIds
) {
}
