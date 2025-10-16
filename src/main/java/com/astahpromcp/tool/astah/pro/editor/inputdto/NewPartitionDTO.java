package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPartitionDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("Super partition identifier. In the absence of a super partition, set an empty string.")
    String superPartitionId,

    @JsonPropertyDescription("Previous partition identifier. In the absence of a previous partition, set an empty string.")
    String previousPartitionId,

    @JsonPropertyDescription("New partition name")
    String newPartitionName,

    @JsonPropertyDescription("Is horizontal (True: Horizontal partition / False: Vertical partition)")
    boolean isHorizontal
) {
}
