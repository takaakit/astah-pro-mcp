package com.astahpromcp.server;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.mcptoolscript.AstahMcpToolScriptHost;
import com.astahpromcp.tool.astah.pro.ExclusiveToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptProviderFactory;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

// Which tools each profile publishes, which catalog they come from, and the instructions the profile hands its clients.
public class ProfileCatalogWiringTest {
    
    @TempDir
    Path workspaceDir;
    
    // The project comparisons the tools of the catalog share
    private ProjectDiffJobs projectDiffJobs;
    
    @BeforeEach
    void setUp() {
        projectDiffJobs = new ProjectDiffJobs(workspaceDir.resolve("project-diff"));
    }
    
    @AfterEach
    void tearDown() {
        projectDiffJobs.shutdown();
    }
    
    private Map<DiagramThumbnails, ToolCatalog> catalogs() {
        return ToolCatalogAssembler.buildAll(workspaceDir.resolve("images"), workspaceDir, workspaceDir.resolve("document-cache"), projectDiffJobs);
    }
    
    private static McpToolScriptProviderFactory.McpToolScriptTools mcpToolScriptTools(Map<DiagramThumbnails, ToolCatalog> catalogs, ToolManifest manifest) {
        return new McpToolScriptProviderFactory().createToolProviders(McpServerApp.scriptCatalog(catalogs), manifest, new AstahMcpToolScriptHost());
    }
    
    private static McpServerApp.ServerProfileConfig profile(String name) {
        return McpServerApp.profiles().stream()
                .filter(candidate -> candidate.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No profile is named " + name));
    }
    
    private static Set<String> publishedNames(List<ToolProvider> providers) {
        Set<String> names = new TreeSet<>();
        for (ToolProvider provider : providers) {
            for (ToolDefinition definition : provider.createToolDefinitions()) {
                names.add(definition.toolSchema().name());
            }
        }
        return names;
    }
    
    // The phrase that names the kind of tool function a reason keeps from mcp tool scripts, as the production code makes it from the tool functions given that reason
    private static String phraseOf(NotMcpToolScriptCallableReason reason, ToolManifest manifest) {
        List<String> names = manifest.notMcpToolScriptCallableReasons().entrySet().stream()
                .filter(entry -> entry.getValue() == reason)
                .map(Map.Entry::getKey)
                .toList();
        return reason.instructionPhrase(names);
    }
    
    @Test
    void publishingCatalog_ok_isTheCapturingOne() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        
        assertSame(catalogs.get(DiagramThumbnails.INCLUDE), McpServerApp.publishingCatalog(catalogs));
        assertNotSame(catalogs.get(DiagramThumbnails.OMIT), McpServerApp.publishingCatalog(catalogs));
    }
    
