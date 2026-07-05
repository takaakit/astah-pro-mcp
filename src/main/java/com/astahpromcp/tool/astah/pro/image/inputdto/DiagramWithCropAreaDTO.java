package com.astahpromcp.tool.astah.pro.image.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiagramWithCropAreaDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("X coordinate of the top-left corner of the crop area, in the diagram coordinate system")
    int x,

    @JsonPropertyDescription("Y coordinate of the top-left corner of the crop area, in the diagram coordinate system")
    int y,

    @JsonPropertyDescription("Width of the crop area, in the diagram coordinate system")
    int width,

    @JsonPropertyDescription("Height of the crop area, in the diagram coordinate system")
    int height
) {
}
