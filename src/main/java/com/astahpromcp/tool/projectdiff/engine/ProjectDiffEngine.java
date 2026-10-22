package com.astahpromcp.tool.projectdiff.engine;

import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.NormalizationDTO;
import com.astahpromcp.tool.projectdiff.outputdto.RuleCountDTO;
import com.astahpromcp.tool.projectdiff.outputdto.SummaryDTO;
import com.astahpromcp.tool.projectdiff.outputdto.assembler.ChangeDTOAssembler;
import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;
import com.astahpromcp.tool.projectdiff.xmi.XmiProperty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
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

    // One side of the comparison: the XMI, what was read of it, and the name its project goes by
    public record Input(Path xmi, XmiIndex index, String projectName) {
    }

    // What the comparison found, apart from the changes the sink received
    public record Outcome(SummaryDTO summary, NormalizationDTO normalization, List<String> warnings) {
    }

    private ProjectDiffEngine() {
    }

    // verifiedVersions holds the XMI versions the normalization rules were checked against, as "exporterVersion/currentModelVersion"
    public static Outcome compare(Input base, Input target, Set<String> verifiedVersions, ChangeSink sink) throws IOException {
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
            warnings.add(written + " this comparison has not been checked against, so parts that the XMI recreates with a new xmi.id on every load were not matched. They appear as removed and added.");
        }

        Comparison comparison = Comparison.compare(base.index(), target.index(), normalize);

        Map<ToleratedDifference, Integer> tolerated = new EnumMap<>(ToleratedDifference.class);
        List<Comparison.Item> items = normalize
                ? withoutToleratedDifferences(comparison, base, target, tolerated)
                : comparison.items();

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
        comparison.counts().forEach((rule, count) -> counts.add(new RuleCountDTO(rule.ruleName(), count)));
        for (ToleratedDifference difference : ToleratedDifference.values()) {
            counts.add(new RuleCountDTO(difference.ruleName(), tolerated.getOrDefault(difference, 0)));
        }
        NormalizationDTO normalization = new NormalizationDTO(NormalizationRule.VERSION, normalize, counts);

        Outcome outcome = new Outcome(new SummaryDTO(items.size(), added, removed, modified, 0, 0), normalization, List.copyOf(warnings));
        sink.begin(outcome);

        for (int from = 0; from < items.size(); from += BATCH_SIZE) {
            List<Comparison.Item> batch = items.subList(from, Math.min(items.size(), from + BATCH_SIZE));
            emit(comparison, base, target, batch, sink);
        }
        return outcome;
    }

    // The items, without the modified elements whose only differences are values Astah recomputes on its own by less than
    // the tolerance allows (see ToleratedDifference). Their values are read in full first: the index keeps only a digest.
    private static List<Comparison.Item> withoutToleratedDifferences(Comparison comparison, Input base, Input target,
                                                                     Map<ToleratedDifference, Integer> tolerated) throws IOException {
        Map<Comparison.Item, ToleratedDifference> candidates = new HashMap<>();
        Set<String> baseIds = new HashSet<>();
        Set<String> targetIds = new HashSet<>();
        for (Comparison.Item item : comparison.items()) {
            if (item.kind() != ChangeKind.MODIFIED || item.moved()) {
                continue;
            }
            XmiIndex.Element before = base.index().get(item.baseIndex());
            XmiIndex.Element after = target.index().get(item.targetIndex());
            ToleratedDifference difference = ToleratedDifference.covering(after);
            if (difference != null && difference.covers(before)) {
                candidates.put(item, difference);
                baseIds.add(before.id());
                targetIds.add(after.id());
            }
        }
        if (candidates.isEmpty()) {
            return comparison.items();
        }

        Map<String, List<XmiProperty>> baseProperties = XmiIndex.readProperties(base.xmi(), baseIds);
        Map<String, List<XmiProperty>> targetProperties = XmiIndex.readProperties(target.xmi(), targetIds);

        List<Comparison.Item> kept = new ArrayList<>();
        for (Comparison.Item item : comparison.items()) {
            ToleratedDifference difference = candidates.get(item);
            if (difference != null && onlyTolerated(difference, comparison,
                    baseProperties.getOrDefault(base.index().get(item.baseIndex()).id(), List.of()),
                    targetProperties.getOrDefault(target.index().get(item.targetIndex()).id(), List.of()))) {
                tolerated.merge(difference, 1, Integer::sum);
                continue;
            }
            kept.add(item);
        }
        return kept;
    }

    // Whether every value that differs between the two is one the given difference tolerates
    private static boolean onlyTolerated(ToleratedDifference difference, Comparison comparison, List<XmiProperty> before, List<XmiProperty> after) {
        Map<String, XmiProperty> beforeByPath = new HashMap<>();
        for (XmiProperty property : before) {
            beforeByPath.put(property.path(), property);
        }
        Map<String, XmiProperty> afterByPath = new HashMap<>();
        for (XmiProperty property : after) {
            afterByPath.put(property.path(), property);
        }
        if (!beforeByPath.keySet().equals(afterByPath.keySet())) {
            return false;
        }
        for (XmiProperty old : before) {
            XmiProperty current = afterByPath.get(old.path());
            String comparable = current.reference() ? comparison.baseIdOf(current.value()) : current.value();
            if (old.reference() == current.reference() && old.value().equals(comparable)) {
                continue;
            }
            if (old.reference() || current.reference() || !difference.tolerates(old.path(), old.value(), current.value())) {
                return false;
            }
        }
        return true;
    }

    private static void emit(Comparison comparison, Input base, Input target, List<Comparison.Item> batch, ChangeSink sink) throws IOException {
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

        ChangeDTOAssembler.Side baseSide = new ChangeDTOAssembler.Side(base.index(), XmiIndex.readProperties(base.xmi(), baseIds), base.projectName());
        ChangeDTOAssembler.Side targetSide = new ChangeDTOAssembler.Side(target.index(), XmiIndex.readProperties(target.xmi(), targetIds), target.projectName());

        for (Comparison.Item item : batch) {
            sink.accept(ChangeDTOAssembler.toDTO(item, comparison, baseSide, targetSide));
        }
    }

    // The version of the XMI format, as the verified versions are written
    private static String versionOf(XmiIndex index) {
        return index.exporterVersion() + "/" + index.currentModelVersion();
    }
}
