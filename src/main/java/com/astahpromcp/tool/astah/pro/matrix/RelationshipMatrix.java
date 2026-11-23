package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.common.MatrixDirection;
import com.astahpromcp.tool.astah.pro.common.MatrixShowElements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// The rows and the columns of a relationship matrix, the cells its relationships are placed in, and what is counted of them.
final class RelationshipMatrix {

    // The symbols a cell shows, read from the row element of the cell
    static final String FROM_ROW = "→";
    static final String FROM_COLUMN = "←";
    static final String BOTH_WAYS = "↔";
    static final String NO_DIRECTION = "—";

    // The rows and the columns are sorted by full name in natural order, and by ID among elements of one full name, so that every call lists them in the same order
    private static final Comparator<MatrixElement> ELEMENT_ORDER = Comparator
            .comparing(MatrixElement::fullName, NaturalOrderComparator.INSTANCE)
            .thenComparing(MatrixElement::id);

    // One relationship placed in one cell, with the symbol it has there.
    // detail says more about it in the list of relationships, such as the elements it joins when the rows and the columns are packages.
    record Entry(int row, int column, String symbol, MatrixRelationship relationship, String detail) {

        // What the cell shows of the relationship, such as ←satisfy
        String text() {
            return symbol + relationship.label();
        }
    }

    private static final Comparator<Entry> ENTRY_ORDER = Comparator
            .comparingInt(Entry::row)
            .thenComparingInt(Entry::column)
            .thenComparing(Entry::text)
            .thenComparing(Entry::detail)
            .thenComparing(entry -> entry.relationship().key());

    private final List<MatrixElement> rows;
    private final List<MatrixElement> columns;
    private final MatrixShowElements showElements;
    private final List<Entry> entries;
    private final List<List<Entry>> entriesByRow;
    private final List<List<Entry>> entriesByColumn;
    private final int relationshipCount;
    private final boolean groupedByPackage;
    private final int rowElementCount;
    private final int columnElementCount;

    private RelationshipMatrix(
            List<MatrixElement> rows,
            List<MatrixElement> columns,
            MatrixShowElements showElements,
            List<Entry> entries,
            boolean groupedByPackage,
            int rowElementCount,
            int columnElementCount) {
        this.rows = rows;
        this.columns = columns;
        this.showElements = showElements;
        this.entries = entries;
        this.groupedByPackage = groupedByPackage;
        this.rowElementCount = rowElementCount;
        this.columnElementCount = columnElementCount;

        // The entries are sorted by row and then by column, so each row keeps them by column and each column by row
        this.entriesByRow = emptyLists(rows.size());
        this.entriesByColumn = emptyLists(columns.size());

        // A relationship placed in two cells, as one between two elements that are both rows and columns is, is still one relationship
        Set<String> placedKeys = new HashSet<>();
        for (Entry entry : entries) {
            placedKeys.add(entry.relationship().key());
            entriesByRow.get(entry.row()).add(entry);
            entriesByColumn.get(entry.column()).add(entry);
        }
        this.relationshipCount = placedKeys.size();
    }

