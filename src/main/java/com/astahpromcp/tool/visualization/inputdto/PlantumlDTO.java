package com.astahpromcp.tool.visualization.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PlantumlDTO(
    @JsonPropertyDescription("PlantUML code")
    String plantumlCode
) {
}