    @Test
    void scriptCatalog_ok_isTheOneWithoutCaptures() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        
        assertSame(catalogs.get(DiagramThumbnails.OMIT), McpServerApp.scriptCatalog(catalogs));
    }
    
    @Test
    void toolProvidersOf_ok_publishesDirectlyFromTheCapturingCatalogOnly() {
        Map<DiagramThumbnails, ToolCatalog> all = catalogs();
        ToolManifest manifest = ToolManifest.load();
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(all, manifest);
        Map<DiagramThumbnails, ToolCatalog> capturingOnly = Map.of(DiagramThumbnails.INCLUDE, all.get(DiagramThumbnails.INCLUDE));
        
        List<ToolProvider> providers = assertDoesNotThrow(
                () -> McpServerApp.toolProvidersOf(profile("programmatic"), capturingOnly, tools, manifest),
                "the directly published tools must come from the capturing catalog alone");
        
        assertTrue(publishedNames(providers).contains("insert_svg_img_on_dgm"),
                "insert_svg_img_on_dgm is published directly, so it comes from the capturing catalog");
    }
    
    @Test
    void toolProvidersOf_ok_publishesExactlyWhatTheManifestSelects() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.load();
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(catalogs, manifest);
        
        Set<String> existing = new TreeSet<>(McpServerApp.publishingCatalog(catalogs).names());
        existing.addAll(tools.names());
        
        for (McpServerApp.ServerProfileConfig profile : McpServerApp.profiles()) {
            Set<String> expected = new TreeSet<>(manifest.namesFor(profile.manifestColumn()));
            expected.retainAll(existing);
            
            Set<String> published = publishedNames(McpServerApp.toolProvidersOf(profile, catalogs, tools, manifest));
            
            assertEquals(expected, published, profile.name());
        }
        assertTrue(publishedNames(McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, tools, manifest))
                .containsAll(tools.names()), "the shipped manifest publishes every tool that runs mcp tool scripts on the programmatic profile");
    }
    
    @Test
    void toolProvidersOf_ok_publishesTheToolsThatRunScriptsWhereverTheManifestSaysSo() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.parse(List.of(
                "Common\trun_mcp_tool_script\tx\ts",
                "Common\tmcp_tool_script_guide\t-\tx\tno_structured_content"), "test");
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(catalogs, manifest);
        
        List<ToolProvider> direct = McpServerApp.toolProvidersOf(profile("direct"), catalogs, tools, manifest);
        List<ToolProvider> programmatic = McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, tools, manifest);
        
        assertEquals(Set.of("run_mcp_tool_script"), publishedNames(direct));
        assertEquals(Set.of("mcp_tool_script_guide"), publishedNames(programmatic));
        
        // A script run holds the Astah API lock for its whole duration, on whichever profile runs it
        assertEquals(1, direct.size());
        assertInstanceOf(ExclusiveToolProvider.class, direct.get(0));
    }
    
    @Test
    void toolProvidersOf_ok_publishesACatalogToolOnlyOnTheProgrammaticProfileWhenTheManifestSaysSo() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.parse(List.of(
                "Common\tget_class_info\t-\tx\tdirect"), "test");
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(catalogs, manifest);
        
        assertEquals(Set.of(), publishedNames(McpServerApp.toolProvidersOf(profile("direct"), catalogs, tools, manifest)));
        assertEquals(Set.of("get_class_info"), publishedNames(McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, tools, manifest)));
        
        // Held so that a script that calls it is told to call it directly, and never called from one
        assertEquals(NotMcpToolScriptCallableReason.PUBLISHED_DIRECTLY, tools.registry().notMcpToolScriptCallableReason("get_class_info"));
        assertFalse(tools.registry().mcpToolScriptCallableNames().contains("get_class_info"));
    }
    
    @Test
    void toolProvidersOf_ok_publishesNothingForAToolOnlyAnMcpToolScriptReaches() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.parse(List.of(
                "Common\tget_class_info\t-\ts"), "test");
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(catalogs, manifest);
        
        assertEquals(Set.of(), publishedNames(McpServerApp.toolProvidersOf(profile("direct"), catalogs, tools, manifest)));
        assertEquals(Set.of(), publishedNames(McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, tools, manifest)));
        assertEquals(Set.of("get_class_info"), tools.registry().mcpToolScriptCallableNames());
    }
    
    @Test
    void instructionsOf_ok_listsTheKindsOfToolAScriptCannotCallAsTheyReadBefore() {
        ToolManifest manifest = ToolManifest.load();
        
        // Every kind of tool function the shipped manifest keeps from mcp tool scripts is named, except the tool functions published directly, which need no saying
        String sentence = McpServerApp.notMcpToolScriptCallableSentence(manifest);
        Set<NotMcpToolScriptCallableReason> reasons = Set.copyOf(manifest.notMcpToolScriptCallableReasons().values());
        assertTrue(reasons.size() > 1, "the shipped manifest gives too few reasons to check: " + reasons);
        for (NotMcpToolScriptCallableReason reason : reasons) {
            String phrase = phraseOf(reason, manifest);
            if (phrase != null) {
                assertTrue(sentence.contains(phrase), reason + ": " + sentence);
            }
        }
        
        String programmatic = McpServerApp.instructionsOf(profile("programmatic"), manifest);
        assertFalse(programmatic.contains(McpServerApp.NOT_MCP_TOOL_SCRIPT_CALLABLE_PLACEHOLDER), programmatic);
        
        String direct = McpServerApp.instructionsOf(profile("direct"), manifest);
        assertEquals(profile("direct").instructions(), direct, "the direct profile's instructions hold no list to fill in");
    }
    
    @Test
    void instructionsOf_ok_tellWhenToReadTheScenariosAgain() {
        ToolManifest manifest = ToolManifest.load();

        for (String name : List.of("direct", "programmatic")) {
            String instructions = McpServerApp.instructionsOf(profile(name), manifest);

            assertTrue(instructions.contains("'read_architecture_scenarios'"), instructions);
        }
    }

    @Test
    void notMcpToolScriptCallableSentence_ok_followsTheManifest() {
        ToolManifest oneKind = ToolManifest.parse(List.of(
                "Common\topen_dgm\tx\tx\tview",
                "Common\tdgm_guide\tx\tx\tdirect"), "test");
        String oneKindSentence = McpServerApp.notMcpToolScriptCallableSentence(oneKind);
        String view = phraseOf(NotMcpToolScriptCallableReason.DRIVES_THE_VIEW, oneKind);
        assertEquals(1, oneKindSentence.split(Pattern.quote(view), -1).length - 1, "the kind is named once: " + oneKindSentence);
        assertFalse(oneKindSentence.contains(phraseOf(NotMcpToolScriptCallableReason.MANAGES_A_PROJECT_FILE, oneKind)), oneKindSentence);
        // A tool function published directly is no kind to name
        assertFalse(oneKindSentence.contains("dgm_guide"), oneKindSentence);
        
        ToolManifest twoKinds = ToolManifest.parse(List.of(
                "Common\topen_dgm\tx\tx\tview",
                "Common\tsave_proj\tx\tx\tproject_file"), "test");
        String twoKindsSentence = McpServerApp.notMcpToolScriptCallableSentence(twoKinds);
        String projectFile = phraseOf(NotMcpToolScriptCallableReason.MANAGES_A_PROJECT_FILE, twoKinds);
        // The kinds are named in the order of the reasons, whatever the order of the rows
        assertTrue(twoKindsSentence.contains(projectFile), twoKindsSentence);
        assertTrue(twoKindsSentence.indexOf(projectFile) < twoKindsSentence.indexOf(phraseOf(NotMcpToolScriptCallableReason.DRIVES_THE_VIEW, twoKinds)), twoKindsSentence);
        
        ToolManifest onlyTheGenericReason = ToolManifest.parse(List.of(
                "Common\tdgm_guide\tx\tx\tdirect"), "test");
        assertEquals("", McpServerApp.notMcpToolScriptCallableSentence(onlyTheGenericReason));
    }
}
