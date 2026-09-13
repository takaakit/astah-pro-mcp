package com.astahpromcp.tool.manifest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ToolManifestTest {

    private static final String SOURCE = "test";

    private static ToolManifest parse(String... lines) {
        return ToolManifest.parse(List.of(lines), SOURCE);
    }

    private static String row(String name, String direct, String programmatic) {
        return row(ToolManifest.Category.CLASS_DIAGRAM, name, direct, programmatic);
    }

    private static String row(ToolManifest.Category category, String name, String direct, String programmatic) {
        return category.label() + "\t" + name + "\t" + direct + "\t" + programmatic;
    }

    @Test
    void load_ok_readsTheShippedManifest() {
        ToolManifest manifest = ToolManifest.load();

        assertEquals(461, manifest.size(), "every tool definition the six factories produce needs a row");
        assertEquals(398, manifest.namesFor(ToolManifest.Profile.DIRECT).size());
        assertEquals(83, manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC).size());
        assertEquals(58, manifest.withheldNames().size());
    }

    @Test
    void load_ok_withheldRowsPublishNowhere() {
        ToolManifest manifest = ToolManifest.load();

        for (String name : manifest.withheldNames()) {
            ToolManifest.Entry entry = manifest.find(name);
            assertFalse(entry.direct() || entry.programmatic(), name);
        }
    }

    @Test
    void load_ok_givesEveryRowACategory() {
        ToolManifest manifest = ToolManifest.load();

        for (String name : manifest.allNames()) {
            assertNotNull(manifest.find(name).category(), name);
        }
    }

    @Test
    void parse_ok_readsCategoryAndFlagsAndIgnoresCommentsBlankLinesAndPadding() {
        ToolManifest manifest = parse(
                "# a comment",
                "",
                "  Class Diagram  \t   get_class_info    \t  x  \t  -  ",
                row("create_class", "x", "-"));

        assertEquals(2, manifest.size());
        assertEquals(new ToolManifest.Entry(ToolManifest.Category.CLASS_DIAGRAM,
                        "get_class_info", true, false, false),
                manifest.find("get_class_info"));
        assertNull(manifest.find("no_such_tool"));
    }

    @Test
    void parse_ok_readsEveryCategoryLabel() {
        for (ToolManifest.Category category : ToolManifest.Category.values()) {
            ToolManifest manifest = parse(row(category, "get_class_info", "x", "-"));

            assertEquals(category, manifest.find("get_class_info").category());
        }
    }

    @Test
    void parse_ng_rejectsAnUnknownCategory() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse("Object Diagram\tget_class_info\tx\t-"));
        assertTrue(e.getMessage().contains("category"), e.getMessage());
    }


    @Test
    void parse_ok_treatsRowsAfterTheDirectiveAsWithheld() {
        ToolManifest manifest = parse(
                row("get_class_info", "x", "-"),
                "#@withheld",
                "# ER: too many editing tools",
                row("create_er_attr", "-", "-"));

        assertEquals(java.util.Set.of("create_er_attr"), manifest.withheldNames());
        assertFalse(manifest.find("get_class_info").withheld());
    }

    @Test
    void parse_ng_rejectsAWrongColumnCount() {
        // A five-column row
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse("Class Diagram\tget_class_info\tx\tx\t-"));
        assertTrue(e.getMessage().contains("columns"), e.getMessage());
    }

    @Test
    void parse_ng_rejectsAFlagThatIsNeitherXNorDash() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "yes", "-")));
        assertTrue(e.getMessage().contains("column 'direct'"), e.getMessage());
    }

    @Test
    void parse_ng_rejectsAnEmptyName() {
        assertThrows(IllegalStateException.class, () -> parse(row("", "x", "-")));
    }

    @Test
    void parse_ng_rejectsADuplicateName() {
        Exception e = assertThrows(IllegalStateException.class, () -> parse(
                row("get_class_info", "x", "-"),
                row("get_class_info", "x", "x")));
        assertTrue(e.getMessage().contains("more than once"), e.getMessage());
    }

    @Test
    void parse_ng_rejectsAnAllDashRowInTheBodySection() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse(row("create_er_attr", "-", "-")));
        assertTrue(e.getMessage().contains("create_er_attr"), e.getMessage());
    }

    @Test
    void parse_ng_rejectsAPublishingRowInTheWithheldSection() {
        Exception e = assertThrows(IllegalStateException.class, () -> parse(
                "#@withheld",
                row("create_er_attr", "x", "-")));
        assertTrue(e.getMessage().contains("create_er_attr"), e.getMessage());
    }
}
