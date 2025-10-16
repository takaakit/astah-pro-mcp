package com.astahpromcp.tool.astah.pro.common;

public enum NavigabilityKind {
    // Since some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, the literals are defined in lowercase.
    navigable,
    non_navigable,
    unspecified;

    public String toAstahValue() {
        return switch (this) {
            case navigable -> "Navigable";
            case non_navigable -> "Non_Navigable";
            case unspecified -> "Unspecified";
        };
    }
}