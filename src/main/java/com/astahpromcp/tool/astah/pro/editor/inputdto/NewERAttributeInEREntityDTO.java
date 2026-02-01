package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewERAttributeInEREntityDTO(
    @JsonPropertyDescription("Target ER entity identifier")
    String targetEREntityId,

    @JsonPropertyDescription("New ER attribute logical name")
    String newERAttributeLogicalName,

    @JsonPropertyDescription("New ER attribute physical name")
    String newERAttributePhysicalName,

    @JsonPropertyDescription("ER datatype identifier")
    String erDatatypeId
) {
}
