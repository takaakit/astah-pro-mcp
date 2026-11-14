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
    
    // An answer that holds no page of changes
    public static DiffResultDTO withoutPage(
            String diffId,
            DiffStatus status,
            String expiresAt,
            DiffFailureDTO failure,
            ProjectSideDTO base,
            ProjectSideDTO target,
            NormalizationDTO normalization,
            List<String> warnings,
            List<String> notCompared,
            SummaryDTO summary,
            String fullDiffPath) {
        return emptyPage(
                diffId,
                status,
                expiresAt,
                failure,
                base,
                target,
                normalization,
                warnings,
                notCompared,
                summary,
                0,
                0,
                fullDiffPath);
    }
    
    // This answer as the frame of the page that starts at startIndex and startPropertyIndex, kept until expiresAt.
    public DiffResultDTO startingAt(
            String expiresAt,
            int startIndex,
            int startPropertyIndex) {
        return emptyPage(
                diffId,
                status,
                expiresAt,
                failure,
                base,
                target,
                normalization,
                warnings,
                notCompared,
                summary,
                startIndex,
                startPropertyIndex,
                fullDiffPath);
    }
    
    // This page with its changes
    public DiffResultDTO withPage(
            SummaryDTO summary,
            int nextIndex,
            int nextPropertyIndex,
            List<ChangeDTO> changes) {
        return new DiffResultDTO(
                diffId,
                status,
                expiresAt,
                failure,
                base,
                target,
                normalization,
                warnings,
                notCompared,
                summary,
                startIndex,
                startPropertyIndex,
                nextIndex,
                nextPropertyIndex,
                changes,
                nextIndex != -1,
                shortened,
                fullDiffPath);
    }
    
    // This answer with the fields other than the changes replaced by shortened ones (ResponseLimiter)
    public DiffResultDTO withShortenedHeader(
            ProjectSideDTO base,
            ProjectSideDTO target,
            List<String> warnings,
            List<String> notCompared) {
        return new DiffResultDTO(
                diffId,
                status,
                expiresAt,
                failure,
                base,
                target,
                normalization,
                warnings,
                notCompared,
                summary,
                startIndex,
                startPropertyIndex,
                nextIndex,
                nextPropertyIndex,
                changes,
                truncated,
                true,
                fullDiffPath);
    }
    
    // The one place that says what a page without changes holds
    private static DiffResultDTO emptyPage(
            String diffId,
            DiffStatus status,
            String expiresAt,
            DiffFailureDTO failure,
            ProjectSideDTO base,
            ProjectSideDTO target,
            NormalizationDTO normalization,
            List<String> warnings,
            List<String> notCompared,
            SummaryDTO summary,
            int startIndex,
            int startPropertyIndex,
            String fullDiffPath) {
        return new DiffResultDTO(
                diffId,
                status,
                expiresAt,
                failure,
                base,
                target,
                normalization,
                warnings,
                notCompared,
                summary,
                startIndex,
                startPropertyIndex,
                -1,
                0,
                List.of(),
                false,
                false,
                fullDiffPath);
    }
}
