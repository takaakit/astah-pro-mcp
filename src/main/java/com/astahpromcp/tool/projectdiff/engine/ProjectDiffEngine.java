package com.astahpromcp.tool.projectdiff.engine;

import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.NormalizationDTO;
import com.astahpromcp.tool.projectdiff.outputdto.RuleCountDTO;
import com.astahpromcp.tool.projectdiff.outputdto.SummaryDTO;
import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;
import com.astahpromcp.tool.projectdiff.xmi.XmiProperty;
import com.astahpromcp.tool.projectdiff.xmi.XmiValueCodec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Compares two XMIs that Astah wrote for the same project, and hands the changes to a sink one by one.
// It touches no Astah API, so that it can run without Astah.
public final class ProjectDiffEngine {
    
    // How many changed elements to read in full at a time. Reading them all at once would hold every value of a large
    // project in memory when two unrelated projects are compared; reading them in batches costs one more pass per batch.
    private static final int BATCH_SIZE = 10_000;
    
    // Receives the result: first what the comparison found as a whole, then the changes one by one
    public interface ChangeSink {
        default void begin(Outcome outcome) throws IOException {
        }
        
        void accept(ChangeDTO change) throws IOException;
    }
    
    // One side of the comparison
    public record Input(Path xmi, XmiIndex index, String projectName, String astahVersion) {
        public Input {
            astahVersion = astahVersion == null ? "" : astahVersion;
        }
    }
    
    // What the comparison found, apart from the changes the sink received
    public record Outcome(SummaryDTO summary, NormalizationDTO normalization, List<String> warnings) {
    }
    
    private ProjectDiffEngine() {
    }
    
    // verifiedVersions holds the XMI versions the normalization rules were checked against, as "exporterVersion/currentModelVersion".
    // checkStop is run at every element read or compared and at every change written, and stops the comparison by throwing when it is to stop.
    public static Outcome compare(Input base, Input target, Set<String> verifiedVersions, ChangeSink sink, Runnable checkStop) throws IOException {
        List<String> warnings = new ArrayList<>();
        String baseVersion = versionOf(base.index());
        String targetVersion = versionOf(target.index());
        boolean baseVerified = verifiedVersions.contains(baseVersion);
        boolean targetVerified = verifiedVersions.contains(targetVersion);
        boolean normalize = baseVerified && targetVerified;
        if (!normalize) {
            // Which side the note is about, once for both when both call for it
            String written;
            if (!baseVerified && !targetVerified) {
                written = baseVersion.equals(targetVersion)
                        ? "The XMIs of both the base, '" + base.projectName() + "', and the target, '" + target.projectName() + "', were written by an Astah whose XMI (version " + baseVersion + ")"
                        : "The XMIs of both the base, '" + base.projectName() + "' (version " + baseVersion + "), and the target, '" + target.projectName() + "' (version " + targetVersion + "), were written by an Astah whose XMI";
            } else if (!baseVerified) {
                written = "The XMI of the base, '" + base.projectName() + "', was written by an Astah whose XMI (version " + baseVersion + ")";
            } else {
                written = "The XMI of the target, '" + target.projectName() + "', was written by an Astah whose XMI (version " + targetVersion + ")";
            }
            warnings.add(written + " this comparison has not been checked against, so parts that the XMI recreates with a new xmi.id on every load were not matched. They appear as removed and added. Values that Astah recomputes on its own, such as coordinates that differ only by rounding, were also compared exactly, so they may appear as modified.");
        }
        
        Comparison comparison = Comparison.compare(base.index(), target.index(), normalize, checkStop);
        
        Map<NormalizationRule, Integer> ruleCounts = new EnumMap<>(comparison.counts());
        Map<ToleratedDifference, Integer> tolerated = new EnumMap<>(ToleratedDifference.class);
        List<Comparison.Item> items = comparison.items();
        if (normalize) {
            List<Comparison.Item> kept = withoutUpdatedVersionsGivenOnLoading(comparison, base, target, items, checkStop);
            ruleCounts.merge(NormalizationRule.DIAGRAM_UPDATED_VERSION, items.size() - kept.size(), Integer::sum);
            items = withoutToleratedDifferences(comparison, base, target, kept, tolerated, checkStop);
        }
        
        int added = 0;
        int removed = 0;
        int modified = 0;
        for (Comparison.Item item : items) {
            switch (item.kind()) {
                case ADDED -> added++;
                case REMOVED -> removed++;
                case MODIFIED -> modified++;
            }
        }
        
        List<RuleCountDTO> counts = new ArrayList<>();
        ruleCounts.forEach((rule, count) -> counts.add(new RuleCountDTO(rule.ruleName(), count)));
        for (ToleratedDifference difference : ToleratedDifference.values()) {
            counts.add(new RuleCountDTO(difference.ruleName(), tolerated.getOrDefault(difference, 0)));
        }
        NormalizationDTO normalization = new NormalizationDTO(NormalizationRule.VERSION, normalize, counts);
        
        Outcome outcome = new Outcome(new SummaryDTO(items.size(), added, removed, modified, 0, 0), normalization, List.copyOf(warnings));
        sink.begin(outcome);
        
        for (int from = 0; from < items.size(); from += BATCH_SIZE) {
            List<Comparison.Item> batch = items.subList(from, Math.min(items.size(), from + BATCH_SIZE));
            emit(comparison, base, target, batch, normalize, sink, checkStop);
        }
        return outcome;
    }
    
