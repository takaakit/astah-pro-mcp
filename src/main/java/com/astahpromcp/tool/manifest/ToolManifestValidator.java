package com.astahpromcp.tool.manifest;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

// Checks the manifest against the tools that exist, at startup.
@Slf4j
public final class ToolManifestValidator {

    private ToolManifestValidator() {
    }

    // The tools that exist are the ones the catalogs hold and the ones that run mcp tool scripts, which no catalog holds.
    public static void verify(ToolManifest manifest,
                              Collection<ToolCatalog> catalogs,
                              Set<String> mcpToolScriptToolNames,
                              boolean missingToolsStopStartup) {
        Set<String> existingNames = new LinkedHashSet<>(mcpToolScriptToolNames);
        for (ToolCatalog catalog : catalogs) {
            existingNames.addAll(catalog.names());
        }

        verifyEveryToolIsDecidedAbout(manifest, existingNames);
        verifyEveryReachingRowNamesAnExistingTool(manifest, existingNames, missingToolsStopStartup);

        log.info("Tool manifest verified: {} rows, direct={} programmatic={} mcpToolScript={}",
                manifest.size(),
                manifest.namesFor(ToolManifest.Profile.DIRECT).size(),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC).size(),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC, ToolManifest.Reach.MCP_TOOL_SCRIPT).size());
    }

    // A tool nobody has decided about must not be published by default, so it has to have a row.
    private static void verifyEveryToolIsDecidedAbout(ToolManifest manifest, Set<String> existingNames) {
        Set<String> undecided = new TreeSet<>();
        for (String name : existingNames) {
            if (manifest.find(name) == null) {
                undecided.add(name);
            }
        }
        if (!undecided.isEmpty()) {
            throw new IllegalStateException("These tools exist but no manifest row says where to publish them, so they would be published nowhere. Add a row to " + ToolManifest.RESOURCE_PATH + ": " + undecided);
        }
    }

    // A row that publishes a tool, or lets an mcp tool script call one, that does not exist provides nothing.
    private static void verifyEveryReachingRowNamesAnExistingTool(ToolManifest manifest, Set<String> existingNames, boolean missingToolsStopStartup) {
        Set<String> unknown = new TreeSet<>();
        for (String name : manifest.allNames()) {
            if (!existingNames.contains(name) && manifest.find(name).reachable()) {
                unknown.add(name);
            }
        }
        if (unknown.isEmpty()) {
            return;
        }
        if (missingToolsStopStartup) {
            throw new IllegalStateException("These manifest rows publish tools, or let an mcp tool script call tools, that do not exist, so the server would not provide what the manifest says. Look earlier in the log for a tool provider that failed to build them: " + unknown);
        }
        log.warn("These manifest rows publish tools, or let an mcp tool script call tools, that do not exist, so they provide nothing. Outside the Astah GUI the view manager tools are expected here: {}", unknown);
    }

    // What a profile actually registered is what the manifest selects from the tools that exist
    public static void verifyRegistered(String profileName,
                                        Set<String> registeredNames,
                                        ToolManifest manifest,
                                        ToolManifest.Profile profile,
                                        Set<String> existingNames) {
        Set<String> expected = new TreeSet<>();
        for (String name : manifest.namesFor(profile)) {
            if (existingNames.contains(name)) {
                expected.add(name);
            }
        }

        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(registeredNames);
        Set<String> unexpected = new TreeSet<>(registeredNames);
        unexpected.removeAll(expected);

        if (!missing.isEmpty() || !unexpected.isEmpty()) {
            throw new IllegalStateException("Profile '" + profileName + "' registered a different set of tools than the manifest selects. Missing: " + missing + ". Unexpected: " + unexpected + ".");
        }
    }
}
