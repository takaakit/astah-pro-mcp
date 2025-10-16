package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NodePresentationWithHeightDTO(
    @JsonPropertyDescription("Target node presentation identifier")
    String nodePresentationId,

    @JsonPropertyDescription("Node height")
    int height
) {
}
