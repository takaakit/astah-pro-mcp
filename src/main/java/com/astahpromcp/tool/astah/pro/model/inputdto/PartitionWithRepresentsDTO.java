package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PartitionWithRepresentsDTO(
    @JsonPropertyDescription("Target partition identifier")
    String targetPartitionId,

    @JsonPropertyDescription("Identifier of the element that the partition represents.")
    String representsId
) {
}
