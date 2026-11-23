package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Writes a relationship matrix as the Markdown the tool returns
final class MatrixMarkdownRenderer {

    static final String NOTATION = "in the cell of a row and a column, " + RelationshipMatrix.FROM_ROW + " is a relationship from the row element to the column element, "
            + RelationshipMatrix.FROM_COLUMN + " one from the column element to the row element, "
            + RelationshipMatrix.BOTH_WAYS + " one in both directions, and "
            + RelationshipMatrix.NO_DIRECTION + " one without a direction. The Related columns use the same notation.";

    static final String NO_RELATIONSHIP_BETWEEN_LISTED = "Omitted: the listed elements have no relationship between them.";

    static final String IMPLIED_RELATIONSHIPS = "a class whose part, held by composition however deeply, satisfies a requirement is taken to satisfy it too, shown as satisfy (implied) with no ID, since it is no model element";

    static final String GROUPED_BY_PACKAGE = "each row and column is a package, a subsystem or the project itself that holds elements the criteria chose, and a cell holds the relationships between the elements of its two packages, ×3 after one of them meaning three of that kind. A relationship within one package is left out.";

    static final String UNNAMED = MatrixElement.UNNAMED;

    // The ID a containment or an implied relationship shows, which is no model element and has no ID
    static final String NO_ID = "—";

    private static final List<String> ELEMENT_COLUMNS = List.of("Key", "Name", "Namespace", "Type", "Req/Test ID", "ID", "Count", "Related");

    private static final List<String> RELATIONSHIP_COLUMNS = List.of("Row", "Column", "Relationship", "Detail", "ID");

    private final int maxGridRows;
    private final int maxGridColumns;

    // The grid is written only while it lists at most maxGridRows rows and maxGridColumns columns
    MatrixMarkdownRenderer(int maxGridRows, int maxGridColumns) {
        this.maxGridRows = maxGridRows;
        this.maxGridColumns = maxGridColumns;
    }

    String render(MatrixCriteriaSummary criteria, RelationshipMatrix matrix, boolean listRelationships) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Relationship Matrix\n");

        appendCriteria(markdown, criteria, matrix);
        appendSummary(markdown, matrix);
        appendGrid(markdown, matrix);
        appendRows(markdown, matrix);
        appendColumns(markdown, matrix);
        if (listRelationships) {
            appendRelationships(markdown, matrix);
        }

