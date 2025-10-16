package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PresentationWithLabelDTO(
        @JsonPropertyDescription("Target presentation identifier")
        String presentationId,

        @JsonPropertyDescription("Label text. Note that newline characters (\\n) cannot be used in labels.")
        String label
) {
}
