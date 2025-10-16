package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ElementListDTO(
    @JsonPropertyDescription("List value of elements")
    List<ElementDTO> value
) {
}
