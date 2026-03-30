package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.astahpromcp.tool.astah.pro.presentation.LineStyleKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LinkPresentationWithLineStyleDTO(
    @JsonPropertyDescription("Target link presentation identifier")
    String targetLinkPresentationId,

    @JsonPropertyDescription("Line style to set (line, line_right_angle, curve, curve_right_angle)")
    LineStyleKind lineStyle
) {
}
