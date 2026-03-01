package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PseudostateKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("choice")
    CHOICE("ChoicePseudostate"),
    @JsonProperty("deep_history")
    DEEP_HISTORY("DeepHistoryPseudostate"),
    @JsonProperty("entry_point")
    ENTRY_POINT("EntryPointPseudostate"),
    @JsonProperty("exit_point")
    EXIT_POINT("ExitPointPseudostate"),
    @JsonProperty("fork")
    FORK("ForkPseudostate"),
    @JsonProperty("initial")
    INITIAL("InitialPseudostate"),
    @JsonProperty("join")
    JOIN("JoinPseudostate"),
    @JsonProperty("junction")
    JUNCTION("JunctionPseudostate"),
    @JsonProperty("shallow_history")
    SHALLOW_HISTORY("ShallowHistoryPseudostate"),
    @JsonProperty("stub")
    STUB("StubState in SubmachineState");

    public final String astahValue;

    private PseudostateKind(String astahValue) {
        this.astahValue = astahValue;
    }
}
