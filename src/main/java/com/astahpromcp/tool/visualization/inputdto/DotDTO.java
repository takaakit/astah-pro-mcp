package com.astahpromcp.tool.visualization.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DotDTO(
    @JsonPropertyDescription("Graphviz DOT code")
    String dotCode
) {
}
