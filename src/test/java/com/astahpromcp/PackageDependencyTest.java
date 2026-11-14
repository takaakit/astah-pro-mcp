package com.astahpromcp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

// The rules that keep the packages in layers, checked against the compiled classes.
public class PackageDependencyTest {

    // Rule E: the base of the Astah tools, without its subpackages: the rules for calling the Astah API, and what every Astah tool is built on
    private static final String BASE = "tool/astah/pro";

    // Rule E: what the base and the DTO layer may depend on besides themselves, without subpackages: the base every tool shares, and the configuration
    private static final Set<String> BELOW_ASTAH_BASE = Set.of("tool", "config");

    // Rule E: the packages of the DTO layer, besides those whose name ends in one of DTO_PACKAGE_NAMES: the value types and the helpers that the DTOs use.
    // A new package of that kind is added here; until it is, a DTO that uses it fails Rule E.
    private static final Set<String> VALUE_PACKAGES = Set.of(
            "tool/astah/pro/common",
            "tool/astah/pro/model/layout",
            "tool/knowledge/document");

    private static final Set<String> DTO_PACKAGE_NAMES = Set.of("inputdto", "outputdto", "assembler");

    // Rules F to H: the layers, from the bottom
    private static final List<String> LAYERS = List.of("the common base", "the tool framework", "the tool modules", "the assembly and the view", "the entry points");

    private static final int TOOL_MODULE_LAYER = 2;

    // Rules F to H: every module with its layer. Each package of the plugin belongs to exactly one module.
    private static final List<Module> MODULES = List.of(
            new Module("config", 0, true),
            new Module("info", 0, true),
            new Module("tls", 0, true),
            new Module("tool/common", 0, true),
            // Its subpackages are modules of their own
            new Module("tool", 1, false),
            new Module("tool/manifest", 1, true),
            new Module("tool/astah", TOOL_MODULE_LAYER, true),
            new Module("tool/mcptoolscript", TOOL_MODULE_LAYER, true),
            new Module("tool/projectdiff", TOOL_MODULE_LAYER, true),
            new Module("tool/knowledge", TOOL_MODULE_LAYER, true),
            new Module("tool/visualization", TOOL_MODULE_LAYER, true),
            new Module("tool/log", TOOL_MODULE_LAYER, true),
            new Module("tool/config", TOOL_MODULE_LAYER, true),
            new Module("tool/info", TOOL_MODULE_LAYER, true),
            new Module("server", 3, true),
            new Module("ui", 3, true),
            // com.astahpromcp itself, which holds the entry points
            new Module("", 4, false));

    // Rule G: the only dependencies allowed between the tool modules, from a module to the modules it may use
    private static final Map<String, Set<String>> ALLOWED_TOOL_MODULE_DEPENDENCIES = Map.of(
            "tool/astah", Set.of("tool/mcptoolscript", "tool/projectdiff", "tool/knowledge", "tool/visualization"));

    // Rule H: the modules that only the entry points may reference, and the entry points
    private static final Set<String> ENTRY_ONLY_MODULES = Set.of("server", "ui");
    private static final List<String> ENTRY_POINTS = List.of("Activator", "ExtraTabView");

    // How many of the classes behind one dependency a failure names
    private static final int EXAMPLES = 3;

    private static List<CompiledClass> classes;

    // A module: its package, and its subpackages too when withSubpackages
    private record Module(String path, int layer, boolean withSubpackages) {

        boolean covers(String packagePath) {
            return packagePath.equals(path) || (withSubpackages && packagePath.startsWith(path + "/"));
        }

        String displayName() {
            return path.isEmpty() ? "com.astahpromcp" : path.replace('/', '.');
        }
    }

    @BeforeAll
    static void readClasses() throws IOException, URISyntaxException {
        classes = CompiledClass.readAll();
    }

    // Rule D
    @Test
    void packages_ok_dependOnOneAnotherWithoutACycle() {
        Map<String, Map<String, Set<String>>> graph = packageGraph();
        List<List<String>> cycles = cyclesOf(graph);

        assertTrue(cycles.isEmpty(), () -> "No package may depend on itself through other packages: the packages of a cycle can be neither understood nor changed one at a time, "
                + "and the layers cannot be checked. Move the type that closes the cycle to the package it belongs with.\n" + describe(cycles, graph));
    }

