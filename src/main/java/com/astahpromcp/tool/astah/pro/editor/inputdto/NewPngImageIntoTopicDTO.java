package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPngImageIntoTopicDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to insert PNG image into")
    String targetTopicId,

    @JsonPropertyDescription("URL pointing to a PNG image. When specifying a local image file, use the 'file:///' protocol.")
    String imageUrl
) {
}
