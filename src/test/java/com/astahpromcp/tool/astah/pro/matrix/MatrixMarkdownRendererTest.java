package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.common.MatrixDirection;
import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.astahpromcp.tool.astah.pro.common.MatrixShowElements;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind.*;
import static org.junit.jupiter.api.Assertions.*;

// Places, counts and writes a relationship matrix built from plain values, without Astah
public class MatrixMarkdownRendererTest {

    private static final int MAX_GRID_ROWS = 50;
    private static final int MAX_GRID_COLUMNS = 20;

    private static MatrixElement requirement(String id, String name, String namespace, String requirementId) {
        return new MatrixElement(id, name, namespace, namespace + "." + name, "Requirement", requirementId);
    }

    private static MatrixElement block(String id, String name) {
        return new MatrixElement(id, name, "Design", "Design." + name, "Class", "");
    }

    private static MatrixRelationship directed(String id, String sourceId, String targetId, MatrixRelationshipKind... kinds) {
        return MatrixRelationship.directed(id, id, sourceId, targetId, List.of(kinds));
    }

    // The requirements and blocks of the examples of the proposal: three blocks satisfy three of the five requirements
    private static final List<MatrixElement> REQUIREMENTS = List.of(
        requirement("r5", "Weight", "Requirements", "REQ-004"),
        requirement("r1", "Noise level", "Requirements", "REQ-001"),
        requirement("r4", "Wet road", "Requirements.Stopping distance", "REQ-003-1"),
        requirement("r3", "Stopping distance", "Requirements", "REQ-003"),
        requirement("r2", "Output power", "Requirements", "REQ-002"));

    private static final List<MatrixElement> BLOCKS = List.of(
        block("c3", "Wheel"),
        block("c1", "BrakeSystem"),
        block("c2", "Engine"));

    private static final List<MatrixRelationship> SATISFY_DEPENDENCIES = List.of(
        directed("s1", "c1", "r3", SATISFY),
        directed("s2", "c1", "r4", SATISFY),
        directed("s3", "c2", "r2", SATISFY));

    private static String render(
            List<MatrixElement> rows,
            List<MatrixElement> columns,
            List<MatrixRelationship> relationships,
            MatrixDirection direction,
            MatrixShowElements showElements,
            boolean listRelationships) {
        return render(new MatrixMarkdownRenderer(MAX_GRID_ROWS, MAX_GRID_COLUMNS), rows, columns, relationships, direction, showElements, listRelationships);
    }

    private static String render(
            MatrixMarkdownRenderer renderer,
            List<MatrixElement> rows,
            List<MatrixElement> columns,
            List<MatrixRelationship> relationships,
            MatrixDirection direction,
            MatrixShowElements showElements,
            boolean listRelationships) {

        List<MatrixRelationshipKind> kinds = new ArrayList<>();
        for (MatrixRelationship relationship : relationships) {
            for (MatrixRelationshipKind kind : relationship.kinds()) {
                if (!kinds.contains(kind)) {
                    kinds.add(kind);
                }
            }
        }
        if (kinds.isEmpty()) {
            kinds.add(SATISFY);
        }

        RelationshipMatrix matrix = RelationshipMatrix.of(rows, columns, relationships, direction, showElements);
        MatrixCriteriaSummary criteria = new MatrixCriteriaSummary(
            "Requirement in Package \"Requirements\" (nested elements included)",
            "Class with stereotype block in Package \"Design\" (nested elements included)",
            kinds,
            direction,
            showElements);
        return renderer.render(criteria, matrix, listRelationships);
    }

    // The numbers a line is made of, in order, as the counts of elements and packages in the criteria
    private static List<Integer> numbersIn(String text) {
        return Pattern.compile("\\d+").matcher(text).results()
            .map(match -> Integer.parseInt(match.group()))
            .toList();
    }

    private static MatrixMarkdown satisfyMatrix(MatrixShowElements showElements) {
        return new MatrixMarkdown(render(REQUIREMENTS, BLOCKS, SATISFY_DEPENDENCIES, MatrixDirection.COLUMN_TO_ROW, showElements, false));
    }

