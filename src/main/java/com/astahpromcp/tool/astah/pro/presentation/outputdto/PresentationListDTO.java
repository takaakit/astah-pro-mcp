package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record PresentationListDTO(
    @JsonPropertyDescription("List value of presentations")
    List<PresentationDTO> value
) {
}
