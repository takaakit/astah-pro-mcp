package com.astahpromcp.tool.scenario.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record WorkTypeDTO(
    @JsonPropertyDescription("new when you create an architecture anew; change when you change an existing one")
    WorkType workType
) {
}
