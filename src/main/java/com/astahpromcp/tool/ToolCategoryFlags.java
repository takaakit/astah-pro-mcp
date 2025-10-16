package com.astahpromcp.tool;

// Tool category flags
public class ToolCategoryFlags {

    private final boolean classDiagramEnabled;
    private final boolean sequenceDiagramEnabled;
    private final boolean activityDiagramEnabled;
    private final boolean stateMachineDiagramEnabled;
    private final boolean useCaseDiagramEnabled;
    private final boolean requirementDiagramEnabled;
    
    public ToolCategoryFlags(
            boolean classDiagramEnabled,
            boolean sequenceDiagramEnabled,
            boolean activityDiagramEnabled,
            boolean stateMachineDiagramEnabled,
            boolean useCaseDiagramEnabled,
            boolean requirementDiagramEnabled) {

        this.classDiagramEnabled = classDiagramEnabled;
        this.sequenceDiagramEnabled = sequenceDiagramEnabled;
        this.activityDiagramEnabled = activityDiagramEnabled;
        this.stateMachineDiagramEnabled = stateMachineDiagramEnabled;
        this.useCaseDiagramEnabled = useCaseDiagramEnabled;
        this.requirementDiagramEnabled = requirementDiagramEnabled;
    }
    
    public boolean isClassDiagramEnabled() {
        return classDiagramEnabled;
    }
    
    public boolean isSequenceDiagramEnabled() {
        return sequenceDiagramEnabled;
    }
    
    public boolean isActivityDiagramEnabled() {
        return activityDiagramEnabled;
    }

    public boolean isStateMachineDiagramEnabled() {
        return stateMachineDiagramEnabled;
    }
    
    public boolean isUseCaseDiagramEnabled() {
        return useCaseDiagramEnabled;
    }
    
    public boolean isRequirementDiagramEnabled() {
        return requirementDiagramEnabled;
    }
}