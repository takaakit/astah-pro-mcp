package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NewSubtypeRelationshipGroupOnERDiagramDTO(
    @JsonPropertyDescription("Target ER diagram identifier")
    String targetERDiagramId,

    @JsonPropertyDescription("Subtype relationship link presentation identifiers")
    List<String> subtypeRelationshipLinkPresentationIds,

    @JsonPropertyDescription("Direction: \"vertical\" or \"horizontal\"")
    String direction
) {
}
