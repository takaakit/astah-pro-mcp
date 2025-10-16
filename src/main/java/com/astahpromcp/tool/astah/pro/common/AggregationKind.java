package com.astahpromcp.tool.astah.pro.common;

public enum AggregationKind {
    // Since some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, the literals are defined in lowercase.
    aggregate,
    composite,
    none;

    public com.change_vision.jude.api.inf.model.AggregationKind toAstahValue() {
        return switch (this) {
            case aggregate -> com.change_vision.jude.api.inf.model.AggregationKind.AGGREGATE;
            case composite -> com.change_vision.jude.api.inf.model.AggregationKind.COMPOSITE;
            case none -> com.change_vision.jude.api.inf.model.AggregationKind.NONE;
        };
    }
}
