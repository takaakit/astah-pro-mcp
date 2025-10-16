package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LabelIdTypeListDTO(
    @JsonPropertyDescription("List value of label, identifier and type")
    List<LabelIdTypeDTO> value
) {
}