package com.astahpromcp.tool.astah.pro.common;

public enum CombinedFragmentKind {
    // Since some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, the literals are defined in lowercase.
    alt,
    assert_,
    break_,
    consider,
    critical,
    ignore,
    loop,
    neg,
    opt,
    par,
    seq,
    strict;

    public String toAstahValue() {
        return switch (this) {
            case alt -> "alt";
            case assert_ -> "assert";
            case break_ -> "break";
            case consider -> "consider";
            case critical -> "critical";
            case ignore -> "ignore";
            case loop -> "loop";
            case neg -> "neg";
            case opt -> "opt";
            case par -> "par";
            case seq -> "seq";
            case strict -> "strict";
        };
    }
}
