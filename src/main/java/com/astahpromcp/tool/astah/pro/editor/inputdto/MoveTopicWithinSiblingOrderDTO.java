package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record MoveTopicWithinSiblingOrderDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to move")
    String targetTopicId,

    @JsonPropertyDescription("New index within sibling topics (0-based)")
    int newSiblingIndex
) {
}
