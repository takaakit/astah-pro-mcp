package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERIndexWithKeyDTO(
    @JsonPropertyDescription("Target ER index identifier")
    String targetERIndexId,

    @JsonPropertyDescription("Is key: true - show as key(AK,IE), false - not show as key(AK,IE)")
    boolean isKey
) {
}
