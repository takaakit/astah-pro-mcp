package com.astahpromcp.tool.scenario.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EvaluationStageDTO(
    @JsonPropertyDescription("evaluation when the editing loop has ended; closing after the steps for after you have finished editing")
    EvaluationStage stage
) {
}
