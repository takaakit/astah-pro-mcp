package com.astahpromcp.server;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.common.DiagramType;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.astah.pro.mcptoolscript.AstahMcpToolScriptHost;
import com.astahpromcp.tool.common.outputdto.GuideDTO;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptProviderFactory;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ToolNameReferencesTest {
    
    // The form of a tool function name
    private static final String NAME = "[a-z][a-z0-9]*(?:_[a-z0-9]+)+";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME);
    
    private static final Pattern NAMED_TOOLS = Pattern.compile(
            "(?:'" + NAME + "'(?:,\\s*(?:and\\s+|or\\s+)?|\\s+(?:and|or)\\s+))*(?:'|\\b)" + NAME + "'?\\s+tool\\b");
    
    @TempDir
    Path workspaceDir;
    
    // The project comparisons the tools of the catalogs share
    private ProjectDiffJobs projectDiffJobs;
    
    @BeforeEach
    void setUp() {
        projectDiffJobs = new ProjectDiffJobs(workspaceDir.resolve("project-diff"));
    }
    
    @AfterEach
    void tearDown() throws Exception {
        projectDiffJobs.shutdown();
        
        // astah_pro_mcp_guide creates a project when none is open
        ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
        if (projectAccessor.hasProject()) {
            projectAccessor.close();
        }
    }
    
    private static String guideOf(ToolCatalog catalog, String name, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = catalog.find(name).toolHandler().apply(null, new McpSchema.CallToolRequest(name, arguments, null));
        assertFalse(Boolean.TRUE.equals(result.isError()), name + ": " + result.content());
        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), GuideDTO.class).contents();
    }
    
    // The descriptions of the tool functions, the guides and the instructions of each profile, by where they come from
    private Map<String, String> textsForAgents(ToolManifest manifest) {
        Map<DiagramThumbnails, ToolCatalog> catalogs = ToolCatalogAssembler.buildAll(workspaceDir.resolve("images"), workspaceDir, workspaceDir.resolve("document-cache"), projectDiffJobs);
        Map<String, String> texts = new LinkedHashMap<>();
        
        catalogs.forEach((thumbnails, catalog) -> {
            for (ToolDefinition definition : catalog.all()) {
                texts.put(definition.toolSchema().name() + " (" + thumbnails + ")", definition.toolSchema().description());
            }
        });
        
        McpToolScriptProviderFactory.McpToolScriptTools mcpToolScriptTools = new McpToolScriptProviderFactory().createToolProviders(McpServerApp.scriptCatalog(catalogs), manifest, new AstahMcpToolScriptHost());
        for (List<ToolProvider> providers : List.of(mcpToolScriptTools.unlockedProviders(), mcpToolScriptTools.lockedProviders())) {
            for (ToolProvider provider : providers) {
                for (ToolDefinition definition : provider.createToolDefinitions()) {
                    texts.put(definition.toolSchema().name(), definition.toolSchema().description());
                }
            }
        }
        
        ToolCatalog catalog = catalogs.get(DiagramThumbnails.INCLUDE);
        for (DiagramType diagramType : DiagramType.values()) {
            String value = JsonSupport.OBJ_MAPPER.convertValue(diagramType, String.class);
            texts.put("dgm_guide " + value, guideOf(catalog, "dgm_guide", Map.of("diagramType", value)));
        }
        texts.put("astah_pro_mcp_guide", guideOf(catalog, "astah_pro_mcp_guide", Map.of()));
        
        for (McpServerApp.ServerProfileConfig profile : McpServerApp.profiles()) {
            texts.put("instructions of " + profile.name(), McpServerApp.instructionsOf(profile, manifest));
        }
        
        return texts;
    }
    
    @Test
    void textsForAgents_ok_nameOnlyToolFunctionsThatExist() {
        ToolManifest manifest = ToolManifest.load();
        Set<String> manifestNames = manifest.allNames();
        
        Set<String> named = new TreeSet<>();
        Map<String, Set<String>> unknownByText = new TreeMap<>();
        textsForAgents(manifest).forEach((where, text) -> {
            Matcher matcher = NAMED_TOOLS.matcher(text == null ? "" : text);
            while (matcher.find()) {
                Matcher names = NAME_PATTERN.matcher(matcher.group());
                while (names.find()) {
                    String name = names.group();
                    named.add(name);
                    if (!manifestNames.contains(name)) {
                        unknownByText.computeIfAbsent(where, key -> new TreeSet<>()).add(name);
                    }
                }
            }
        });
        
        // The search finds the names the texts are known to give, one of them only as the first of two names, so that it cannot pass by finding nothing
        assertTrue(named.containsAll(Set.of("run_mcp_tool_script", "astah_pro_mcp_guide", "uml_modeling_architecture_insights")), "the search for named tool functions finds too little: " + named);
        assertEquals(Map.of(), unknownByText, "the texts name tool functions that have no row in the manifest");
    }
}
