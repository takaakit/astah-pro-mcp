package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DeleteRegionDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Parent node presentation identifier")
    String parentNodePresentationId,

    @JsonPropertyDescription("0-based index of the region to delete: from 0 to the number of regions of the parent state minus 1")
    int index
) {
}
