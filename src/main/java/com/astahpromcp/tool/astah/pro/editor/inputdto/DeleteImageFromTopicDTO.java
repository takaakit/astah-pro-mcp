package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DeleteImageFromTopicDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to delete the image from")
    String targetTopicId
) {
}
