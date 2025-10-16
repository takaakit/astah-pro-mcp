package com.astahpromcp.tool.astah.pro.common;

public enum VisibilityKind {
    // Since some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, the literals are defined in lowercase.
    public_,
    protected_,
    private_,
    package_;

    public String toAstahValue() {
        return switch (this) {
            case public_ -> "public";
            case protected_ -> "protected";
            case private_ -> "private";
            case package_ -> "package";
        };
    }
}