        return markdown.toString();
    }

    private static void appendCriteria(StringBuilder markdown, MatrixCriteriaSummary criteria, RelationshipMatrix matrix) {
        String kinds = criteria.relationshipKinds().stream()
                .map(MatrixRelationshipKind::label)
                .collect(Collectors.joining(", "));

        markdown.append("\n## Criteria\n");
        markdown.append("- Rows: ").append(oneLine(criteria.rows())).append(": ").append(chosen(matrix.rowElementCount(), matrix.rows().size(), matrix)).append('\n');
        markdown.append("- Columns: ").append(oneLine(criteria.columns())).append(": ").append(chosen(matrix.columnElementCount(), matrix.columns().size(), matrix)).append('\n');
        markdown.append("- Relationship kinds: ").append(kinds).append('\n');
        markdown.append("- Direction: ").append(criteria.direction().label()).append('\n');
        markdown.append("- Show elements: ").append(criteria.showElements().label()).append('\n');
        if (criteria.impliedRelationships()) {
            markdown.append("- Implied relationships: ").append(IMPLIED_RELATIONSHIPS).append('\n');
        }
        if (criteria.groupedByPackage()) {
            markdown.append("- Grouped by package: ").append(GROUPED_BY_PACKAGE).append('\n');
        }
        markdown.append("- Notation: ").append(NOTATION).append('\n');
    }

    // Such as "6 elements", or "6 elements in 2 packages" when the rows or the columns are the packages that hold them
    private static String chosen(int elements, int packages, RelationshipMatrix matrix) {
        String chosen = count(elements, "element");
        return matrix.groupedByPackage() ? chosen + " in " + count(packages, "package") : chosen;
    }

    // Counted over every element the criteria chose, before showElements leaves any out, so that the coverage reads the same whatever showElements is
    private static void appendSummary(StringBuilder markdown, RelationshipMatrix matrix) {
        List<String> rowsWithout = new ArrayList<>();
        int rowsWithoutNotListed = 0;
        for (int row = 0; row < matrix.rows().size(); row++) {
            if (matrix.rowCount(row) > 0) {
                continue;
            }
            if (matrix.isRowListed(row)) {
                rowsWithout.add(RelationshipMatrix.rowKey(row));
            } else {
                rowsWithoutNotListed++;
            }
        }

        List<String> columnsWithout = new ArrayList<>();
        int columnsWithoutNotListed = 0;
        for (int column = 0; column < matrix.columns().size(); column++) {
            if (matrix.columnCount(column) > 0) {
                continue;
            }
            if (matrix.isColumnListed(column)) {
                columnsWithout.add(RelationshipMatrix.columnKey(column));
            } else {
                columnsWithoutNotListed++;
            }
        }

        markdown.append("\n## Summary\n");
        markdown.append("- Relationships matching the criteria: ").append(matrix.relationshipCount()).append('\n');
        markdown.append("- Rows with relationships: ").append(ratio(matrix.rowsWithRelationships(), matrix.rows().size())).append('\n');
        markdown.append("- Columns with relationships: ").append(ratio(matrix.columnsWithRelationships(), matrix.columns().size())).append('\n');
        markdown.append("- Rows without relationships: ").append(keys(rowsWithout, rowsWithoutNotListed)).append('\n');
        markdown.append("- Columns without relationships: ").append(keys(columnsWithout, columnsWithoutNotListed)).append('\n');
    }

    private void appendGrid(StringBuilder markdown, RelationshipMatrix matrix) {
        markdown.append("\n## Matrix\n");

        List<Integer> rows = matrix.listedRows();
        List<Integer> columns = matrix.listedColumns();

        if (matrix.entriesBetweenListedElements().isEmpty()) {
            markdown.append(NO_RELATIONSHIP_BETWEEN_LISTED).append('\n');
            return;
        }

        if (rows.size() > maxGridRows || columns.size() > maxGridColumns) {
            markdown.append("Omitted: ").append(count(rows.size(), "row")).append(" and ").append(count(columns.size(), "column"))
                    .append(" are listed, over the limit of ").append(count(maxGridRows, "row")).append(" and ").append(count(maxGridColumns, "column"))
                    .append(". The Related columns list every relationship between them.\n");
            return;
        }

        List<String> header = new ArrayList<>();
        header.add("");
        for (int column : columns) {
            header.add(RelationshipMatrix.columnKey(column) + " " + name(matrix.columns().get(column).name()));
        }
        appendTableRow(markdown, header);
        appendSeparator(markdown, header.size());

        for (int row : rows) {
            List<String> cells = new ArrayList<>();
            cells.add(RelationshipMatrix.rowKey(row) + " " + name(matrix.rows().get(row).name()));

            List<RelationshipMatrix.Entry> rowEntries = matrix.entriesInRow(row);
            for (int column : columns) {
                cells.add(String.join(", ", counted(rowEntries.stream()
                        .filter(entry -> entry.column() == column)
                        .map(RelationshipMatrix.Entry::text)
                        .toList())));
            }
            appendTableRow(markdown, cells);
        }
    }

    // Each text once, with how many times it comes in a row when that is more than once, such as →dependency ×3.
    // The entries of a cell are sorted by text, so the same texts always come together.
    static List<String> counted(List<String> texts) {
        List<String> counted = new ArrayList<>();
        int start = 0;
        while (start < texts.size()) {
            int end = start;
            while (end < texts.size() && texts.get(end).equals(texts.get(start))) {
                end++;
            }
            int times = end - start;
            counted.add(times == 1 ? texts.get(start) : texts.get(start) + " ×" + times);
            start = end;
        }
        return counted;
    }

    private static void appendRows(StringBuilder markdown, RelationshipMatrix matrix) {
        markdown.append("\n## Rows\n");

        List<Integer> rows = matrix.listedRows();
        if (matrix.rows().isEmpty()) {
            markdown.append("No element matched the row criteria.\n");
            return;
        }
        if (rows.isEmpty()) {
            markdown.append("None of the ").append(count(matrix.rows().size(), matrix.groupedByPackage() ? "row package" : "row element"))
                    .append(" is listed, because showElements is ").append(matrix.showElements().label()).append(".\n");
            return;
        }

        appendTableRow(markdown, ELEMENT_COLUMNS);
        appendSeparator(markdown, ELEMENT_COLUMNS.size());
        for (int row : rows) {
            List<String> related = new ArrayList<>();
            int notListed = 0;
            for (RelationshipMatrix.Entry entry : matrix.entriesInRow(row)) {
                if (matrix.isColumnListed(entry.column())) {
                    related.add(RelationshipMatrix.columnKey(entry.column()) + " " + entry.text());
                } else {
                    notListed++;
                }
            }
            appendElement(markdown, RelationshipMatrix.rowKey(row), matrix.rows().get(row), matrix.rowCount(row), counted(related), notListed);
        }
    }

    private static void appendColumns(StringBuilder markdown, RelationshipMatrix matrix) {
        markdown.append("\n## Columns\n");

        List<Integer> columns = matrix.listedColumns();
        if (matrix.columns().isEmpty()) {
            markdown.append("No element matched the column criteria.\n");
            return;
        }
        if (columns.isEmpty()) {
            markdown.append("None of the ").append(count(matrix.columns().size(), matrix.groupedByPackage() ? "column package" : "column element"))
                    .append(" is listed, because showElements is ").append(matrix.showElements().label()).append(".\n");
            return;
        }

        appendTableRow(markdown, ELEMENT_COLUMNS);
        appendSeparator(markdown, ELEMENT_COLUMNS.size());
        for (int column : columns) {
            List<String> related = new ArrayList<>();
            int notListed = 0;
            for (RelationshipMatrix.Entry entry : matrix.entriesInColumn(column)) {
                if (matrix.isRowListed(entry.row())) {
                    related.add(RelationshipMatrix.rowKey(entry.row()) + " " + entry.text());
                } else {
                    notListed++;
                }
            }
            appendElement(markdown, RelationshipMatrix.columnKey(column), matrix.columns().get(column), matrix.columnCount(column), counted(related), notListed);
        }
    }

    // The symbol of a relationship in Related is the one of the cell it is placed in, read from the row element
    private static void appendElement(StringBuilder markdown, String key, MatrixElement element, int count, List<String> related, int notListed) {
        List<String> relatedParts = new ArrayList<>(related);
        if (notListed > 0) {
            relatedParts.add("(+" + notListed + " not listed)");
        }

        appendTableRow(markdown, List.of(
            key,
            name(element.name()),
            text(element.namespace()),
            text(element.type()),
            text(element.requirementOrTestCaseId()),
            text(element.id()),
            String.valueOf(count),
            String.join("; ", relatedParts)));
    }

    private static void appendRelationships(StringBuilder markdown, RelationshipMatrix matrix) {
        markdown.append("\n## Relationships\n");

        List<RelationshipMatrix.Entry> entries = matrix.entriesBetweenListedElements();
        if (entries.isEmpty()) {
            markdown.append("No relationship between the listed elements.\n");
            return;
        }

        appendTableRow(markdown, RELATIONSHIP_COLUMNS);
        appendSeparator(markdown, RELATIONSHIP_COLUMNS.size());
        for (RelationshipMatrix.Entry entry : entries) {
            String id = entry.relationship().id();
            appendTableRow(markdown, List.of(
                RelationshipMatrix.rowKey(entry.row()) + " " + name(matrix.rows().get(entry.row()).name()),
                RelationshipMatrix.columnKey(entry.column()) + " " + name(matrix.columns().get(entry.column()).name()),
                entry.text(),
                text(entry.detail()),
                id == null ? NO_ID : text(id)));
        }
    }

    private static void appendTableRow(StringBuilder markdown, List<String> cells) {
        markdown.append('|');
        for (String cell : cells) {
            markdown.append(cell.isEmpty() ? " " : " " + cell + " ").append('|');
        }
        markdown.append('\n');
    }

    private static void appendSeparator(StringBuilder markdown, int columns) {
        markdown.append('|');
        for (int i = 0; i < columns; i++) {
            markdown.append("---|");
        }
        markdown.append('\n');
    }

    // A value written into a table cell, which a vertical bar would end and a line break would break
    static String text(String value) {
        if (value == null) {
            return "";
        }
        return oneLine(value).replace("|", "\\|");
    }

    // A name written into a table cell, which shows that the element has none rather than an empty cell
    static String name(String name) {
        if (name == null || name.isBlank()) {
            return UNNAMED;
        }
        return text(name);
    }

    private static String oneLine(String value) {
        return value.replace("\r\n", " ").replace('\r', ' ').replace('\n', ' ');
    }

    private static String count(int count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }

    // Such as 3 / 5 (60%), with the percentage rounded half up
    private static String ratio(int part, int whole) {
        if (whole == 0) {
            return "0 / 0";
        }
        return part + " / " + whole + " (" + Math.round(100.0 * part / whole) + "%)";
    }

    // The keys of the listed elements, and how many more there are that showElements left out
    private static String keys(List<String> listed, int notListed) {
        List<String> parts = new ArrayList<>(listed);
        if (notListed > 0) {
            parts.add(notListed + " (not listed)");
        }
        return parts.isEmpty() ? "none" : String.join(", ", parts);
    }
}
