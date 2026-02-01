package com.astahpromcp.tool;

// Tool category flags
public record ToolCategoryFlags(
        boolean classDiagramEnabled,
        boolean sequenceDiagramEnabled,
        boolean activityDiagramEnabled,
        boolean stateMachineDiagramEnabled,
        boolean useCaseDiagramEnabled,
        boolean requirementDiagramEnabled,
        boolean communicationDiagramEnabled,
        boolean compositeStructureDiagramEnabled,
        boolean erDiagramEnabled) {
}
