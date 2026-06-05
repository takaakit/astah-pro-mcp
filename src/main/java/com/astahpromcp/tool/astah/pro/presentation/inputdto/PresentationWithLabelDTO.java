package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PresentationWithLabelDTO(
        @JsonPropertyDescription("Target presentation identifier")
        String presentationId,

        @JsonPropertyDescription("Label text. Note that escape sequences such as \\n cannot be used in labels, but actual newline characters (Unicode U+000A, embedded directly in the string) are supported.")
        String label
) {
}
