package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewSvgImageIntoTopicDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to insert SVG image into")
    String targetTopicId,

    @JsonPropertyDescription("Image SVG code. Note that SVG code must be enclosed within SVG tags.")
    String imageSvgCode
) {
}
