package com.astahpromcp.tool.astah.pro.common.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record IdListDTO(
    @JsonPropertyDescription("List value of identifiers")
    List<IdDTO> value
) {
}