    // Rule E
    @Test
    void base_ok_dependsOnlyOnWhatLiesBelowIt() {
        assertDependsOnlyOn(
                c -> c.packagePath().equals(BASE),
                p -> BELOW_ASTAH_BASE.contains(p),
                "The base of the Astah tools (tool.astah.pro, without its subpackages) may depend only on " + BELOW_ASTAH_BASE
                        + ". Every Astah tool depends on it, so it must know no tool, no DTO and no other function: what assembles the tools goes in tool.astah.pro.assembly, "
                        + "and what implements an interface of another function goes in a subpackage of tool.astah.pro.");
    }

    // Rule E
    @Test
    void dtoLayer_ok_dependsOnlyOnItselfTheBaseAndWhatLiesBelowIt() {
        assertDependsOnlyOn(
                c -> isDtoLayer(c.packagePath()),
                p -> isDtoLayer(p) || p.equals(BASE) || BELOW_ASTAH_BASE.contains(p),
                "The DTO layer (the packages whose name ends in one of " + DTO_PACKAGE_NAMES + ", and " + VALUE_PACKAGES
                        + ") may depend only on itself, on the base of the Astah tools and on " + BELOW_ASTAH_BASE
                        + ". A type that a DTO uses belongs in the DTO layer, not in a package of tools.");
    }

    // Rule F
    @Test
    void modules_ok_dependOnlyOnTheirOwnLayerAndTheLayersBelow() {
        assertDependsOnlyOn(
                c -> moduleOf(c.packagePath()) != null,
                (c, p) -> moduleOf(p) != null && moduleOf(p).layer() <= moduleOf(c.packagePath()).layer(),
                "A module may depend only on the modules of its own layer and of the layers below it. The layers, from the bottom: " + describeLayers()
                        + ". A dependency on a higher layer reverses the direction the Logical Architecture diagram shows: move the type it needs down, "
                        + "or have the higher layer hand it over through an interface of the lower one, as McpToolScriptHost and ProjectDiffHost do.");
    }

    // Rule G
    @Test
    void toolModules_ok_dependOnOneAnotherOnlyAsAllowed() {
        assertDependsOnlyOn(
                c -> isToolModule(moduleOf(c.packagePath())),
                (c, p) -> {
                    Module from = moduleOf(c.packagePath());
                    Module to = moduleOf(p);
                    // A package of no module is reported by Rule F
                    return !isToolModule(to) || to.equals(from)
                            || ALLOWED_TOOL_MODULE_DEPENDENCIES.getOrDefault(from.path(), Set.of()).contains(to.path());
                },
                "Among the tool modules, only these dependencies are allowed, as the Logical Architecture diagram shows: " + ALLOWED_TOOL_MODULE_DEPENDENCIES
                        + ". The other tool modules know nothing of one another, so that each can be changed alone. One that needs something of another takes it "
                        + "through an interface of its own that tool.astah implements, as McpToolScriptHost and ProjectDiffHost do. "
                        + "To change this on purpose, change ALLOWED_TOOL_MODULE_DEPENDENCIES and the diagram together.");
    }

    // Rule H
    @Test
    void serverAndUi_ok_areReferencedOnlyByTheEntryPoints() {
        assertDependsOnlyOn(
                c -> ENTRY_POINTS.stream().noneMatch(c::isTopLevelClass),
                (c, p) -> {
                    Module to = moduleOf(p);
                    return to == null || !ENTRY_ONLY_MODULES.contains(to.path()) || to.equals(moduleOf(c.packagePath()));
                },
                "Only the entry points " + ENTRY_POINTS + " may reference " + ENTRY_ONLY_MODULES
                        + ", which assemble and show the MCP server, and these two may not reference each other. "
                        + "Whatever else referenced them would depend on the whole plugin.");
    }

    // A package of no module would escape Rules F to H, and one of two modules would be judged twice
    @Test
    void modules_ok_coverEveryPackageExactlyOnce() {
        Set<String> wrong = new TreeSet<>();
        for (CompiledClass c : classes) {
            long covering = MODULES.stream().filter(m -> m.covers(c.packagePath())).count();
            if (covering != 1) {
                wrong.add("com.astahpromcp." + c.packagePath().replace('/', '.') + " is in " + covering + " modules");
            }
        }

        assertTrue(wrong.isEmpty(), () -> "Every package must belong to exactly one module of MODULES. Decide the layer of a new module and add it there: " + wrong);
    }

    // The search for modules is tried on packages whose module is known
    @Test
    void moduleOf_ok_findsTheModuleOfEachPackage() {
        assertEquals("tool", moduleOf("tool").path());
        assertEquals("tool/common", moduleOf("tool/common/inputdto").path());
        assertEquals("tool/astah", moduleOf("tool/astah/pro/model/outputdto").path());
        assertEquals("", moduleOf("").path());
        assertNull(moduleOf("tool/astahx"), "A module must cover its subpackages only, not every package whose name starts with its own");
    }

