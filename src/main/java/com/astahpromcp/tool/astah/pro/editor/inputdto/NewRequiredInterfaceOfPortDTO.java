package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewRequiredInterfaceOfPortDTO(
    @JsonPropertyDescription("Target port identifier")
    String targetPortId,

    @JsonPropertyDescription("Target interface identifier")
    String targetInterfaceId
) {
}
