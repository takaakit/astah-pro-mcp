package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NamedElementListDTO(
    @JsonPropertyDescription("List value of named elements")
    List<NamedElementDTO> value
) {
}
