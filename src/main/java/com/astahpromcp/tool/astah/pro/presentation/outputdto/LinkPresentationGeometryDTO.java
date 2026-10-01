package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LinkPresentationGeometryDTO(
    @JsonPropertyDescription("Presentation identifier")
    String id,

    @JsonPropertyDescription("Label text")
    String label,

    @JsonPropertyDescription("Presentation type name")
    String type,

    @JsonPropertyDescription("Corresponding model element (named element)")
    NameIdTypeDTO correspondingModelElement,

    @JsonPropertyDescription("Label, identifier and type of the source end presentation")
    LabelIdTypeDTO sourceNodeEnd,

    @JsonPropertyDescription("Label, identifier and type of the target end presentation")
    LabelIdTypeDTO targetNodeEnd,

    @JsonPropertyDescription("Drawn points in diagram coordinates, including the connection points in the rectangles of both ends. Their order does not necessarily run from the source end to the target end")
    List<PointDoubleDTO> drawnPoints,

    @JsonPropertyDescription("Line style")
    LineStyleKind lineStyle
) {
}