    @Test
    void render_ok_writesTheFirstExampleOfTheProposal() {
        MatrixMarkdown markdown = satisfyMatrix(MatrixShowElements.ALL);

        // The criteria name the element types, the stereotype and the scopes, and count the elements.
        String rows = markdown.item("Criteria", "Rows");
        assertTrue(rows.contains("Requirement"), rows);
        assertTrue(rows.contains("\"Requirements\""), rows);
        assertEquals(List.of(5), numbersIn(rows));
        String columns = markdown.item("Criteria", "Columns");
        assertTrue(columns.contains("Class"), columns);
        assertTrue(columns.contains("block"), columns);
        assertTrue(columns.contains("\"Design\""), columns);
        assertEquals(List.of(3), numbersIn(columns));
        assertEquals("satisfy", markdown.item("Criteria", "Relationship kinds"));
        assertEquals("column_to_row", markdown.item("Criteria", "Direction"));
        assertEquals("all", markdown.item("Criteria", "Show elements"));

        assertEquals("3", markdown.item("Summary", "Relationships matching the criteria"));
        assertEquals("3 / 5 (60%)", markdown.item("Summary", "Rows with relationships"));
        assertEquals("2 / 3 (67%)", markdown.item("Summary", "Columns with relationships"));
        assertEquals("R1, R5", markdown.item("Summary", "Rows without relationships"));
        assertEquals("C3", markdown.item("Summary", "Columns without relationships"));

        assertEquals(List.of(
            "| | C1 BrakeSystem | C2 Engine | C3 Wheel |",
            "|---|---|---|---|",
            "| R1 Noise level | | | |",
            "| R2 Output power | | ←satisfy | |",
            "| R3 Stopping distance | ←satisfy | | |",
            "| R4 Wet road | ←satisfy | | |",
            "| R5 Weight | | | |"), markdown.section("Matrix"));
        assertEquals(List.of(
            "| Key | Name | Namespace | Type | Req/Test ID | ID | Count | Related |",
            "|---|---|---|---|---|---|---|---|",
            "| R1 | Noise level | Requirements | Requirement | REQ-001 | r1 | 0 | |",
            "| R2 | Output power | Requirements | Requirement | REQ-002 | r2 | 1 | C2 ←satisfy |",
            "| R3 | Stopping distance | Requirements | Requirement | REQ-003 | r3 | 1 | C1 ←satisfy |",
            "| R4 | Wet road | Requirements.Stopping distance | Requirement | REQ-003-1 | r4 | 1 | C1 ←satisfy |",
            "| R5 | Weight | Requirements | Requirement | REQ-004 | r5 | 0 | |"), markdown.section("Rows"));
        assertEquals(List.of(
            "| Key | Name | Namespace | Type | Req/Test ID | ID | Count | Related |",
            "|---|---|---|---|---|---|---|---|",
            "| C1 | BrakeSystem | Design | Class | | c1 | 2 | R3 ←satisfy; R4 ←satisfy |",
            "| C2 | Engine | Design | Class | | c2 | 1 | R2 ←satisfy |",
            "| C3 | Wheel | Design | Class | | c3 | 0 | |"), markdown.section("Columns"));
    }

    @Test
    void render_ok_writesTheSecondExampleOfTheProposal() {
        MatrixMarkdown markdown = satisfyMatrix(MatrixShowElements.ROWS_WITHOUT_RELATIONSHIPS);

        assertEquals(MatrixMarkdownRenderer.NO_RELATIONSHIP_BETWEEN_LISTED, markdown.line("Matrix"));
        assertEquals(List.of(
            "| Key | Name | Namespace | Type | Req/Test ID | ID | Count | Related |",
            "|---|---|---|---|---|---|---|---|",
            "| R1 | Noise level | Requirements | Requirement | REQ-001 | r1 | 0 | |",
            "| R5 | Weight | Requirements | Requirement | REQ-004 | r5 | 0 | |"),
            markdown.section("Rows"));
        assertEquals(List.of(
            "| Key | Name | Namespace | Type | Req/Test ID | ID | Count | Related |",
            "|---|---|---|---|---|---|---|---|",
            "| C1 | BrakeSystem | Design | Class | | c1 | 2 | (+2 not listed) |",
            "| C2 | Engine | Design | Class | | c2 | 1 | (+1 not listed) |",
            "| C3 | Wheel | Design | Class | | c3 | 0 | |"),
            markdown.section("Columns"));
    }

