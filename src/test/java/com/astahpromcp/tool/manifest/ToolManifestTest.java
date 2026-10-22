package com.astahpromcp.tool.manifest;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static String row(String name, String direct, String programmatic, String reason) {
        return row(name, direct, programmatic) + "\t" + reason;
    }

    @Test
    void load_ok_readsTheShippedManifest() {
        ToolManifest manifest = ToolManifest.load();

        assertEquals(454, manifest.size(), "every tool definition the seven factories produce needs a row");
        assertEquals(391, manifest.namesFor(ToolManifest.Profile.DIRECT).size());
        assertEquals(391, manifest.namesReachedOn(ToolManifest.Profile.DIRECT).size());
        assertEquals(84, manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC).size());
        assertEquals(312, manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC, ToolManifest.Reach.MCP_TOOL_SCRIPT).size());
        // 396 = 84 published directly + 312 called from an mcp tool script
        assertEquals(396, manifest.namesReachedOn(ToolManifest.Profile.PROGRAMMATIC).size());
    }

    @Test
    void load_ok_givesEveryRowACategory() {
        ToolManifest manifest = ToolManifest.load();

        for (String name : manifest.allNames()) {
            assertNotNull(manifest.find(name).category(), name);
        }
    }

    @Test
    void load_ok_givesARowAReasonExactlyWhenTheProgrammaticProfilePublishesItDirectly() {
        ToolManifest manifest = ToolManifest.load();

        for (String name : manifest.allNames()) {
            ToolManifest.Entry entry = manifest.find(name);
            assertEquals(entry.publishesTo(ToolManifest.Profile.PROGRAMMATIC), entry.reason() != null, name);
        }
        // 84 = rows with programmatic = x
        assertEquals(84, manifest.notMcpToolScriptCallableReasons().size());
    }

    @Test
    void parse_ok_readsCategoryAndValuesAndIgnoresCommentsBlankLinesAndPadding() {
        ToolManifest manifest = parse(
                "# a comment",
                "",
                "  Class Diagram  \t   get_class_info    \t  x  \t  s  ",
                row("create_class", "x", "s"));

        assertEquals(2, manifest.size());
        assertEquals(new ToolManifest.Entry(ToolManifest.Category.CLASS_DIAGRAM,
                        "get_class_info", ToolManifest.Reach.TOOL_LIST, ToolManifest.Reach.MCP_TOOL_SCRIPT, null),
                manifest.find("get_class_info"));
        assertNull(manifest.find("no_such_tool"));
    }

    @Test
    void parse_ok_readsTheReasonOfARowTheProgrammaticProfilePublishesDirectly() {
        ToolManifest manifest = parse(
                "  Class Diagram  \t   open_dgm    \t  x  \t  x  \t  view  ",
                row("get_class_info", "x", "s"));

        assertEquals(new ToolManifest.Entry(ToolManifest.Category.CLASS_DIAGRAM,
                        "open_dgm", ToolManifest.Reach.TOOL_LIST, ToolManifest.Reach.TOOL_LIST, NotMcpToolScriptCallableReason.DRIVES_THE_VIEW),
                manifest.find("open_dgm"));
        assertEquals(Map.of("open_dgm", NotMcpToolScriptCallableReason.DRIVES_THE_VIEW),
                manifest.notMcpToolScriptCallableReasons());
    }

    @Test
    void parse_ok_readsEveryValueEachColumnTakes() {
        ToolManifest manifest = parse(
                row("get_class_info", "x", "s"),
                row("open_dgm", "-", "x", "view"),
                row("create_er_attr", "-", "-"));

        ToolManifest.Entry directAndFromAScript = manifest.find("get_class_info");
        assertEquals(ToolManifest.Reach.TOOL_LIST, directAndFromAScript.reachOn(ToolManifest.Profile.DIRECT));
        assertEquals(ToolManifest.Reach.MCP_TOOL_SCRIPT, directAndFromAScript.reachOn(ToolManifest.Profile.PROGRAMMATIC));

        ToolManifest.Entry onTheProgrammaticProfileOnly = manifest.find("open_dgm");
        assertEquals(ToolManifest.Reach.NONE, onTheProgrammaticProfileOnly.reachOn(ToolManifest.Profile.DIRECT));
        assertEquals(ToolManifest.Reach.TOOL_LIST, onTheProgrammaticProfileOnly.reachOn(ToolManifest.Profile.PROGRAMMATIC));

        ToolManifest.Entry reachedByNoProfile = manifest.find("create_er_attr");
        assertEquals(ToolManifest.Reach.NONE, reachedByNoProfile.reachOn(ToolManifest.Profile.DIRECT));
        assertEquals(ToolManifest.Reach.NONE, reachedByNoProfile.reachOn(ToolManifest.Profile.PROGRAMMATIC));
    }

    @Test
    void parse_ng_rejectsAValueItsColumnDoesNotTake() {
        Exception sInDirect = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "s", "s")));
        assertTrue(sInDirect.getMessage().contains("column 'direct' must be one of [x, -] but was 's'"), sInDirect.getMessage());

        Exception unknownInDirect = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "yes", "s")));
        assertTrue(unknownInDirect.getMessage().contains("column 'direct' must be one of [x, -] but was 'yes'"), unknownInDirect.getMessage());

        Exception unknownInProgrammatic = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "x", "y")));
        assertTrue(unknownInProgrammatic.getMessage().contains("column 'programmatic' must be one of [x, s, -] but was 'y'"), unknownInProgrammatic.getMessage());
    }

    @Test
    void parse_ng_rejectsARowTheDirectProfilePublishesButTheProgrammaticProfileDoesNotReach() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "x", "-")));
        assertTrue(e.getMessage().contains("get_class_info"), e.getMessage());
        assertTrue(e.getMessage().contains("column 'programmatic'"), e.getMessage());

        // The same row with a reason is told the same, because what it needs is a programmatic column that reaches it, whichever way
        Exception withAReason = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "x", "-", "direct")));
        assertTrue(withAReason.getMessage().contains("column 'programmatic'"), withAReason.getMessage());
    }

    @Test
    void parse_ok_acceptsARowOnlyAnMcpToolScriptReaches() {
        ToolManifest manifest = parse(row("get_class_info", "-", "s"));

        assertEquals(new ToolManifest.Entry(ToolManifest.Category.CLASS_DIAGRAM,
                        "get_class_info", ToolManifest.Reach.NONE, ToolManifest.Reach.MCP_TOOL_SCRIPT, null),
                manifest.find("get_class_info"));
        assertEquals(Set.of(), manifest.namesFor(ToolManifest.Profile.DIRECT));
        assertEquals(Set.of(), manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC));
        assertEquals(Set.of("get_class_info"), manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC, ToolManifest.Reach.MCP_TOOL_SCRIPT));
    }

    @Test
    void namesReachedOn_ok_holdsTheRowsWhoseColumnIsXOrSInFileOrder() {
        ToolManifest manifest = parse(
                row("open_dgm", "x", "s"),
                row("get_class_info", "-", "x", "direct"),
                row("create_class", "x", "s"),
                row("save_proj", "x", "x", "project_file"),
                row("delete_class", "-", "s"),
                row("create_er_attr", "-", "-"));

        assertEquals(List.of("open_dgm", "get_class_info", "create_class", "save_proj", "delete_class"),
                List.copyOf(manifest.namesReachedOn(ToolManifest.Profile.PROGRAMMATIC)));
        assertEquals(List.of("open_dgm", "create_class", "delete_class"),
                List.copyOf(manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC, ToolManifest.Reach.MCP_TOOL_SCRIPT)));
        assertEquals(List.of("get_class_info", "save_proj"),
                List.copyOf(manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)));

        assertEquals(List.of("open_dgm", "create_class", "save_proj"),
                List.copyOf(manifest.namesReachedOn(ToolManifest.Profile.DIRECT)));
        assertEquals(List.of("open_dgm", "create_class", "save_proj"),
                List.copyOf(manifest.namesFor(ToolManifest.Profile.DIRECT)));
    }

    @Test
    void parse_ok_readsEveryReasonCode() {
        for (NotMcpToolScriptCallableReason reason : NotMcpToolScriptCallableReason.values()) {
            ToolManifest manifest = parse(row("get_class_info", "x", "x", reason.code()));

            assertEquals(reason, manifest.find("get_class_info").reason());
        }
    }

    @Test
    void parse_ok_treatsAnEmptyReasonColumnAsNoReason() {
        ToolManifest manifest = parse(row("get_class_info", "x", "s", "  "));

        assertNull(manifest.find("get_class_info").reason());
    }

    @Test
    void parse_ng_rejectsAnUnknownReason() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse(row("open_dgm", "x", "x", "drives_view")));
        assertTrue(e.getMessage().contains("column 'reason'"), e.getMessage());
    }

    @Test
    void parse_ng_rejectsARowTheProgrammaticProfilePublishesDirectlyWithoutAReason() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse(row("open_dgm", "x", "x")));
        assertTrue(e.getMessage().contains("open_dgm"), e.getMessage());
        assertTrue(e.getMessage().contains("column 'reason'"), e.getMessage());

        // Published on the programmatic profile only, as the tools that run mcp tool scripts are, it still needs one
        Exception onTheProgrammaticProfileOnly = assertThrows(IllegalStateException.class,
                () -> parse(row("run_mcp_tool_script", "-", "x")));
        assertTrue(onTheProgrammaticProfileOnly.getMessage().contains("run_mcp_tool_script"), onTheProgrammaticProfileOnly.getMessage());
        assertTrue(onTheProgrammaticProfileOnly.getMessage().contains("column 'reason'"), onTheProgrammaticProfileOnly.getMessage());
    }

    @Test
    void parse_ng_rejectsAReasonOnARowTheProgrammaticProfileDoesNotPublishDirectly() {
        Exception onTheDirectProfileAndFromAScript = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "x", "s", "direct")));
        assertTrue(onTheDirectProfileAndFromAScript.getMessage().contains("get_class_info"), onTheDirectProfileAndFromAScript.getMessage());
        assertTrue(onTheDirectProfileAndFromAScript.getMessage().contains("column 'reason'"), onTheDirectProfileAndFromAScript.getMessage());

        Exception fromAScriptOnly = assertThrows(IllegalStateException.class,
                () -> parse(row("get_class_info", "-", "s", "direct")));
        assertTrue(fromAScriptOnly.getMessage().contains("get_class_info"), fromAScriptOnly.getMessage());
        assertTrue(fromAScriptOnly.getMessage().contains("column 'reason'"), fromAScriptOnly.getMessage());

        Exception reachedByNoProfile = assertThrows(IllegalStateException.class,
                () -> parse(row("create_er_attr", "-", "-", "direct")));
        assertTrue(reachedByNoProfile.getMessage().contains("create_er_attr"), reachedByNoProfile.getMessage());
    }

    @Test
    void parse_ok_readsEveryCategoryLabel() {
        for (ToolManifest.Category category : ToolManifest.Category.values()) {
            ToolManifest manifest = parse(row(category, "get_class_info", "x", "s"));

            assertEquals(category, manifest.find("get_class_info").category());
        }
    }

    @Test
    void parse_ng_rejectsAnUnknownCategory() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> parse("Object Diagram\tget_class_info\tx\ts"));
        assertTrue(e.getMessage().contains("category"), e.getMessage());
    }


    @Test
    void parse_ng_rejectsAWrongColumnCount() {
        // A three-column row
        Exception tooFew = assertThrows(IllegalStateException.class,
                () -> parse("Class Diagram\tget_class_info\tx"));
        assertTrue(tooFew.getMessage().contains("columns"), tooFew.getMessage());

        // A six-column row
        Exception tooMany = assertThrows(IllegalStateException.class,
                () -> parse("Class Diagram\tget_class_info\tx\tx\tdirect\t-"));
        assertTrue(tooMany.getMessage().contains("columns"), tooMany.getMessage());
    }

    @Test
    void parse_ng_rejectsAnEmptyName() {
        assertThrows(IllegalStateException.class, () -> parse(row("", "x", "s")));
    }

    @Test
    void parse_ng_rejectsADuplicateName() {
        Exception e = assertThrows(IllegalStateException.class, () -> parse(
                row("get_class_info", "x", "s"),
                row("get_class_info", "x", "x", "direct")));
        assertTrue(e.getMessage().contains("more than once"), e.getMessage());
    }
}
