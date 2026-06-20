package com.astahpromcp.tool.common.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TextContentsDTO(
    @JsonPropertyDescription("Text contents")
    String contents
) {
}
