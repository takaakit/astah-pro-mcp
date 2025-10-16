package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.PseudostateKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PseudostateDTO(
    @JsonPropertyDescription("Vertex information")
    VertexDTO vertex,

    @JsonPropertyDescription("Pseudostate kind")
    PseudostateKind pseudostateKind
) {
}
