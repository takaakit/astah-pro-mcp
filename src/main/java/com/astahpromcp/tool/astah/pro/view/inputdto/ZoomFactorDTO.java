package com.astahpromcp.tool.astah.pro.view.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ZoomFactorDTO(
    @JsonPropertyDescription("Zoom factor value (e.g., 1.0 for 100%, 1.5 for 150%, 0.5 for 50%)")
    double zoomFactorValue
) {
}
