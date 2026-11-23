package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

// Which way the relationships a relationship matrix shows go between its rows and its columns
public enum MatrixDirection {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("row_to_column")
    ROW_TO_COLUMN,

    @JsonProperty("column_to_row")
    COLUMN_TO_ROW,

    @JsonProperty("both")
    BOTH;

    // Whether a relationship from a row element to a column element is shown
    public boolean showsRowToColumn() {
        return this != COLUMN_TO_ROW;
    }

    // Whether a relationship from a column element to a row element is shown
    public boolean showsColumnToRow() {
        return this != ROW_TO_COLUMN;
    }

    // The name the matrix shows, which is also the value the tool takes
    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }
}
