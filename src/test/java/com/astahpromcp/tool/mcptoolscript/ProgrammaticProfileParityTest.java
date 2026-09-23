package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.AstahProToolFactory;
import com.astahpromcp.tool.astah.pro.ExclusiveToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

// What the manifest cannot say, and the startup checks therefore cannot check.
public class ProgrammaticProfileParityTest {

    @TempDir
    Path workspaceDir;

    private ToolCatalog catalog(DiagramThumbnails thumbnails) {
        return ToolCatalog.build(thumbnails, workspaceDir.resolve("images"), workspaceDir);
    }

    private static ToolManifest manifest() {
        return ToolManifest.load();
    }

    // The programmatic profile as McpServerApp assembles it: its own two tools, plus what the programmatic column selects
    private List<ToolProvider> programmaticProfileProviders(ToolCatalog catalog, ToolManifest manifest) {
        List<ToolProvider> providers = new ArrayList<>(new McpToolScriptProviderFactory().createToolProviders(catalog, manifest));
        providers.addAll(catalog.select(manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)).withAstahLock());
        return providers;
    }

    private static Set<String> toolNamesOf(List<ToolProvider> providers) {
        Set<String> names = new TreeSet<>();
        for (ToolProvider provider : providers) {
            for (ToolDefinition definition : provider.createToolDefinitions()) {
                names.add(definition.toolSchema().name());
            }
        }
        return names;
    }

    // The programmatic profile as McpToolScriptProviderFactory builds it: the direct profile can be reached, the programmatic column cannot
    private static AstahToolRegistry registryOf(ToolCatalog catalog, ToolManifest manifest) {
        return AstahToolRegistry.from(
                catalog,
                manifest.namesFor(ToolManifest.Profile.DIRECT),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC));
    }

    @Test
    void mcpToolScriptCallableNames_ok_isTheRegistryWithoutTheToolsDerivedAsUncallableOrPublishedDirectly() {
        ToolCatalog catalog = catalog(DiagramThumbnails.OMIT);
        ToolManifest manifest = manifest();
        AstahToolRegistry registry = registryOf(catalog, manifest);

        Set<String> withheld = new TreeSet<>(registry.names());
        Set<String> reasons = new TreeSet<>(catalog.notMcpToolScriptCallableNames());
        reasons.addAll(manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC));
        withheld.retainAll(reasons);

        assertEquals(registry.size() - withheld.size(), registry.mcpToolScriptCallableNames().size());
        assertFalse(withheld.isEmpty(), "the comparison is only meaningful if some tools are actually withheld");
    }

    // The half of the rule the manifest cannot state and the startup checks therefore cannot check.
    // Publishing a tool directly and leaving it callable from a script would give the agent two ways to do one thing
    // with nothing to choose between them, so the two sets have to be disjoint as well as cover the direct profile.
    @Test
    void programmaticProfile_ok_reachesNoToolBothDirectlyAndFromAScript() {
        ToolCatalog catalog = catalog(DiagramThumbnails.OMIT);
        ToolManifest manifest = manifest();
        AstahToolRegistry registry = registryOf(catalog, manifest);

        Set<String> both = new TreeSet<>(toolNamesOf(programmaticProfileProviders(catalog, manifest)));
        both.retainAll(registry.mcpToolScriptCallableNames());

        assertTrue(both.isEmpty(), "reachable both directly and from a script: " + both);
    }

    // The reason for the test above, stated as the thing that would break it: every tool the programmatic column names
    // and the catalog holds is withheld from a script, whatever the catalog itself thinks of its shape.
    @Test
    void mcpToolScriptCallableNames_ok_holdsNoToolTheProgrammaticColumnPublishes() {
        ToolCatalog catalog = catalog(DiagramThumbnails.OMIT);
        ToolManifest manifest = manifest();
        AstahToolRegistry registry = registryOf(catalog, manifest);

        for (String name : manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)) {
            if (!catalog.contains(name)) {
                continue;
            }
            assertFalse(registry.mcpToolScriptCallableNames().contains(name), name);
            assertNotNull(registry.notMcpToolScriptCallableReason(name), name);
        }
    }

    // The other half of the rule the test above states: the two sets cover the direct profile between them, so
    // nothing the direct profile publishes becomes unreachable on the programmatic profile. Disjoint and covering together
    // are what make it a partition -- exactly one way to reach every tool, and never none.
    @Test
    void programmaticProfile_ok_leavesNoToolOfTheDirectProfileUnreachable() {
        ToolCatalog catalog = catalog(DiagramThumbnails.OMIT);
        ToolManifest manifest = manifest();
        AstahToolRegistry registry = registryOf(catalog, manifest);
        Set<String> publishedDirectly = toolNamesOf(programmaticProfileProviders(catalog, manifest));

        Set<String> unreachable = new TreeSet<>();
        for (String name : manifest.namesFor(ToolManifest.Profile.DIRECT)) {
            if (!catalog.contains(name)) {
                continue;
            }
            if (!registry.mcpToolScriptCallableNames().contains(name) && !publishedDirectly.contains(name)) {
                unreachable.add(name);
            }
        }

        assertTrue(unreachable.isEmpty(),
                "neither callable from a script nor published directly: " + unreachable);
    }

    // Locking has to match the direct profile tool for tool. A tool that touches the Astah API without the lock could
    // run while another agent is midway through an edit. The manifest has no column for this.
    @Test
    void programmaticProfile_ok_locksTheSameToolsTheDirectProfileLocks() {
        ToolCatalog catalog = catalog(DiagramThumbnails.OMIT);
        ToolManifest manifest = manifest();

        Set<String> locked = new TreeSet<>();
        Set<String> unlocked = new TreeSet<>();
        for (ToolProvider provider : programmaticProfileProviders(catalog, manifest)) {
            Set<String> names = toolNamesOf(List.of(provider));
            if (provider instanceof ExclusiveToolProvider) {
                locked.addAll(names);
            } else {
                unlocked.addAll(names);
            }
        }

        for (String name : manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)) {
            if (!catalog.contains(name)) {
                continue;
            }
            if (catalog.astahLockedNames().contains(name)) {
                assertTrue(locked.contains(name),
                        "'" + name + "' holds the Astah API lock on the direct profile and must here too");
            } else {
                assertTrue(unlocked.contains(name),
                        "'" + name + "' does not hold the Astah API lock on the direct profile and must not here");
            }
        }

        // A script run serializes every tool call it makes, so it holds the lock for its whole duration.
        assertTrue(locked.contains("run_mcp_tool_script"));

        // Looking names up in the registry touches nothing that needs serializing, and taking the lock would make
        // a plain lookup queue behind another agent's edit.
        assertTrue(unlocked.contains("get_chunk_of_tools_callable_from_mcp_tool_script"));
        assertTrue(unlocked.contains("get_info_of_tools_callable_from_mcp_tool_script"));
    }

    // The deadlock this whole design is arranged to avoid: a script runs on its own thread while the request thread
    // holds the lock, and a ReentrantLock is only reentrant for the thread that owns it.
    @Test
    void catalog_ok_holdsDefinitionsThatDoNotTakeTheAstahApiLock() {
        ToolCatalog catalog = catalog(DiagramThumbnails.OMIT);

        for (ToolProvider provider : catalog.select(catalog.names()).unlocked()) {
            assertFalse(provider instanceof ExclusiveToolProvider,
                    "The mcp tool script registry must be built from providers that do not take the lock: " + provider.name());
        }
    }

    // Dropping the after-the-edit thumbnail changes what an editing tool returns alongside its result,
    // never which tools exist or what they are called.
    @Test
    void createRawToolProviders_ok_omittingThumbnailsChangesNoToolAtAll() {
        AstahProToolFactory factory = new AstahProToolFactory(workspaceDir.resolve("images"));

        Set<String> withThumbnails = toolNamesOf(factory.createRawToolProviders(DiagramThumbnails.INCLUDE));
        Set<String> withoutThumbnails = toolNamesOf(factory.createRawToolProviders(DiagramThumbnails.OMIT));

        assertEquals(new TreeSet<>(withThumbnails), new TreeSet<>(withoutThumbnails));
        assertFalse(withThumbnails.isEmpty(), "The comparison is only meaningful if tools were actually built");
    }

    // The direct profile must keep capturing. It reaches the factory through the overload that does not mention
    // thumbnails at all, and that one has to mean INCLUDE.
    @Test
    void createRawToolProviders_ok_capturesThumbnailsUnlessAskedNotTo() {
        AstahProToolFactory factory = new AstahProToolFactory(workspaceDir.resolve("images"));

        Set<String> byDefault = toolNamesOf(factory.createRawToolProviders());
        Set<String> explicitlyIncluded = toolNamesOf(factory.createRawToolProviders(DiagramThumbnails.INCLUDE));

        assertEquals(new TreeSet<>(explicitlyIncluded), new TreeSet<>(byDefault));
    }

    // The counterpart of the check above: on the direct profile every Astah provider is wrapped.
    @Test
    void select_ok_wrapsEveryAstahProviderForTheDirectProfile() {
        ToolCatalog catalog = catalog(DiagramThumbnails.INCLUDE);
        ToolManifest manifest = manifest();

        ToolCatalog.Selection selection = catalog.select(manifest.namesFor(ToolManifest.Profile.DIRECT));

        for (ToolProvider provider : selection.withAstahLock()) {
            Set<String> names = toolNamesOf(List.of(provider));
            boolean reachesAstah = catalog.astahLockedNames().containsAll(names);
            if (reachesAstah) {
                assertInstanceOf(ExclusiveToolProvider.class, provider, provider.name());
            } else {
                assertFalse(provider instanceof ExclusiveToolProvider, provider.name());
            }
        }
    }
}
