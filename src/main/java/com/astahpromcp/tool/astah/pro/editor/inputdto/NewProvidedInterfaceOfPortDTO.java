package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewProvidedInterfaceOfPortDTO(
    @JsonPropertyDescription("Target port identifier")
    String targetPortId,

    @JsonPropertyDescription("Target interface identifier")
    String targetInterfaceId
) {
}
