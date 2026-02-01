package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewNonIdentifyingRelationshipDTO(
    @JsonPropertyDescription("Parent ER entity identifier")
    String parentEREntityId,

    @JsonPropertyDescription("Child ER entity identifier")
    String childEREntityId,

    @JsonPropertyDescription("New relationship logical name")
    String newRelationshipLogicalName,

    @JsonPropertyDescription("New relationship physical name")
    String newRelationshipPhysicalName,

    @JsonPropertyDescription("ER index identifier (optional)")
    String erIndexId
) {
}
