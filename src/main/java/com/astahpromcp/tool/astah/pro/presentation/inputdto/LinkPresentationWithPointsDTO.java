package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.astahpromcp.tool.astah.pro.common.inputdto.PointIntDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LinkPresentationWithPointsDTO(
    @JsonPropertyDescription("Target link presentation identifier")
    String targetLinkPresentationId,

    @JsonPropertyDescription("Draw points with the connection points in the rectangles.")
    List<PointIntDTO> drawPoints
) {
}