    // The keys, the counts and the summary are worked out before showElements leaves any element out
    @Test
    void render_ok_keepsKeysCountsAndSummaryWhateverShowElementsIs() {
        MatrixMarkdown all = satisfyMatrix(MatrixShowElements.ALL);
        Map<String, Map<String, String>> allRows = all.table("Rows");
        Map<String, Map<String, String>> allColumns = all.table("Columns");

        for (MatrixShowElements showElements : MatrixShowElements.values()) {
            MatrixMarkdown markdown = satisfyMatrix(showElements);

            for (String label : List.of("Relationships matching the criteria", "Rows with relationships", "Columns with relationships")) {
                assertEquals(all.item("Summary", label), markdown.item("Summary", label), showElements + ": " + label);
            }

            if (!markdown.section("Rows").get(0).startsWith("|")) {
                continue;
            }
            for (Map.Entry<String, Map<String, String>> row : markdown.table("Rows").entrySet()) {
                assertEquals(allRows.get(row.getKey()).get("ID"), row.getValue().get("ID"), showElements + ": " + row.getKey());
                assertEquals(allRows.get(row.getKey()).get("Count"), row.getValue().get("Count"), showElements + ": " + row.getKey());
            }
            for (Map.Entry<String, Map<String, String>> column : markdown.table("Columns").entrySet()) {
                assertEquals(allColumns.get(column.getKey()).get("ID"), column.getValue().get("ID"), showElements + ": " + column.getKey());
                assertEquals(allColumns.get(column.getKey()).get("Count"), column.getValue().get("Count"), showElements + ": " + column.getKey());
            }
        }
    }

    @Test
    void render_ok_listsTheElementsEachValueOfShowElementsAsks() {
        assertEquals(List.of("R1", "R2", "R3", "R4", "R5"), List.copyOf(satisfyMatrix(MatrixShowElements.ALL).table("Rows").keySet()));
        assertEquals(List.of("C1", "C2", "C3"), List.copyOf(satisfyMatrix(MatrixShowElements.ALL).table("Columns").keySet()));

        assertEquals(List.of("R2", "R3", "R4"), List.copyOf(satisfyMatrix(MatrixShowElements.WITH_RELATIONSHIPS).table("Rows").keySet()));
        assertEquals(List.of("C1", "C2"), List.copyOf(satisfyMatrix(MatrixShowElements.WITH_RELATIONSHIPS).table("Columns").keySet()));

        assertEquals(List.of("R1", "R5"), List.copyOf(satisfyMatrix(MatrixShowElements.WITHOUT_RELATIONSHIPS).table("Rows").keySet()));
        assertEquals(List.of("C3"), List.copyOf(satisfyMatrix(MatrixShowElements.WITHOUT_RELATIONSHIPS).table("Columns").keySet()));

        assertEquals(List.of("R1", "R5"), List.copyOf(satisfyMatrix(MatrixShowElements.ROWS_WITHOUT_RELATIONSHIPS).table("Rows").keySet()));
        assertEquals(List.of("C1", "C2", "C3"), List.copyOf(satisfyMatrix(MatrixShowElements.ROWS_WITHOUT_RELATIONSHIPS).table("Columns").keySet()));

        assertEquals(List.of("R1", "R2", "R3", "R4", "R5"), List.copyOf(satisfyMatrix(MatrixShowElements.COLUMNS_WITHOUT_RELATIONSHIPS).table("Rows").keySet()));
        assertEquals(List.of("C3"), List.copyOf(satisfyMatrix(MatrixShowElements.COLUMNS_WITHOUT_RELATIONSHIPS).table("Columns").keySet()));
    }

    @Test
    void render_ok_countsTheElementsWithoutRelationshipsThatAreNotListed() {
        MatrixMarkdown markdown = satisfyMatrix(MatrixShowElements.WITH_RELATIONSHIPS);

        assertEquals("2 (not listed)", markdown.item("Summary", "Rows without relationships"));
        assertEquals("1 (not listed)", markdown.item("Summary", "Columns without relationships"));
        assertEquals("| R3 Stopping distance | ←satisfy | |", markdown.section("Matrix").get(3));
    }

