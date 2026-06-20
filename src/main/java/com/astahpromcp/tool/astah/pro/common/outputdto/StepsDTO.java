package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StepsDTO(
        @JsonPropertyDescription("Steps contents")
        String contents
) {
}