    // The items, without the tagged values 'jude.diagram.updated_version' that Astah gave a diagram when it loaded a file saved without one.
    private static List<Comparison.Item> withoutUpdatedVersionsGivenOnLoading(Comparison comparison, Input base, Input target,
            List<Comparison.Item> items, Runnable checkStop) throws IOException {
        Set<Integer> baseDiagrams = diagramsWithUpdatedVersion(base.index());
        Set<Integer> targetDiagrams = diagramsWithUpdatedVersion(target.index());
        
        Map<Comparison.Item, String> candidates = new HashMap<>();
        Set<String> baseIds = new HashSet<>();
        Set<String> targetIds = new HashSet<>();
        for (Comparison.Item item : items) {
            if (item.kind() == ChangeKind.REMOVED) {
                XmiIndex.Element element = base.index().get(item.baseIndex());
                if (NormalizationRule.DIAGRAM_UPDATED_VERSION.covers(base.index(), element)) {
                    int diagram = comparison.targetIndexOf(element.parent());
                    if (diagram >= 0 && !targetDiagrams.contains(diagram)) {
                        candidates.put(item, element.id());
                        baseIds.add(element.id());
                    }
                }
            } else if (item.kind() == ChangeKind.ADDED) {
                XmiIndex.Element element = target.index().get(item.targetIndex());
                if (NormalizationRule.DIAGRAM_UPDATED_VERSION.covers(target.index(), element)) {
                    int diagram = comparison.baseIndexOf(element.parent());
                    if (diagram >= 0 && !baseDiagrams.contains(diagram)) {
                        candidates.put(item, element.id());
                        targetIds.add(element.id());
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return items;
        }
        
        Map<String, List<XmiProperty>> baseProperties = XmiIndex.readProperties(base.xmi(), baseIds, checkStop);
        Map<String, List<XmiProperty>> targetProperties = XmiIndex.readProperties(target.xmi(), targetIds, checkStop);
        
        List<Comparison.Item> kept = new ArrayList<>();
        for (Comparison.Item item : items) {
            String id = candidates.get(item);
            if (id != null) {
                boolean removed = item.kind() == ChangeKind.REMOVED;
                String astahVersion = removed ? base.astahVersion() : target.astahVersion();
                String value = taggedValueOf((removed ? baseProperties : targetProperties).getOrDefault(id, List.of()));
                if (!astahVersion.isEmpty() && astahVersion.equals(value)) {
                    continue;
                }
            }
            kept.add(item);
        }
        return kept;
    }
    
    // The indexes of the diagrams that have a tagged value 'jude.diagram.updated_version' of their own
    private static Set<Integer> diagramsWithUpdatedVersion(XmiIndex index) {
        Set<Integer> diagrams = new HashSet<>();
        for (XmiIndex.Element element : index.elements()) {
            if (NormalizationRule.DIAGRAM_UPDATED_VERSION.covers(index, element)) {
                diagrams.add(element.parent());
            }
        }
        return diagrams;
    }
    
    // The value of a tagged value, decoded, or null when it has none
    private static String taggedValueOf(List<XmiProperty> properties) {
        for (XmiProperty property : properties) {
            if (property.path().equals("@value") && !property.reference()) {
                return XmiValueCodec.decode("UML:TaggedValue", false, "@value", property.value()).value();
            }
        }
        return null;
    }
    
    // The items, without the modified elements whose only differences are values Astah recomputes on its own by less than a tolerance allows (see ToleratedDifference).
    private static List<Comparison.Item> withoutToleratedDifferences(Comparison comparison, Input base, Input target, List<Comparison.Item> items,
            Map<ToleratedDifference, Integer> tolerated, Runnable checkStop) throws IOException {
        Map<Comparison.Item, List<ToleratedDifference>> candidates = new HashMap<>();
        Set<String> baseIds = new HashSet<>();
        Set<String> targetIds = new HashSet<>();
        for (Comparison.Item item : items) {
            if (item.kind() != ChangeKind.MODIFIED || item.moved()) {
                continue;
            }
            XmiIndex.Element before = base.index().get(item.baseIndex());
            XmiIndex.Element after = target.index().get(item.targetIndex());
            List<ToleratedDifference> differences = ToleratedDifference.covering(before, after);
            if (!differences.isEmpty()) {
                candidates.put(item, differences);
                baseIds.add(before.id());
                targetIds.add(after.id());
            }
        }
        if (candidates.isEmpty()) {
            return items;
        }
        
        Map<String, List<XmiProperty>> baseProperties = XmiIndex.readProperties(base.xmi(), baseIds, checkStop);
        Map<String, List<XmiProperty>> targetProperties = XmiIndex.readProperties(target.xmi(), targetIds, checkStop);
        
        List<Comparison.Item> kept = new ArrayList<>();
        for (Comparison.Item item : items) {
            List<ToleratedDifference> differences = candidates.get(item);
            if (differences != null) {
                Set<ToleratedDifference> used = tolerating(differences, comparison,
                        baseProperties.getOrDefault(base.index().get(item.baseIndex()).id(), List.of()),
                        targetProperties.getOrDefault(target.index().get(item.targetIndex()).id(), List.of()));
                if (used != null) {
                    for (ToleratedDifference difference : used) {
                        tolerated.merge(difference, 1, Integer::sum);
                    }
                    continue;
                }
            }
            kept.add(item);
        }
        return kept;
    }
    
    // The tolerated differences that account for every value that differs between the two
    private static Set<ToleratedDifference> tolerating(List<ToleratedDifference> differences, Comparison comparison,
            List<XmiProperty> before, List<XmiProperty> after) {
        Map<String, XmiProperty> beforeByPath = new HashMap<>();
        for (XmiProperty property : before) {
            beforeByPath.put(property.path(), property);
        }
        Map<String, XmiProperty> afterByPath = new HashMap<>();
        for (XmiProperty property : after) {
            afterByPath.put(property.path(), property);
        }
        if (!beforeByPath.keySet().equals(afterByPath.keySet())) {
            return null;
        }
        
        Set<ToleratedDifference> used = EnumSet.noneOf(ToleratedDifference.class);
        for (XmiProperty old : before) {
            XmiProperty current = afterByPath.get(old.path());
            String comparable = current.reference() ? comparison.baseIdOf(current.value()) : current.value();
            if (old.reference() == current.reference() && old.value().equals(comparable)) {
                continue;
            }
            if (old.reference() || current.reference()) {
                return null;
            }
            ToleratedDifference tolerating = null;
            for (ToleratedDifference difference : differences) {
                if (difference.tolerates(old.path(), old.value(), current.value())) {
                    tolerating = difference;
                    break;
                }
            }
            if (tolerating == null) {
                return null;
            }
            used.add(tolerating);
        }
        // Nothing that differs was found: keep the element as it is rather than leave it out unexplained
        return used.isEmpty() ? null : used;
    }
    
    private static void emit(Comparison comparison, Input base, Input target, List<Comparison.Item> batch, boolean tolerate, ChangeSink sink,
            Runnable checkStop) throws IOException {
        Set<String> baseIds = new HashSet<>();
        Set<String> targetIds = new HashSet<>();
        for (Comparison.Item item : batch) {
            if (item.baseIndex() >= 0) {
                baseIds.add(base.index().get(item.baseIndex()).id());
            }
            if (item.targetIndex() >= 0) {
                targetIds.add(target.index().get(item.targetIndex()).id());
            }
        }
        
        ChangeDTOAssembler.Side baseSide = new ChangeDTOAssembler.Side(base.index(), XmiIndex.readProperties(base.xmi(), baseIds, checkStop), base.projectName());
        ChangeDTOAssembler.Side targetSide = new ChangeDTOAssembler.Side(target.index(), XmiIndex.readProperties(target.xmi(), targetIds, checkStop), target.projectName());
        
        for (Comparison.Item item : batch) {
            checkStop.run();
            sink.accept(ChangeDTOAssembler.toDTO(item, comparison, baseSide, targetSide, tolerate));
        }
    }
    
    // The version of the XMI format, as the verified versions are written
    private static String versionOf(XmiIndex index) {
        return index.exporterVersion() + "/" + index.currentModelVersion();
    }
}
