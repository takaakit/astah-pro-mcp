package com.astahpromcp.tool.scenario.inputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

// What a piece of architecture work does, which decides how its scenarios are developed and what its evaluation may change
public enum WorkType {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("new")
    NEW("New"),

    @JsonProperty("change")
    CHANGE("Change");

    private final String label;

    WorkType(String label) {
        this.label = label;
    }

    // The name the scenario file records under "# Work"
    public String label() {
        return label;
    }

    // The work type the scenario file names, read without regard to case
    public static Optional<WorkType> ofLabel(String label) {
        if (label == null) {
            return Optional.empty();
        }

        String trimmed = label.trim();
        for (WorkType workType : values()) {
            if (workType.label.equalsIgnoreCase(trimmed)) {
                return Optional.of(workType);
            }
        }
        return Optional.empty();
    }
}
