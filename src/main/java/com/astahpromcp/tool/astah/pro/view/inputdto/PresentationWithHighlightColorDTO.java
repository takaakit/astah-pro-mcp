package com.astahpromcp.tool.astah.pro.view.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PresentationWithHighlightColorDTO(
        @JsonPropertyDescription("Target presentation identifier")
        String presentationId,

        @JsonPropertyDescription("Highlight color value (in the format #FFFFFF).")
        String highlightColor
) {
}

