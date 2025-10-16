package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NodePresentationDTO(
    @JsonPropertyDescription("Presentation information")
    PresentationDTO presentation,

    @JsonPropertyDescription("Identifiers of connected links")
    List<String> linkIds,

    @JsonPropertyDescription("Drawn rectangle")
    RectangleDTO drawnRectangle
) {
}
