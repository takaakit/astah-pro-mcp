package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

// The kinds of relationship a relationship matrix shows
public enum MatrixRelationshipKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("association")
    ASSOCIATION(null),

    @JsonProperty("aggregation")
    AGGREGATION(null),

    @JsonProperty("composition")
    COMPOSITION(null),

    @JsonProperty("generalization")
    GENERALIZATION(null),

    @JsonProperty("realization")
    REALIZATION(null),

    @JsonProperty("dependency")
    DEPENDENCY(null),

    @JsonProperty("usage")
    USAGE(null),

    @JsonProperty("include")
    INCLUDE(null),

    @JsonProperty("extend")
    EXTEND(null),

    @JsonProperty("containment")
    CONTAINMENT(null),

    @JsonProperty("transition")
    TRANSITION(null),

    @JsonProperty("flow")
    FLOW(null),

    @JsonProperty("connector")
    CONNECTOR(null),

    @JsonProperty("link")
    LINK(null),

    @JsonProperty("data_flow")
    DATA_FLOW(null),

    @JsonProperty("er_relationship")
    ER_RELATIONSHIP(null),

    @JsonProperty("satisfy")
    SATISFY("satisfy"),

    @JsonProperty("verify")
    VERIFY("verify"),

    @JsonProperty("refine")
    REFINE("refine"),

    @JsonProperty("trace")
    TRACE("trace"),

    @JsonProperty("derive_reqt")
    DERIVE_REQT("deriveReqt"),

    @JsonProperty("copy")
    COPY("copy");

    // The stereotype that makes a dependency this requirement relationship, compared case-sensitively; null for the other kinds
    public final String requirementStereotype;

    private MatrixRelationshipKind(String requirementStereotype) {
        this.requirementStereotype = requirementStereotype;
    }

    // The name the matrix shows, which is also the value the tool takes
    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }
}
