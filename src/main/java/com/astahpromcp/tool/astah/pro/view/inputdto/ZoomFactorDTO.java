package com.astahpromcp.tool.astah.pro.view.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ZoomFactorDTO(
    @JsonPropertyDescription("Absolute zoom factor between 0.05 and 4.0 (e.g., 1.0 for 100%, 1.5 for 150%, 0.5 for 50%), not relative to the current zoom factor. Astah may round it to its own zoom steps.")
    double zoomFactorValue
) {
}