    @Test
    void render_ok_omitsTheGridOverTheLimit() {
        MatrixMarkdownRenderer renderer = new MatrixMarkdownRenderer(4, MAX_GRID_COLUMNS);

        MatrixMarkdown markdown = new MatrixMarkdown(render(renderer, REQUIREMENTS, BLOCKS, SATISFY_DEPENDENCIES, MatrixDirection.COLUMN_TO_ROW, MatrixShowElements.ALL, false));

        assertEquals("Omitted: 5 rows and 3 columns are listed, over the limit of 4 rows and 20 columns. The Related columns list every relationship between them.", markdown.line("Matrix"));
        assertEquals("C1 ←satisfy", markdown.table("Rows").get("R3").get("Related"), "the relationships are listed all the same");
    }

    @Test
    void render_ok_drawsTheGridAtTheLimit() {
        MatrixMarkdownRenderer renderer = new MatrixMarkdownRenderer(5, 3);

        MatrixMarkdown markdown = new MatrixMarkdown(render(renderer, REQUIREMENTS, BLOCKS, SATISFY_DEPENDENCIES, MatrixDirection.COLUMN_TO_ROW, MatrixShowElements.ALL, false));

        assertEquals("| | C1 BrakeSystem | C2 Engine | C3 Wheel |", markdown.section("Matrix").get(0));
    }

    @Test
    void render_ok_escapesVerticalBarsAndLineBreaksAndNamesTheUnnamed() {
        List<MatrixElement> rows = List.of(
            new MatrixElement("a", "Speed | torque\r\nlimit", "Spec|A", "Spec|A.Speed | torque\r\nlimit", "Requirement", "REQ|1"),
            new MatrixElement("b", "", "Spec|B", "Spec|B.", "Requirement", ""));
        List<MatrixElement> columns = List.of(block("c", "Brake\nSystem"));
        List<MatrixRelationship> relationships = List.of(directed("s", "c", "a", SATISFY), directed("t", "c", "b", SATISFY));

        MatrixMarkdown markdown = new MatrixMarkdown(render(rows, columns, relationships, MatrixDirection.BOTH, MatrixShowElements.ALL, false));

        assertTrue(markdown.text().contains("| R1 | Speed \\| torque limit | Spec\\|A | Requirement | REQ\\|1 | a | 1 | C1 ←satisfy |"), markdown.text());
        assertTrue(markdown.text().contains("| R2 | (unnamed) | Spec\\|B | Requirement | | b | 1 | C1 ←satisfy |"), markdown.text());
        assertEquals("| | C1 Brake System |", markdown.section("Matrix").get(0));
        assertEquals("| R1 Speed \\| torque limit | ←satisfy |", markdown.section("Matrix").get(2));
        assertEquals("Speed | torque limit", markdown.table("Rows").get("R1").get("Name"), "the bar is escaped, not dropped");
    }

    @Test
    void render_ok_showsADirectedRelationshipOnlyInTheDirectionAskedFor() {
        List<MatrixElement> rows = List.of(block("a", "A"));
        List<MatrixElement> columns = List.of(block("b", "B"));
        List<MatrixRelationship> relationships = List.of(directed("d", "a", "b", DEPENDENCY));

        assertEquals("C1 →dependency", relatedOfFirstRow(rows, columns, relationships, MatrixDirection.ROW_TO_COLUMN));
        assertEquals("", relatedOfFirstRow(rows, columns, relationships, MatrixDirection.COLUMN_TO_ROW));
        assertEquals("C1 →dependency", relatedOfFirstRow(rows, columns, relationships, MatrixDirection.BOTH));

        // The same relationship read from the other side
        assertEquals("", relatedOfFirstRow(columns, rows, relationships, MatrixDirection.ROW_TO_COLUMN));
        assertEquals("C1 ←dependency", relatedOfFirstRow(columns, rows, relationships, MatrixDirection.COLUMN_TO_ROW));
        assertEquals("C1 ←dependency", relatedOfFirstRow(columns, rows, relationships, MatrixDirection.BOTH));
    }

    @Test
    void render_ok_showsARelationshipWithoutADirectionOrWithBothWhateverTheDirectionIs() {
        List<MatrixElement> rows = List.of(block("a", "A"));
        List<MatrixElement> columns = List.of(block("b", "B"));

        for (MatrixDirection direction : MatrixDirection.values()) {
            assertEquals("C1 —association", relatedOfFirstRow(rows, columns, List.of(MatrixRelationship.undirected("u", "a", "b", ASSOCIATION)), direction), direction.label());
            assertEquals("C1 —association", relatedOfFirstRow(rows, columns, List.of(MatrixRelationship.undirected("u", "b", "a", ASSOCIATION)), direction), direction.label());
            assertEquals("C1 ↔association", relatedOfFirstRow(rows, columns, List.of(MatrixRelationship.bidirectional("n", "a", "b", ASSOCIATION)), direction), direction.label());
        }
    }

