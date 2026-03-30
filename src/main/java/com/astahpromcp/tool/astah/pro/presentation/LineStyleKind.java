package com.astahpromcp.tool.astah.pro.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LineStyleKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("line")
    LINE("line"),
    @JsonProperty("line_right_angle")
    LINE_RIGHT_ANGLE("line_right_angle"),
    @JsonProperty("curve")
    CURVE("curve"),
    @JsonProperty("curve_right_angle")
    CURVE_RIGHT_ANGLE("curve_right_angle"),
    @JsonProperty("unknown")
    UNKNOWN(null);

    public final String astahValue;

    private LineStyleKind(String astahValue) {
        this.astahValue = astahValue;
    }

    public static LineStyleKind getCorrespondingType(String astahValue) {
        if (astahValue == null) {
            return UNKNOWN;
        }

        for (LineStyleKind kind : values()) {
            if (astahValue.equals(kind.astahValue)) {
                return kind;
            }
        }
        return UNKNOWN;   
    }
}
