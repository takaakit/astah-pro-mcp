package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TopicWithBoundaryVisibilityDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to set the boundary visibility")
    String targetTopicId,

    @JsonPropertyDescription("Boundary visibility: true for visible, false for hidden")
    boolean boundaryVisibility
) {
}
