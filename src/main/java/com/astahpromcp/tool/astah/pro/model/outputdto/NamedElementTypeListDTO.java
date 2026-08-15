package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NamedElementTypeListDTO(
    @JsonPropertyDescription("List value of named element type names")
    List<String> value
) {
}
