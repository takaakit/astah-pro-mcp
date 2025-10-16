package com.astahpromcp.tool.astah.pro.common.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PointIntDTO(
    @JsonPropertyDescription("X coordinate")
    int x,

    @JsonPropertyDescription("Y coordinate")
    int y
) {
}
