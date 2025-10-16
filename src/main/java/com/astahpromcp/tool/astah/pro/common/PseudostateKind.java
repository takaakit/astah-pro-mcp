package com.astahpromcp.tool.astah.pro.common;

public enum PseudostateKind {
    // Since some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, the literals are defined in lowercase.
    choice,
    deep_history,
    entry_point,
    exit_point,
    fork,
    initial,
    join,
    junction,
    shallow_history,
    stub
}
