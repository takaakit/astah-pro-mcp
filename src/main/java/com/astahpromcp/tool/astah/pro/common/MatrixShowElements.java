package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

// Which row and column elements a relationship matrix lists, by whether they have a relationship.
public enum MatrixShowElements {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("all")
    ALL(true, true, true, true),

    @JsonProperty("with_relationships")
    WITH_RELATIONSHIPS(true, false, true, false),

    @JsonProperty("without_relationships")
    WITHOUT_RELATIONSHIPS(false, true, false, true),

    @JsonProperty("rows_without_relationships")
    ROWS_WITHOUT_RELATIONSHIPS(false, true, true, true),

    @JsonProperty("columns_without_relationships")
    COLUMNS_WITHOUT_RELATIONSHIPS(true, true, false, true);

    private final boolean listsRowsWithRelationships;
    private final boolean listsRowsWithoutRelationships;
    private final boolean listsColumnsWithRelationships;
    private final boolean listsColumnsWithoutRelationships;

    private MatrixShowElements(
            boolean listsRowsWithRelationships,
            boolean listsRowsWithoutRelationships,
            boolean listsColumnsWithRelationships,
            boolean listsColumnsWithoutRelationships) {
        this.listsRowsWithRelationships = listsRowsWithRelationships;
        this.listsRowsWithoutRelationships = listsRowsWithoutRelationships;
        this.listsColumnsWithRelationships = listsColumnsWithRelationships;
        this.listsColumnsWithoutRelationships = listsColumnsWithoutRelationships;
    }

    // Whether a row element with the given number of relationships is listed
    public boolean listsRow(int relationships) {
        return relationships > 0 ? listsRowsWithRelationships : listsRowsWithoutRelationships;
    }

    // Whether a column element with the given number of relationships is listed
    public boolean listsColumn(int relationships) {
        return relationships > 0 ? listsColumnsWithRelationships : listsColumnsWithoutRelationships;
    }

    // The name the matrix shows, which is also the value the tool takes
    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }
}