    private static String relatedOfFirstRow(List<MatrixElement> rows, List<MatrixElement> columns, List<MatrixRelationship> relationships, MatrixDirection direction) {
        MatrixMarkdown markdown = new MatrixMarkdown(render(rows, columns, relationships, direction, MatrixShowElements.ALL, false));
        return markdown.table("Rows").get("R1").get("Related");
    }

    // An element that is both a row and a column has a relationship to another such element placed in two cells
    @Test
    void render_ok_placesARelationshipBetweenElementsThatAreRowsAndColumnsInTwoCellsAndCountsItOnce() {
        List<MatrixElement> requirements = List.of(
            requirement("a", "Derived", "Requirements", ""),
            requirement("b", "Source", "Requirements", ""));
        List<MatrixRelationship> relationships = List.of(directed("d", "a", "b", DERIVE_REQT));

        MatrixMarkdown markdown = new MatrixMarkdown(render(requirements, requirements, relationships, MatrixDirection.BOTH, MatrixShowElements.ALL, false));

        assertEquals(List.of(
            "| | C1 Derived | C2 Source |",
            "|---|---|---|",
            "| R1 Derived | | →derive_reqt |",
            "| R2 Source | ←derive_reqt | |"),
            markdown.section("Matrix"));
        assertEquals("1", markdown.item("Summary", "Relationships matching the criteria"));
        assertEquals("1", markdown.table("Rows").get("R1").get("Count"));
        assertEquals("1", markdown.table("Rows").get("R2").get("Count"));
    }

    @Test
    void render_ok_placesARelationshipOfAnElementWithItselfOnceWithTheSymbolOfItsDirection() {
        List<MatrixElement> elements = List.of(block("a", "A"));

        for (MatrixDirection direction : MatrixDirection.values()) {
            assertEquals("C1 —association", relatedOfFirstRow(elements, elements, List.of(MatrixRelationship.undirected("u", "a", "a", ASSOCIATION)), direction), direction.label());
            assertEquals("C1 ↔association", relatedOfFirstRow(elements, elements, List.of(MatrixRelationship.bidirectional("n", "a", "a", ASSOCIATION)), direction), direction.label());
            assertEquals("C1 →dependency", relatedOfFirstRow(elements, elements, List.of(directed("d", "a", "a", DEPENDENCY)), direction), direction.label());
        }
    }

    @Test
    void render_ok_joinsTheRelationshipsOfOneCellAndTheKindsOfOneRelationship() {
        List<MatrixElement> rows = List.of(requirement("r", "Stopping distance", "Requirements", ""));
        List<MatrixElement> columns = List.of(block("c", "BrakeSystem"));
        List<MatrixRelationship> relationships = List.of(
            directed("v", "c", "r", VERIFY),
            directed("s", "c", "r", SATISFY, TRACE));

        MatrixMarkdown markdown = new MatrixMarkdown(render(rows, columns, relationships, MatrixDirection.BOTH, MatrixShowElements.ALL, false));

        assertEquals("| R1 Stopping distance | ←satisfy+trace, ←verify |", markdown.section("Matrix").get(2));
        assertEquals("C1 ←satisfy+trace; C1 ←verify", markdown.table("Rows").get("R1").get("Related"));
        assertEquals("2", markdown.item("Summary", "Relationships matching the criteria"));
    }

    @Test
    void render_ok_listsTheRelationshipsWithTheirIdsAndNoneForAContainment() {
        List<MatrixElement> rows = List.of(requirement("p", "Parent", "Requirements", ""));
        List<MatrixElement> columns = List.of(
            requirement("c", "Child", "Requirements.Parent", ""),
            requirement("o", "Other", "Requirements", ""));
        List<MatrixRelationship> relationships = List.of(
            MatrixRelationship.directed("p>c", null, "p", "c", List.of(CONTAINMENT)),
            directed("t1", "p", "o", TRACE));

        MatrixMarkdown markdown = new MatrixMarkdown(render(rows, columns, relationships, MatrixDirection.BOTH, MatrixShowElements.ALL, true));

        assertEquals(List.of(
            "| Row | Column | Relationship | Detail | ID |",
            "|---|---|---|---|---|",
            "| R1 Parent | C1 Other | →trace | | t1 |",
            "| R1 Parent | C2 Child | →containment | | " + MatrixMarkdownRenderer.NO_ID + " |"),
            markdown.section("Relationships"));
    }

