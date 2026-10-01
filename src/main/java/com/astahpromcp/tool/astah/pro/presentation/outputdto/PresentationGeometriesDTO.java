package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record PresentationGeometriesDTO(
    @JsonPropertyDescription("Diagram whose presentations are listed")
    NameIdTypeDTO diagram,

    @JsonPropertyDescription("Boundary rectangle of the diagram in diagram coordinates")
    RectangleDTO diagramRectangle,

    @JsonPropertyDescription("Node presentations on the diagram with their drawn rectangles")
    List<NodePresentationGeometryDTO> nodes,

    @JsonPropertyDescription("Link presentations on the diagram with their drawn points")
    List<LinkPresentationGeometryDTO> links
) {
}
