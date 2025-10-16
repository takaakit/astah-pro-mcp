package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SlotWithValueDTO(
    @JsonPropertyDescription("Target slot identifier")
    String targetSlotId,

    @JsonPropertyDescription("Slot value")
    String value
) {
}
