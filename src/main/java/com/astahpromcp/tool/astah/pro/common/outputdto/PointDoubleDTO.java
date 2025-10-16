package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PointDoubleDTO(
    @JsonPropertyDescription("X coordinate")
    double x,

    @JsonPropertyDescription("Y coordinate")
    double y
) {
}
