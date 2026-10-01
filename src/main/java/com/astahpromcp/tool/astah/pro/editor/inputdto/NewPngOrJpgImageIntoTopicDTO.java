package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPngOrJpgImageIntoTopicDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to insert PNG or JPG image into")
    String targetTopicId,

    @JsonPropertyDescription("URL pointing to a PNG or JPG image. When specifying a local image file, use the 'file:///' protocol.")
    String imageUrl
) {
}
