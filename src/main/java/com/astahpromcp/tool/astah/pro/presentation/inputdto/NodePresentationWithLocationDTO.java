package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NodePresentationWithLocationDTO(
    @JsonPropertyDescription("Target node presentation identifier")
    String nodePresentationId,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
