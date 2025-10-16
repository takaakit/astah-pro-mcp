package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTaggedValueToElementDTO(
        @JsonPropertyDescription("Target element identifier")
        String targetElementId,

        @JsonPropertyDescription("New key of tagged value")
        String newKeyOfTaggedValue,

        @JsonPropertyDescription("New value of tagged value")
        String newValueOfTaggedValue
) {
}