    private static MatrixMarkdown renderWith(RelationshipMatrix matrix, boolean impliedRelationships, boolean groupedByPackage) {
        MatrixCriteriaSummary criteria = new MatrixCriteriaSummary(
            "Requirement in the whole project",
            "Class in the whole project",
            List.of(SATISFY),
            MatrixDirection.BOTH,
            MatrixShowElements.ALL,
            impliedRelationships,
            groupedByPackage);
        return new MatrixMarkdown(new MatrixMarkdownRenderer(MAX_GRID_ROWS, MAX_GRID_COLUMNS).render(criteria, matrix, true));
    }

    @Test
    void render_ok_marksAnImpliedRelationshipAndListsThePartsItComesThrough() {
        List<MatrixElement> rows = List.of(requirement("w", "Weight", "Requirements", "REQ-004"));
        List<MatrixElement> columns = List.of(block("b", "BrakeSystem"));
        RelationshipMatrix matrix = RelationshipMatrix.of(
            rows,
            columns,
            List.of(MatrixRelationship.implied("b", "w", SATISFY, "via Caliper, Pad")),
            MatrixDirection.BOTH,
            MatrixShowElements.ALL);

        MatrixMarkdown markdown = renderWith(matrix, true, false);

        assertEquals("| R1 Weight | ←satisfy (implied) |", markdown.section("Matrix").get(2));
        assertEquals("C1 ←satisfy (implied)", markdown.table("Rows").get("R1").get("Related"));
        assertEquals("| R1 Weight | C1 BrakeSystem | ←satisfy (implied) | via Caliper, Pad | " + MatrixMarkdownRenderer.NO_ID + " |", markdown.section("Relationships").get(2));
        assertEquals(MatrixMarkdownRenderer.IMPLIED_RELATIONSHIPS, markdown.item("Criteria", "Implied relationships"));
        assertFalse(markdown.text().contains("- Grouped by package:"), markdown.text());
    }

    @Test
    void render_ok_countsTheRelationshipsOfOneKindInOneCell() {
        List<MatrixElement> rows = List.of(block("a", "A"));
        List<MatrixElement> columns = List.of(block("b", "B"));
        List<MatrixRelationship> relationships = List.of(directed("d1", "a", "b", DEPENDENCY), directed("d2", "a", "b", DEPENDENCY), directed("u", "a", "b", USAGE));

        MatrixMarkdown markdown = new MatrixMarkdown(render(rows, columns, relationships, MatrixDirection.BOTH, MatrixShowElements.ALL, false));

        assertEquals("| R1 A | →dependency ×2, →usage |", markdown.section("Matrix").get(2));
        assertEquals("C1 →dependency ×2; C1 →usage", markdown.table("Rows").get("R1").get("Related"));
        assertEquals("R1 →dependency ×2; R1 →usage", markdown.table("Columns").get("C1").get("Related"));
        assertEquals("3", markdown.table("Rows").get("R1").get("Count"));
        assertEquals("3", markdown.item("Summary", "Relationships matching the criteria"));
    }

