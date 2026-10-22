package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NormalizationDTO(
    @JsonPropertyDescription("Version of the matching rules")
    int version,

    @JsonPropertyDescription("True if the matching rules were applied")
    boolean applied,

    @JsonPropertyDescription("Number of pairs each rule matched")
    List<RuleCountDTO> counts
) {
}
