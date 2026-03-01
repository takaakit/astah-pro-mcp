package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewLinkBetweenTopicsDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Source topic (node presentation) identifier")
    String sourceTopicId,

    @JsonPropertyDescription("Target topic (node presentation) identifier")
    String targetTopicId
) {
}
