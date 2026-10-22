package com.astahpromcp.tool.projectdiff.outputdto;

import com.astahpromcp.config.McpServerConfig;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record DiffResultDTO(
    @JsonPropertyDescription("Comparison ID")
    String diffId,

    @JsonPropertyDescription("running (call again), done, or failed")
    DiffStatus status,

    @JsonPropertyDescription("Time until which the result is kept at least while it has not been fetched (ISO 8601); empty while running. Once fetched, it may be deleted earlier to make room, when "
            + McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES + " minutes have passed since it was last fetched")
    String expiresAt,

    @JsonPropertyDescription("Failure of the comparison")
    DiffFailureDTO failure,

    @JsonPropertyDescription("Project compared from")
    ProjectSideDTO base,

    @JsonPropertyDescription("Project compared to")
    ProjectSideDTO target,

    @JsonPropertyDescription("Matching of the parts recreated with a new xmi.id")
    NormalizationDTO normalization,

    @JsonPropertyDescription("Notes on reading the result")
    List<String> warnings,

    @JsonPropertyDescription("Data never compared")
    List<String> notCompared,

    @JsonPropertyDescription("Numbers of changed elements in the whole result, and of the changes in this page")
    SummaryDTO summary,

    @JsonPropertyDescription("Index of the first change in 'changes' among the changes of the whole result, counted from 0")
    int startIndex,

    @JsonPropertyDescription("Index of the first property in this page among the properties of the change at 'startIndex', counted from 0")
    int startPropertyIndex,

    @JsonPropertyDescription("Value to pass as 'startIndex' to get the next page; -1 when 'changes' reaches the end of the result")
    int nextIndex,

    @JsonPropertyDescription("Value to pass as 'startPropertyIndex' to get the next page; above 0 when this page ends inside a change, which the next page goes on with")
    int nextPropertyIndex,

    @JsonPropertyDescription("Changes of this page, in XMI order")
    List<ChangeDTO> changes,

    @JsonPropertyDescription("True if the result goes on after this page, that is, if 'nextIndex' is not -1")
    boolean truncated,

    @JsonPropertyDescription("True if fields other than 'changes' were shortened")
    boolean shortened,

    @JsonPropertyDescription("Path of the JSON file with the full result; empty unless done")
    String fullDiffPath
) {
}
