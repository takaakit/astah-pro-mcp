package com.astahpromcp.tool.manifest;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

// Checks the manifest against the catalog at startup.
@Slf4j
public final class ToolManifestValidator {

    private ToolManifestValidator() {
    }

    public static void verify(ToolManifest manifest, Collection<ToolCatalog> catalogs) {
        Set<String> catalogNames = new LinkedHashSet<>();
        for (ToolCatalog catalog : catalogs) {
            catalogNames.addAll(catalog.names());
        }
        ToolCatalog reference = catalogs.iterator().next();

        verifyEveryToolIsDecidedAbout(manifest, catalogNames);
        verifyEveryWithheldRowStillExists(manifest, catalogNames);
        verifyEveryRowStillExists(manifest, catalogNames);
        verifyToolsAnMcpToolScriptCannotCallArePublishedDirectly(manifest, reference);
        verifyProgrammaticProfileIsASubsetOfTheDirectProfile(manifest, reference);

        log.info("Tool manifest verified: {} rows, direct={} programmatic={} withheld={}",
                manifest.size(),
                manifest.namesFor(ToolManifest.Profile.DIRECT).size(),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC).size(),
                manifest.withheldNames().size());
    }

    // A tool nobody has decided about must not be published by default, so it has to have a row.
    private static void verifyEveryToolIsDecidedAbout(ToolManifest manifest, Set<String> catalogNames) {
        Set<String> undecided = new TreeSet<>();
        for (String name : catalogNames) {
            if (manifest.find(name) == null) {
                undecided.add(name);
            }
        }
        if (!undecided.isEmpty()) {
            throw new IllegalStateException("These tools exist but no manifest row says where to publish them, so they would be published nowhere. Add a row to " + ToolManifest.RESOURCE_PATH + ": " + undecided);
        }
    }

    // A row naming a tool that no longer exists selects nothing.
    private static void verifyEveryRowStillExists(ToolManifest manifest, Set<String> catalogNames) {
        Set<String> unknown = new TreeSet<>();
        for (String name : manifest.allNames()) {
            if (!catalogNames.contains(name) && manifest.find(name).publishesTo(ToolManifest.Profile.DIRECT)) {
                unknown.add(name);
            }
        }
        if (!unknown.isEmpty()) {
            log.warn("These manifest rows name tools the catalog does not hold, so they publish nothing. Outside the Astah GUI the view manager tools are expected here: {}", unknown);
        }
    }

    // (the part that needs the catalog): a withheld row for a tool that no longer exists is stale text.
    // It publishes nowhere and is not callable from an MCP tool script either, so it does no harm beyond being wrong.
    private static void verifyEveryWithheldRowStillExists(ToolManifest manifest, Set<String> catalogNames) {
        Set<String> unknown = new TreeSet<>();
        for (String name : manifest.withheldNames()) {
            if (!catalogNames.contains(name)) {
                unknown.add(name);
            }
        }
        if (!unknown.isEmpty()) {
            log.error("These rows are withheld from every profile but the tools no longer exist: {}", unknown);
        }
    }

    // Whatever an MCP tool script cannot call has to be published directly, or the programmatic profile could not reach it at all
    private static void verifyToolsAnMcpToolScriptCannotCallArePublishedDirectly(ToolManifest manifest, ToolCatalog catalog) {
        Set<String> unreachable = new TreeSet<>();
        for (String name : catalog.notMcpToolScriptCallableNames()) {
            ToolManifest.Entry entry = manifest.find(name);
            if (entry == null || !entry.programmatic()) {
                unreachable.add(name);
            }
        }
        if (!unreachable.isEmpty()) {
            throw new IllegalStateException("These tools cannot be called from an MCP tool script and are not published on the programmatic profile either, so nothing on that profile could reach them. Set programmatic = x in "
                    + ToolManifest.RESOURCE_PATH + ": " + unreachable);
        }
    }

    // The programmatic profile is a subset of the direct profile, apart from its own two tools, which no catalog holds
    private static void verifyProgrammaticProfileIsASubsetOfTheDirectProfile(ToolManifest manifest, ToolCatalog catalog) {
        Set<String> missing = new TreeSet<>();
        for (String name : manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)) {
            if (catalog.contains(name) && !manifest.find(name).direct()) {
                missing.add(name);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("These tools are published on the programmatic profile but not on the direct profile, and the programmatic profile must stay a subset of it: " + missing);
        }
    }

    // What a profile actually registered is what the manifest selects from the catalog
    public static void verifyRegistered(String profileName,
                                        Set<String> registeredNames,
                                        ToolManifest manifest,
                                        ToolManifest.Profile profile,
                                        ToolCatalog catalog,
                                        Set<String> extraNames) {
        Set<String> expected = new TreeSet<>(extraNames);
        for (String name : manifest.namesFor(profile)) {
            if (catalog.contains(name)) {
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