    // A package that a rule names but that holds no class would leave that rule checking nothing, for instance after a rename
    @Test
    void classes_ok_existInEveryPackageTheRulesName() {
        Set<String> packages = new TreeSet<>(BELOW_ASTAH_BASE);
        packages.add(BASE);
        packages.addAll(VALUE_PACKAGES);

        for (String packagePath : packages) {
            assertTrue(classes.stream().anyMatch(c -> c.packagePath().equals(packagePath)),
                    "No class is compiled into com.astahpromcp." + packagePath.replace('/', '.'));
        }
        assertTrue(classes.stream().anyMatch(c -> isDtoLayer(c.packagePath()) && !VALUE_PACKAGES.contains(c.packagePath())), "No DTO package is compiled");

        for (Module module : MODULES) {
            assertTrue(classes.stream().anyMatch(c -> module.covers(c.packagePath())), "No class is compiled into the module " + module.displayName());
        }
        ALLOWED_TOOL_MODULE_DEPENDENCIES.forEach((from, to) -> {
            assertTrue(isToolModule(moduleNamed(from)), "ALLOWED_TOOL_MODULE_DEPENDENCIES names " + from + ", which is not a tool module");
            to.forEach(path -> assertTrue(isToolModule(moduleNamed(path)), "ALLOWED_TOOL_MODULE_DEPENDENCIES names " + path + ", which is not a tool module"));
        });
        for (String path : ENTRY_ONLY_MODULES) {
            assertNotNull(moduleNamed(path), "ENTRY_ONLY_MODULES names " + path + ", which is not a module");
        }
        for (String entryPoint : ENTRY_POINTS) {
            assertTrue(classes.stream().anyMatch(c -> c.isTopLevelClass(entryPoint)), entryPoint + " is not compiled");
        }
    }

    // A search that finds nothing would let every rule pass however the code changed, so it is tried where the dependencies are known to be
    @Test
    void referencedPackages_ok_findsThePackagesWhereTheyAreKnownToBe() {
        assertTrue(classAt("server/ToolCatalogAssembler.class").referencedPackages().containsAll(Set.of("tool/astah/pro/assembly", "tool/knowledge", "tool/manifest")));
        assertFalse(classAt("tool/ToolProvider.class").referencedPackages().contains(BASE));
    }

    // The search for cycles is tried on a graph whose cycles are known
    @Test
    void cyclesOf_ok_findsEveryCycleAndOnlyThem() {
        Map<String, Map<String, Set<String>>> graph = new TreeMap<>();
        graph.put("a", Map.of("b", Set.of()));
        graph.put("b", Map.of("c", Set.of(), "a", Set.of()));
        graph.put("c", Map.of());
        graph.put("d", Map.of("a", Set.of(), "e", Set.of()));
        graph.put("e", Map.of("d", Set.of()));

        assertEquals(List.of(List.of("a", "b"), List.of("d", "e")), cyclesOf(graph));
    }

    private static CompiledClass classAt(String path) {
        return classes.stream()
                .filter(c -> c.path().equals(path))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No class is compiled at com/astahpromcp/" + path));
    }

    private static boolean isDtoLayer(String packagePath) {
        String name = packagePath.substring(packagePath.lastIndexOf('/') + 1);
        return DTO_PACKAGE_NAMES.contains(name) || VALUE_PACKAGES.contains(packagePath);
    }

    // The module that covers the package, or null when no module or more than one does
    private static Module moduleOf(String packagePath) {
        List<Module> covering = MODULES.stream().filter(m -> m.covers(packagePath)).toList();
        return covering.size() == 1 ? covering.get(0) : null;
    }

    private static Module moduleNamed(String path) {
        return MODULES.stream().filter(m -> m.path().equals(path)).findFirst().orElse(null);
    }

    private static boolean isToolModule(Module module) {
        return module != null && module.layer() == TOOL_MODULE_LAYER;
    }

    // Each layer with its modules, from the bottom
    private static String describeLayers() {
        return IntStream.range(0, LAYERS.size())
                .mapToObj(layer -> "L" + layer + " " + LAYERS.get(layer) + " " + MODULES.stream()
                        .filter(m -> m.layer() == layer)
                        .map(Module::displayName)
                        .toList())
                .collect(Collectors.joining(", "));
    }

