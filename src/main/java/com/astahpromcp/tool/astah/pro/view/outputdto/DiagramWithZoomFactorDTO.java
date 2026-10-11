package com.astahpromcp.tool.astah.pro.view.outputdto;

import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiagramWithZoomFactorDTO(
    @JsonPropertyDescription("Diagram info")
    DiagramDTO diagram,

    @JsonPropertyDescription("Zoom factor Astah actually applied to the diagram (1.0 is 100%)")
    double zoomFactorValue
) {
}
