package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiffFailureDTO(
    @JsonPropertyDescription("Stage that failed; none unless the comparison failed")
    DiffStage stage,

    @JsonPropertyDescription("Error message; empty unless the comparison failed")
    String message,

    @JsonPropertyDescription("True if starting the same comparison again, with a new 'requestId', may succeed")
    boolean retryable
) {
    public static DiffFailureDTO empty() {
        return new DiffFailureDTO(
            DiffStage.NONE,
            "",
            false);
    }
}
