package com.astahpromcp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

// The rules that keep the Astah API behind the Astah API lock, checked against the compiled classes.
public class AstahApiBoundaryTest {

    // The names the rules look for, both as a class file spells a type and as a string constant naming a class would
    private static final List<String> ASTAH_API = List.of("com/change_vision/", "com.change_vision.");
    private static final List<String> TOOL_ASTAH = List.of("com/astahpromcp/tool/astah/", "com.astahpromcp.tool.astah.");

    // Rule A: outside tool.astah.**, only the view this plugin adds to Astah may reference the Astah API
    private static final String TOOL_ASTAH_PACKAGE = "tool/astah";
    private static final String EXTRA_TAB_VIEW = "ExtraTabView";

    // Rule B: the packages whose tools ToolCatalogAssembler registers without the Astah API lock, and tool.common, which they share; each with its subpackages
    private static final List<String> UNLOCKED_TOOL_PACKAGES = List.of(
            "tool/projectdiff",
            "tool/log",
            "tool/knowledge",
            "tool/visualization",
            "tool/config",
            "tool/info",
            "tool/common");

    // Rule C: the only classes outside tool.astah.** that may reference it, each with its nested and anonymous classes: the two classes of server that assemble the tools.
    // The rest of server serves HTTP on Jetty's threads, which do not hold the Astah API lock.
    private static final Set<String> TOOL_ASTAH_CLIENTS = Set.of(
            "server/McpServerApp",
            "server/ToolCatalogAssembler");

    private static List<CompiledClass> classes;

    @BeforeAll
    static void readClasses() throws IOException, URISyntaxException {
        classes = CompiledClass.readAll();
    }

    // Rule A
    @Test
    void astahApi_ok_isReferencedOnlyFromToolAstahAndExtraTabView() {
        assertNoneReference(ASTAH_API,
                c -> !c.isWithin(TOOL_ASTAH_PACKAGE) && !c.isTopLevelClass(EXTRA_TAB_VIEW),
                "Only tool.astah.** and ExtraTabView may reference the Astah API. The Astah API lock is put around the tools of tool.astah.** alone, so a call from anywhere else reaches the Astah API without it.");
    }

    // Rule B
    @Test
    void toolAstah_ok_isNotReferencedFromThePackagesOfUnlockedTools() {
        assertNoneReference(TOOL_ASTAH,
                c -> UNLOCKED_TOOL_PACKAGES.stream().anyMatch(c::isWithin),
                "The packages whose tools are registered without the Astah API lock, and tool.common, must not reference tool.astah.**: through it they would reach the Astah API without the lock.");
    }

    // Rule C
    @Test
    void toolAstah_ok_isReferencedFromOutsideOnlyByTheClassesThatAssembleTheTools() {
        assertNoneReference(TOOL_ASTAH,
                c -> !c.isWithin(TOOL_ASTAH_PACKAGE) && TOOL_ASTAH_CLIENTS.stream().noneMatch(c::isClass),
                "Outside tool.astah.**, only the classes that assemble the tools " + TOOL_ASTAH_CLIENTS + " may reference tool.astah.**. "
                        + "The rest of server serves HTTP on Jetty's threads, which do not hold the Astah API lock, so through tool.astah.** it would reach the Astah API without it. "
                        + "A package that needs the rules for calling the Astah API (the lock, the transaction of a run, what settles after a call) takes them from those classes as a function or through an interface of its own, such as McpToolScriptHost.");
    }

    // A package that a rule names but that holds no class would leave that rule checking nothing, for instance after a rename
    @Test
    void classes_ok_existInEveryPackageTheRulesName() {
        List<String> packages = new ArrayList<>();
        packages.add(TOOL_ASTAH_PACKAGE);
        packages.addAll(UNLOCKED_TOOL_PACKAGES);

        for (String packagePath : packages) {
            assertTrue(classes.stream().anyMatch(c -> c.isWithin(packagePath)),
                    "No class is compiled into com.astahpromcp." + packagePath.replace('/', '.'));
        }
        assertTrue(classes.stream().anyMatch(c -> c.isTopLevelClass(EXTRA_TAB_VIEW)), "ExtraTabView is not compiled");
    }

    // An allowance of Rule C that names no class, or a class that no longer references tool.astah.**, would only widen the rule
    @Test
    void toolAstahClients_ok_areCompiledAndStillReferenceToolAstah() {
        for (String client : TOOL_ASTAH_CLIENTS) {
            assertTrue(classes.stream().filter(c -> c.isClass(client)).anyMatch(c -> c.firstOf(TOOL_ASTAH) != null),
                    "TOOL_ASTAH_CLIENTS names " + client + ", but no class of it references tool.astah.**: remove it, or correct the name after a rename");
        }
    }

    // A search that finds nothing would let every rule pass however the code changed, so it is tried where the names are known to be
    @Test
    void firstOf_ok_findsTheNamesWhereTheyAreKnownToBe() {
        assertEquals("com/change_vision/", classAt("ExtraTabView.class").firstOf(ASTAH_API));
        assertEquals("com/astahpromcp/tool/astah/", classAt("server/ToolCatalogAssembler.class").firstOf(TOOL_ASTAH));
        assertNull(classAt("tool/ToolProvider.class").firstOf(ASTAH_API));
    }

    private static CompiledClass classAt(String path) {
        return classes.stream()
                .filter(c -> c.path().equals(path))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No class is compiled at com/astahpromcp/" + path));
    }

    // Fail with every class the rule covers that contains one of the given names
    private static void assertNoneReference(List<String> names, Predicate<CompiledClass> covered, String rule) {
        int checked = 0;
        List<String> violations = new ArrayList<>();
        for (CompiledClass c : classes) {
            if (!covered.test(c)) {
                continue;
            }
            checked++;
            String found = c.firstOf(names);
            if (found != null) {
                violations.add(c.className() + " references " + found);
            }
        }

        assertTrue(checked > 0, "The rule covers no class, so it checks nothing: " + rule);
        assertTrue(violations.isEmpty(), () -> rule + "\n" + String.join("\n", violations));
    }
}
