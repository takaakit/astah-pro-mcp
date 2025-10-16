package com.astahpromcp.tool.visualization.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PlantumlDTO(
    @JsonPropertyDescription("PlantUML code")
    String plantumlCode
) {
}
