package com.astahpromcp.tool.astah.pro.image.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SvgOverlayOnDiagramDTO(
    @JsonPropertyDescription("Target diagram identifier. Specify an empty string to render the SVG image on its own, without overlaying it on a diagram image.")
    String targetDiagramId,

    @JsonPropertyDescription("Image SVG code. Note that SVG code must be enclosed within SVG tags. The viewBox min-x and min-y are the diagram coordinates at which the image's top-left corner is placed, while the width and height attributes give its displayed size. For example, viewBox=\"300 -40 200 150\" places the image at diagram coordinates (300, -40).")
    String imageSvgCode
) {
}
