package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPartitionDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("Super partition presentation identifier. Set an empty string for a top-level partition, or to place the new partition next to the previous partition.")
    String superPartitionId,

    @JsonPropertyDescription("Presentation identifier of the partition right before the new partition. It must be a sub partition of the super partition. Set an empty string to append the new partition after the last one.")
    String previousPartitionId,

    @JsonPropertyDescription("New partition name")
    String newPartitionName,

    @JsonPropertyDescription("Is horizontal (True: Horizontal partition / False: Vertical partition)")
    boolean isHorizontal
) {
}
