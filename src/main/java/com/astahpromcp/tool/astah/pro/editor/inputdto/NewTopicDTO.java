package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTopicDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Parent topic (node presentation) identifier. Must be a topic on the target mind map diagram.")
    String parentTopicId,

    @JsonPropertyDescription("New topic label")
    String newTopicLabel
) {
}
