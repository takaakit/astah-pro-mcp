package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RuleCountDTO(
    @JsonPropertyDescription("Rule name")
    String rule,

    @JsonPropertyDescription("Number of pairs the rule matched")
    int count
) {
}
