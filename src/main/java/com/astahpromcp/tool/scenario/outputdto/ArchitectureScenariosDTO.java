package com.astahpromcp.tool.scenario.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ArchitectureScenariosDTO(
    @JsonPropertyDescription("Whether the scenario file exists")
    boolean scenarioFileExists,

    @JsonPropertyDescription("Contents of the scenario file; empty if the file does not exist")
    String contents,

    @JsonPropertyDescription("What keeps the scenarios from being fully developed; empty when they are fully developed")
    List<String> problems,

    @JsonPropertyDescription("What is wrong with the architecture decisions in the scenario file; empty when nothing is")
    List<String> architectureDecisionProblems,

    @JsonPropertyDescription("Minutes since the scenario file was last modified; -1 if the file does not exist")
    long minutesSinceLastModified,

    @JsonPropertyDescription("Absolute path of the scenario file")
    String scenarioFilePath,

    @JsonPropertyDescription("Absolute path of the abstract scenario image file")
    String abstractScenarioImageFilePath,

    @JsonPropertyDescription("Whether the abstract scenario image file exists")
    boolean abstractScenarioImageExists,

    @JsonPropertyDescription("Absolute path of the evaluation results file")
    String evaluationResultsFilePath,

    @JsonPropertyDescription("Whether the evaluation results file exists")
    boolean evaluationResultsFileExists,

    @JsonPropertyDescription("Contents of the evaluation results file; empty if the file does not exist")
    String evaluationResultsContents,

    @JsonPropertyDescription("Absolute path of the improvement file")
    String architectureImprovementsFilePath,

    @JsonPropertyDescription("Contents of the improvement file; empty if the file does not exist")
    String architectureImprovementsContents,

    @JsonPropertyDescription("What is wrong with the evaluation results file and the improvement file; empty when nothing is")
    List<String> evaluationProblems,

    @JsonPropertyDescription("What to do next about the architecture scenario evaluation and improvement; empty when no evaluation has begun")
    String evaluationGuidance,

    @JsonPropertyDescription("Absolute paths of the working directories set aside earlier, newest first")
    List<String> setAsideDirectoryPaths
) {
}