    // Each package, the packages it depends on, and the classes behind each of those dependencies
    private static Map<String, Map<String, Set<String>>> packageGraph() {
        Map<String, Map<String, Set<String>>> graph = new TreeMap<>();
        for (CompiledClass c : classes) {
            Map<String, Set<String>> dependencies = graph.computeIfAbsent(c.packagePath(), p -> new TreeMap<>());
            for (String packagePath : c.referencedPackages()) {
                dependencies.computeIfAbsent(packagePath, p -> new TreeSet<>()).add(c.className());
                graph.computeIfAbsent(packagePath, p -> new TreeMap<>());
            }
        }
        return graph;
    }

    // The packages that depend on themselves through others, as the strongly connected components with more than one package, each sorted (Tarjan's algorithm)
    private static List<List<String>> cyclesOf(Map<String, Map<String, Set<String>>> graph) {
        Map<String, Integer> index = new HashMap<>();
        Map<String, Integer> lowLink = new HashMap<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> onStack = new TreeSet<>();
        List<List<String>> cycles = new ArrayList<>();

        for (String start : graph.keySet()) {
            if (!index.containsKey(start)) {
                connect(start, graph, index, lowLink, stack, onStack, cycles);
            }
        }
        cycles.sort((a, b) -> a.get(0).compareTo(b.get(0)));
        return cycles;
    }

    private static void connect(String packagePath, Map<String, Map<String, Set<String>>> graph, Map<String, Integer> index, Map<String, Integer> lowLink,
                                Deque<String> stack, Set<String> onStack, List<List<String>> cycles) {
        index.put(packagePath, index.size());
        lowLink.put(packagePath, index.get(packagePath));
        stack.push(packagePath);
        onStack.add(packagePath);

        for (String next : graph.getOrDefault(packagePath, Map.of()).keySet()) {
            if (!index.containsKey(next)) {
                connect(next, graph, index, lowLink, stack, onStack, cycles);
                lowLink.put(packagePath, Math.min(lowLink.get(packagePath), lowLink.get(next)));
            } else if (onStack.contains(next)) {
                lowLink.put(packagePath, Math.min(lowLink.get(packagePath), index.get(next)));
            }
        }

        if (lowLink.get(packagePath).equals(index.get(packagePath))) {
            List<String> component = new ArrayList<>();
            String member;
            do {
                member = stack.pop();
                onStack.remove(member);
                component.add(member);
            } while (!member.equals(packagePath));

            if (component.size() > 1) {
                component.sort(null);
                cycles.add(List.copyOf(component));
            }
        }
    }

    // Each cycle with the dependencies inside it and some of the classes behind each
    private static String describe(List<List<String>> cycles, Map<String, Map<String, Set<String>>> graph) {
        StringBuilder text = new StringBuilder();
        for (List<String> cycle : cycles) {
            text.append("Cycle: ").append(String.join(", ", cycle)).append('\n');
            for (String from : cycle) {
                for (Map.Entry<String, Set<String>> dependency : graph.get(from).entrySet()) {
                    if (cycle.contains(dependency.getKey())) {
                        text.append("  ").append(from).append(" -> ").append(dependency.getKey())
                                .append(" by ").append(dependency.getValue().stream().limit(EXAMPLES).toList()).append('\n');
                    }
                }
            }
        }
        return text.toString();
    }

    // Fail with every class the rule covers that depends on a package the rule does not allow
    private static void assertDependsOnlyOn(Predicate<CompiledClass> covered, Predicate<String> allowed, String rule) {
        assertDependsOnlyOn(covered, (c, packagePath) -> allowed.test(packagePath), rule);
    }

    // As above, for a rule whose allowed packages depend on the class that depends on them
    private static void assertDependsOnlyOn(Predicate<CompiledClass> covered, BiPredicate<CompiledClass, String> allowed, String rule) {
        int checked = 0;
        List<String> violations = new ArrayList<>();
        for (CompiledClass c : classes) {
            if (!covered.test(c)) {
                continue;
            }
            checked++;
            for (String packagePath : c.referencedPackages()) {
                if (!allowed.test(c, packagePath)) {
                    violations.add(c.className() + " depends on " + (packagePath.isEmpty() ? "com.astahpromcp" : "com.astahpromcp." + packagePath.replace('/', '.')));
                }
            }
        }

        assertTrue(checked > 0, "The rule covers no class, so it checks nothing: " + rule);
        assertTrue(violations.isEmpty(), () -> rule + "\n" + String.join("\n", violations));
    }
}
