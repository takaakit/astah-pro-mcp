package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ImageFileDTO(
    @JsonPropertyDescription("Image file path")
    String imageFilePath,
    
    @JsonPropertyDescription("Image format (e.g., 'png', 'jpg', 'gif')")
    String imageFormat,

    @JsonPropertyDescription("Image width in pixels")
    int imageWidth,

    @JsonPropertyDescription("Image height in pixels")
    int imageHeight
) {
}

