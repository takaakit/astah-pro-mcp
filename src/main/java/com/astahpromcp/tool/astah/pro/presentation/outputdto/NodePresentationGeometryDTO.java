package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NodePresentationGeometryDTO(
    @JsonPropertyDescription("Presentation identifier")
    String id,

    @JsonPropertyDescription("Label text")
    String label,

    @JsonPropertyDescription("Presentation type name")
    String type,

    @JsonPropertyDescription("Corresponding model element (named element)")
    NameIdTypeDTO correspondingModelElement,

    @JsonPropertyDescription("Drawn rectangle in diagram coordinates")
    RectangleDTO drawnRectangle
) {
}
