package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewJpgImageIntoTopicDTO(
    @JsonPropertyDescription("Target mind map diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target topic (node presentation) identifier to insert JPG image into")
    String targetTopicId,

    @JsonPropertyDescription("URL pointing to a JPG image. When specifying a local image file, use the 'file:///' protocol.")
    String imageUrl
) {
}
