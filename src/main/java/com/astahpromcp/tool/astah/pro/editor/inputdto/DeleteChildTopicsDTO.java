package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DeleteChildTopicsDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target parent topic (node presentation) identifier whose child topics are to be deleted")
    String targetParentTopicId
) {
}