    // A1 and A2 are in package P, B1 in package Q
    @Test
    void groupedBy_ok_showsHowThePackagesOfTheElementsRelate() {
        MatrixElement a1 = new MatrixElement("a1", "A1", "P", "P.A1", "Class", "");
        MatrixElement a2 = new MatrixElement("a2", "A2", "P", "P.A2", "Class", "");
        MatrixElement b1 = new MatrixElement("b1", "B1", "Q", "Q.B1", "Class", "");
        MatrixElement p = new MatrixElement("p", "P", "", "P", "Package", "");
        MatrixElement q = new MatrixElement("q", "Q", "", "Q", "Package", "");
        List<MatrixElement> elements = List.of(a1, a2, b1);
        List<MatrixRelationship> relationships = List.of(
            directed("d1", "a1", "b1", DEPENDENCY),
            directed("d2", "a2", "b1", DEPENDENCY),
            directed("d3", "a1", "a2", DEPENDENCY),
            MatrixRelationship.directed("u1", "u1", "b1", "a1", List.of(USAGE)));

        RelationshipMatrix matrix = RelationshipMatrix.of(elements, elements, relationships, MatrixDirection.ROW_TO_COLUMN, MatrixShowElements.ALL)
            .groupedBy(Map.of("a1", p, "a2", p, "b1", q));
        MatrixMarkdown markdown = renderWith(matrix, false, true);

        String rowCriteria = markdown.item("Criteria", "Rows");
        assertTrue(rowCriteria.contains("Requirement"), rowCriteria);
        assertEquals(List.of(3, 2), numbersIn(rowCriteria));
        assertEquals(MatrixMarkdownRenderer.GROUPED_BY_PACKAGE, markdown.item("Criteria", "Grouped by package"));
        assertEquals(List.of(
            "| | C1 P | C2 Q |",
            "|---|---|---|",
            "| R1 P | | →dependency ×2 |",
            "| R2 Q | →usage | |"),
            markdown.section("Matrix"));

        // The dependency of A1 on A2, within package P, is left out
        assertEquals("3", markdown.item("Summary", "Relationships matching the criteria"));
        assertEquals("2", markdown.table("Rows").get("R1").get("Count"));
        assertEquals("Package", markdown.table("Rows").get("R1").get("Type"));
        assertEquals(List.of(
            "| Row | Column | Relationship | Detail | ID |",
            "|---|---|---|---|---|",
            "| R1 P | C2 Q | →dependency | A1 → B1 | d1 |",
            "| R1 P | C2 Q | →dependency | A2 → B1 | d2 |",
            "| R2 Q | C1 P | →usage | B1 → A1 | u1 |"),
            markdown.section("Relationships"));
    }

    @Test
    void groupedBy_ok_readsTheElementsOfARelationshipFromTheRowAsTheCellDoes() {
        MatrixElement a = new MatrixElement("a", "A", "P", "P.A", "Class", "");
        MatrixElement b = new MatrixElement("b", "B", "Q", "Q.B", "Class", "");
        MatrixElement p = new MatrixElement("p", "P", "", "P", "Package", "");
        MatrixElement q = new MatrixElement("q", "Q", "", "Q", "Package", "");

        // B depends on A, read from the row of A's package
        RelationshipMatrix matrix = RelationshipMatrix.of(List.of(a), List.of(b), List.of(directed("d", "b", "a", DEPENDENCY)), MatrixDirection.BOTH, MatrixShowElements.ALL)
            .groupedBy(Map.of("a", p, "b", q));
        MatrixMarkdown markdown = renderWith(matrix, false, true);

        assertEquals("| R1 P | C1 Q | ←dependency | A ← B | d |", markdown.section("Relationships").get(2));
        // One element in one package
        assertEquals(List.of(1, 1), numbersIn(markdown.item("Criteria", "Columns")));
    }

    @Test
    void groupedBy_ok_saysSoWhenShowElementsListsNoneOfThePackages() {
        MatrixElement a = new MatrixElement("a", "A", "P", "P.A", "Class", "");
        MatrixElement p = new MatrixElement("p", "P", "", "P", "Package", "");

        RelationshipMatrix matrix = RelationshipMatrix.of(List.of(a), List.of(a), List.of(), MatrixDirection.BOTH, MatrixShowElements.WITH_RELATIONSHIPS)
            .groupedBy(Map.of("a", p));
        MatrixMarkdown markdown = renderWith(matrix, false, true);

        assertEquals("None of the 1 row package is listed, because showElements is with_relationships.", markdown.line("Rows"));
    }

    @Test
    void render_ok_listsNoRelationshipWhenTheListedElementsHaveNone() {
        MatrixMarkdown markdown = new MatrixMarkdown(render(REQUIREMENTS, BLOCKS, SATISFY_DEPENDENCIES, MatrixDirection.COLUMN_TO_ROW, MatrixShowElements.ROWS_WITHOUT_RELATIONSHIPS, true));

        assertEquals("No relationship between the listed elements.", markdown.line("Relationships"));
    }

    @Test
    void render_ok_appendsNoRelationshipSectionUnlessAskedTo() {
        assertFalse(satisfyMatrix(MatrixShowElements.ALL).hasSection("Relationships"));
    }

