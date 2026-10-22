package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SummaryDTO(
    @JsonPropertyDescription("Number of changed elements")
    int total,

    @JsonPropertyDescription("Number of added elements")
    int added,

    @JsonPropertyDescription("Number of removed elements")
    int removed,

    @JsonPropertyDescription("Number of modified elements")
    int modified,

    @JsonPropertyDescription("Number of changes, or parts of changes, in 'changes'")
    int returned,

    @JsonPropertyDescription("Number of values in 'changes' that are cut short ('cut' marks the properties that have one); 0 if none is")
    int valuesCut
) {
    public static SummaryDTO empty() {
        return new SummaryDTO(
            0,
            0,
            0,
            0,
            0,
            0);
    }
}
