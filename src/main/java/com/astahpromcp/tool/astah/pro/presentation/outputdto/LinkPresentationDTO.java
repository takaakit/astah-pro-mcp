package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LinkPresentationDTO(
        @JsonPropertyDescription("Presentation info")
        PresentationDTO presentation,

        @JsonPropertyDescription("Label, identifier and type of source end node")
        LabelIdTypeDTO sourceNodeEnd,

        @JsonPropertyDescription("Label, identifier and type of target end node")
        LabelIdTypeDTO targetNodeEnd,

        @JsonPropertyDescription("Drawn points with the connection points in the rectangles")
        List<PointDoubleDTO> drawnPoints,

        @JsonPropertyDescription("Line style")
        LineStyleKind lineStyle
) {
}
