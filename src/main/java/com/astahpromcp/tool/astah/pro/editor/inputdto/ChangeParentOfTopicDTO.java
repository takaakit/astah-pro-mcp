package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ChangeParentOfTopicDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to change the parent of")
    String targetTopicId,

    @JsonPropertyDescription("New parent topic (node presentation) identifier")
    String newParentTopicId
) {
}
