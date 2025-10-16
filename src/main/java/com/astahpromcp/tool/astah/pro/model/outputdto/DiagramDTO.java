package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record DiagramDTO(
        @JsonPropertyDescription("Named element information")
        NamedElementDTO namedElement,
        
        @JsonPropertyDescription("Identifiers of contained presentations")
        List<String> containedPresentationIds
) {
}
