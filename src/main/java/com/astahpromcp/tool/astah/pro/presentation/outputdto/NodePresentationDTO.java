package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.FilePathHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.UrlHyperlinkDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NodePresentationDTO(
    @JsonPropertyDescription("Presentation information")
    PresentationDTO presentation,

    @JsonPropertyDescription("Identifiers of connected links")
    List<String> linkIds,

    @JsonPropertyDescription("Drawn rectangle")
    RectangleDTO drawnRectangle,

    @JsonPropertyDescription("Hyperlinks to URL")
    List<UrlHyperlinkDTO> urlHyperlinks,

    @JsonPropertyDescription("Hyperlinks to file path")
    List<FilePathHyperlinkDTO> filePathHyperlinks,

    @JsonPropertyDescription("Hyperlinks to named element")
    List<NamedElementHyperlinkDTO> namedElementHyperlinks
) {
}
