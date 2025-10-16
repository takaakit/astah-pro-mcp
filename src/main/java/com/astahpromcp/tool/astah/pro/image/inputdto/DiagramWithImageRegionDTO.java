package com.astahpromcp.tool.astah.pro.image.inputdto;

import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiagramWithImageRegionDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Image region")
    ImageRegion region
) {
}
