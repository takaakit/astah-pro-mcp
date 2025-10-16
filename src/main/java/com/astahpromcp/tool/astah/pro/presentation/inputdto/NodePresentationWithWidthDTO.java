package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NodePresentationWithWidthDTO(
    @JsonPropertyDescription("Target node presentation identifier")
    String nodePresentationId,

    @JsonPropertyDescription("Node width")
    int width
) {
}