    private static List<List<Entry>> emptyLists(int size) {
        List<List<Entry>> lists = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            lists.add(new ArrayList<>());
        }
        return lists;
    }

    // Sorts the elements, places each relationship in the cells its direction and ends allow, and counts them.
    // An element is taken once even if it is given twice, and a relationship whose ends are not both rows or columns is placed nowhere.
    static RelationshipMatrix of(
            List<MatrixElement> rowElements,
            List<MatrixElement> columnElements,
            List<MatrixRelationship> relationships,
            MatrixDirection direction,
            MatrixShowElements showElements) {

        List<MatrixElement> rows = sortedOnce(rowElements);
        List<MatrixElement> columns = sortedOnce(columnElements);
        Map<String, Integer> rowIndexes = indexesById(rows);
        Map<String, Integer> columnIndexes = indexesById(columns);

        List<Entry> entries = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        for (MatrixRelationship relationship : relationships) {
            if (!seenKeys.add(relationship.key())) {
                continue;
            }
            place(relationship, rowIndexes, columnIndexes, direction, entries);
        }
        entries.sort(ENTRY_ORDER);

        return new RelationshipMatrix(rows, columns, showElements, List.copyOf(entries), false, rows.size(), columns.size());
    }

    private static void place(
            MatrixRelationship relationship,
            Map<String, Integer> rowIndexes,
            Map<String, Integer> columnIndexes,
            MatrixDirection direction,
            List<Entry> entries) {

        String sourceId = relationship.sourceId();
        String targetId = relationship.targetId();

        String symbol = switch (relationship.orientation()) {
            case DIRECTED -> FROM_ROW;
            case BIDIRECTIONAL -> BOTH_WAYS;
            case UNDIRECTED -> NO_DIRECTION;
        };

        // A relationship of an element to itself has one cell, where it is placed once whatever the direction
        if (sourceId.equals(targetId)) {
            addIfBoth(rowIndexes.get(sourceId), columnIndexes.get(sourceId), symbol, relationship, entries);
            return;
        }

        // A relationship without a direction, or with both, is shown whatever the direction
        if (relationship.orientation() != MatrixRelationship.Orientation.DIRECTED) {
            addIfBoth(rowIndexes.get(sourceId), columnIndexes.get(targetId), symbol, relationship, entries);
            addIfBoth(rowIndexes.get(targetId), columnIndexes.get(sourceId), symbol, relationship, entries);
            return;
        }

        if (direction.showsRowToColumn()) {
            addIfBoth(rowIndexes.get(sourceId), columnIndexes.get(targetId), FROM_ROW, relationship, entries);
        }
        if (direction.showsColumnToRow()) {
            addIfBoth(rowIndexes.get(targetId), columnIndexes.get(sourceId), FROM_COLUMN, relationship, entries);
        }
    }

    private static void addIfBoth(Integer row, Integer column, String symbol, MatrixRelationship relationship, List<Entry> entries) {
        if (row != null && column != null) {
            entries.add(new Entry(row, column, symbol, relationship, relationship.detail()));
        }
    }

    // The same matrix with each element replaced by the package that holds it, as a dependency structure matrix shows packages.
    // Every relationship stays in the cell of the packages of the row and the column it was placed in, so that the directions and the cells read as they did between the elements.
    // A relationship within one package is left out, as it says nothing of how packages depend on one another.
    RelationshipMatrix groupedBy(Map<String, MatrixElement> packagesByElementId) {
        List<MatrixElement> rowPackages = sortedOnce(rows.stream().map(element -> packageOf(packagesByElementId, element)).toList());
        List<MatrixElement> columnPackages = sortedOnce(columns.stream().map(element -> packageOf(packagesByElementId, element)).toList());
        Map<String, Integer> rowIndexes = indexesById(rowPackages);
        Map<String, Integer> columnIndexes = indexesById(columnPackages);

        List<Entry> packageEntries = new ArrayList<>();
        for (Entry entry : entries) {
            MatrixElement rowElement = rows.get(entry.row());
            MatrixElement columnElement = columns.get(entry.column());
            MatrixElement rowPackage = packageOf(packagesByElementId, rowElement);
            MatrixElement columnPackage = packageOf(packagesByElementId, columnElement);
            if (rowPackage.id().equals(columnPackage.id())) {
                continue;
            }

            // The elements the relationship joins, read from the row as the cell is, such as: Logger ← Car
            String detail = rowElement.displayName() + " " + entry.symbol() + " " + columnElement.displayName()
                    + (entry.detail().isEmpty() ? "" : " (" + entry.detail() + ")");
            packageEntries.add(new Entry(rowIndexes.get(rowPackage.id()), columnIndexes.get(columnPackage.id()), entry.symbol(), entry.relationship(), detail));
        }
        packageEntries.sort(ENTRY_ORDER);

        return new RelationshipMatrix(rowPackages, columnPackages, showElements, List.copyOf(packageEntries), true, rows.size(), columns.size());
    }

    private static MatrixElement packageOf(Map<String, MatrixElement> packagesByElementId, MatrixElement element) {
        MatrixElement holder = packagesByElementId.get(element.id());
        if (holder == null) {
            throw new IllegalStateException("No package is given for the element " + element.id());
        }
        return holder;
    }

    private static List<MatrixElement> sortedOnce(List<MatrixElement> elements) {
        Map<String, MatrixElement> byId = new LinkedHashMap<>();
        for (MatrixElement element : elements) {
            byId.putIfAbsent(element.id(), element);
        }
        List<MatrixElement> sorted = new ArrayList<>(byId.values());
        sorted.sort(ELEMENT_ORDER);
        return List.copyOf(sorted);
    }

    private static Map<String, Integer> indexesById(List<MatrixElement> elements) {
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < elements.size(); i++) {
            indexes.put(elements.get(i).id(), i);
        }
        return indexes;
    }

    // Every row the criteria chose, listed or not, in the order of their keys: elements, or the packages that hold them
    List<MatrixElement> rows() {
        return rows;
    }

    // Every column the criteria chose, listed or not, in the order of their keys
    List<MatrixElement> columns() {
        return columns;
    }

    // Whether the rows and the columns are the packages that hold the elements the criteria chose
    boolean groupedByPackage() {
        return groupedByPackage;
    }

    // The number of elements the row criteria chose, which is the number of rows unless they are grouped by package
    int rowElementCount() {
        return rowElementCount;
    }

    int columnElementCount() {
        return columnElementCount;
    }

    // The keys are given before showElements leaves any element out, so that they stay the same whatever it is
    static String rowKey(int row) {
        return "R" + (row + 1);
    }

    static String columnKey(int column) {
        return "C" + (column + 1);
    }

    MatrixShowElements showElements() {
        return showElements;
    }

    // Every placement of a relationship, by row, then by column
    List<Entry> entries() {
        return entries;
    }

    // The number of relationships placed in at least one cell, each counted once
    int relationshipCount() {
        return relationshipCount;
    }

    // The number of relationships in the row, counted before showElements leaves any row or column out
    int rowCount(int row) {
        return entriesByRow.get(row).size();
    }

    int columnCount(int column) {
        return entriesByColumn.get(column).size();
    }

    boolean isRowListed(int row) {
        return showElements.listsRow(rowCount(row));
    }

    boolean isColumnListed(int column) {
        return showElements.listsColumn(columnCount(column));
    }

    List<Integer> listedRows() {
        List<Integer> listed = new ArrayList<>();
        for (int row = 0; row < rows.size(); row++) {
            if (isRowListed(row)) {
                listed.add(row);
            }
        }
        return listed;
    }

    List<Integer> listedColumns() {
        List<Integer> listed = new ArrayList<>();
        for (int column = 0; column < columns.size(); column++) {
            if (isColumnListed(column)) {
                listed.add(column);
            }
        }
        return listed;
    }

    int rowsWithRelationships() {
        return (int) entriesByRow.stream().filter(list -> !list.isEmpty()).count();
    }

    int columnsWithRelationships() {
        return (int) entriesByColumn.stream().filter(list -> !list.isEmpty()).count();
    }

    // The placements in the row, by column
    List<Entry> entriesInRow(int row) {
        return List.copyOf(entriesByRow.get(row));
    }

    // The placements in the column, by row
    List<Entry> entriesInColumn(int column) {
        return List.copyOf(entriesByColumn.get(column));
    }

    // The placements whose row and column are both listed, by row, then by column
    List<Entry> entriesBetweenListedElements() {
        return entries.stream()
                .filter(entry -> isRowListed(entry.row()) && isColumnListed(entry.column()))
                .toList();
    }
}
