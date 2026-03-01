package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AggregationKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("aggregate")
    AGGREGATE(com.change_vision.jude.api.inf.model.AggregationKind.AGGREGATE),
    @JsonProperty("composite")
    COMPOSITE(com.change_vision.jude.api.inf.model.AggregationKind.COMPOSITE),
    @JsonProperty("none")
    NONE(com.change_vision.jude.api.inf.model.AggregationKind.NONE);

    public final com.change_vision.jude.api.inf.model.AggregationKind astahValue;

    private AggregationKind(com.change_vision.jude.api.inf.model.AggregationKind astahValue) {
        this.astahValue = astahValue;
    }
}
