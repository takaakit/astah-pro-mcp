package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record PresentationTypeListDTO(
    @JsonPropertyDescription("List value of presentation type names")
    List<String> value
) {
}
