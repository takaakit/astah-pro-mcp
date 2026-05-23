package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record GuideDTO(
        @JsonPropertyDescription("Guide content")
        String content
) {
}
