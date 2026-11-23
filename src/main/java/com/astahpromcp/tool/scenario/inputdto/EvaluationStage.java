package com.astahpromcp.tool.scenario.inputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Which steps of the architecture scenario evaluation and improvement are asked for
public enum EvaluationStage {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("evaluation")
    EVALUATION,

    @JsonProperty("closing")
    CLOSING
}
