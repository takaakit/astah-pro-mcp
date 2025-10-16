package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StateWithEntryDTO(
    @JsonPropertyDescription("Target state ID")
    String targetStateId,

    @JsonPropertyDescription("Entry")
    String entry
) {
}
