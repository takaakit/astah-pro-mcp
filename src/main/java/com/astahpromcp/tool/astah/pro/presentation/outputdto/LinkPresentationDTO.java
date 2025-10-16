package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LinkPresentationDTO(
        @JsonPropertyDescription("Presentation information")
        PresentationDTO presentation,

        @JsonPropertyDescription("Identifier of source end node")
        String sourceNodeEndId,

        @JsonPropertyDescription("Identifier of target end node")
        String targetNodeEndId,

        @JsonPropertyDescription("Drawn points with the connection points in the rectangles")
        List<PointDoubleDTO> drawnPoints
) {
}
