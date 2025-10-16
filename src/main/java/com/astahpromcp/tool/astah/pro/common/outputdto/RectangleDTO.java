package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RectangleDTO(
        @JsonPropertyDescription("X coordinate (double value)")
        double x,

        @JsonPropertyDescription("Y coordinate (double value)")
        double y,

        @JsonPropertyDescription("Width (double value)")
        double width,
        
        @JsonPropertyDescription("Height (double value)")
        double height
) {
}
