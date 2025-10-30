package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewSvgImageWithPointDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Image SVG code. Note that SVG code must be enclosed within SVG tags.")
    String imageSvgCode,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
