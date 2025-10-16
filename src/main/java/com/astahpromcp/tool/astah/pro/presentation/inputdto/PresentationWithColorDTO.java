package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PresentationWithColorDTO(
        @JsonPropertyDescription("Target presentation identifier")
        String presentationId,

        @JsonPropertyDescription("Color value (in the format #FFFFFF).")
        String color
) {
}
