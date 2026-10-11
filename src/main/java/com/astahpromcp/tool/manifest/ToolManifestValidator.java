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
        verifyEveryWithheldRowStillExists(manifest, existingNames);
        verifyEveryPublishingRowNamesAnExistingTool(manifest, existingNames, missingToolsStopStartup);

        log.info("Tool manifest verified: {} rows, direct={} programmatic={} withheld={}",
                manifest.size(),
                manifest.namesFor(ToolManifest.Profile.DIRECT).size(),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC).size(),
                manifest.withheldNames().size());
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

    // A row that publishes a tool that does not exist publishes nothing.
    private static void verifyEveryPublishingRowNamesAnExistingTool(ToolManifest manifest, Set<String> existingNames, boolean missingToolsStopStartup) {
        Set<String> unknown = new TreeSet<>();
        for (String name : manifest.allNames()) {
            ToolManifest.Entry entry = manifest.find(name);
            if (!existingNames.contains(name) && (entry.direct() || entry.programmatic())) {
                unknown.add(name);
            }
        }
        if (unknown.isEmpty()) {
            return;
        }
        if (missingToolsStopStartup) {
            throw new IllegalStateException("These manifest rows publish tools that do not exist, so the server would not publish what the manifest says. Look earlier in the log for a tool provider that failed to build them: " + unknown);
        }
        log.warn("These manifest rows publish tools that do not exist, so they publish nothing. Outside the Astah GUI the view manager tools are expected here: {}", unknown);
    }

    // (the part that needs the catalog): a withheld row for a tool that no longer exists is stale text.
    // It publishes nowhere and is not callable from an MCP tool script either, so it does no harm beyond being wrong.
    private static void verifyEveryWithheldRowStillExists(ToolManifest manifest, Set<String> existingNames) {
        Set<String> unknown = new TreeSet<>();
        for (String name : manifest.withheldNames()) {
            if (!existingNames.contains(name)) {
                unknown.add(name);
            }
        }
        if (!unknown.isEmpty()) {
            log.error("These rows are withheld from every profile but the tools no longer exist: {}", unknown);
        }
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