    @Test
    void render_ok_saysSoWhenNoElementMatched() {
        MatrixMarkdown markdown = new MatrixMarkdown(render(List.of(), BLOCKS, List.of(), MatrixDirection.BOTH, MatrixShowElements.ALL, false));

        assertEquals("No element matched the row criteria.", markdown.line("Rows"));
        assertEquals("0 / 0", markdown.item("Summary", "Rows with relationships"));
        assertEquals("0 / 3 (0%)", markdown.item("Summary", "Columns with relationships"));
        assertEquals("none", markdown.item("Summary", "Rows without relationships"));
        assertEquals(MatrixMarkdownRenderer.NO_RELATIONSHIP_BETWEEN_LISTED, markdown.line("Matrix"));
        String rowCriteria = markdown.item("Criteria", "Rows");
        assertTrue(rowCriteria.contains("Requirement"), rowCriteria);
        assertTrue(rowCriteria.contains("\"Requirements\""), rowCriteria);
        assertEquals(List.of(0), numbersIn(rowCriteria));
    }

    @Test
    void render_ok_saysSoWhenShowElementsListsNoneOfTheElements() {
        MatrixMarkdown markdown = new MatrixMarkdown(render(REQUIREMENTS, BLOCKS, List.of(), MatrixDirection.BOTH, MatrixShowElements.WITH_RELATIONSHIPS, false));

        assertEquals("None of the 5 row elements is listed, because showElements is with_relationships.", markdown.line("Rows"));
        assertEquals("None of the 3 column elements is listed, because showElements is with_relationships.", markdown.line("Columns"));
    }

    @Test
    void render_ok_sortsByFullNameInNaturalOrderAndThenById() {
        List<MatrixElement> rows = List.of(
            requirement("x10", "REQ-10", "Requirements", ""),
            requirement("x2", "REQ-2", "Requirements", ""),
            requirement("y1", "REQ-1", "Requirements", ""),
            requirement("x1", "REQ-1", "Requirements", ""),
            requirement("z", "REQ-1", "Other", ""));

        MatrixMarkdown markdown = new MatrixMarkdown(render(rows, BLOCKS, List.of(), MatrixDirection.BOTH, MatrixShowElements.ALL, false));

        List<String> ids = markdown.table("Rows").values().stream().map(row -> row.get("ID")).toList();
        assertEquals(List.of("z", "x1", "y1", "x2", "x10"), ids);
    }

    @Test
    void render_ok_roundsThePercentagesHalfUp() {
        List<MatrixElement> rows = List.of(
            requirement("a", "A", "Requirements", ""),
            requirement("b", "B", "Requirements", ""),
            requirement("c", "C", "Requirements", ""));
        List<MatrixElement> columns = List.of(block("k", "K"));

        MatrixMarkdown markdown = new MatrixMarkdown(render(rows, columns, List.of(directed("s", "k", "a", SATISFY)), MatrixDirection.BOTH, MatrixShowElements.ALL, false));

        assertEquals("1 / 3 (33%)", markdown.item("Summary", "Rows with relationships"));
        assertEquals("1 / 1 (100%)", markdown.item("Summary", "Columns with relationships"));
    }

    @Test
    void of_ok_takesAnElementAndARelationshipGivenTwiceOnce() {
        MatrixElement a = block("a", "A");
        MatrixElement b = block("b", "B");
        MatrixRelationship dependency = directed("d", "a", "b", DEPENDENCY);

        RelationshipMatrix matrix = RelationshipMatrix.of(List.of(a, a), List.of(b, b), List.of(dependency, dependency), MatrixDirection.BOTH, MatrixShowElements.ALL);

        assertEquals(1, matrix.rows().size());
        assertEquals(1, matrix.columns().size());
        assertEquals(1, matrix.entries().size());
        assertEquals(1, matrix.relationshipCount());
    }

    @Test
    void of_ok_placesNoRelationshipWhoseEndIsNeitherARowNorAColumn() {
        RelationshipMatrix matrix = RelationshipMatrix.of(
            List.of(block("a", "A")),
            List.of(block("b", "B")),
            List.of(directed("d", "a", "elsewhere", DEPENDENCY)),
            MatrixDirection.BOTH,
            MatrixShowElements.ALL);

        assertEquals(0, matrix.relationshipCount());
        assertTrue(matrix.entries().isEmpty());
    }
}
