package com.astahpromcp.tool.projectdiff.engine;

import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Which element of the base is which element of the target, and which of them differ. Elements are matched by their
// xmi.id first; only the ones left over are matched by the normalization rules.
public final class Comparison {

    // One element that differs. baseIndex is -1 for an added element, targetIndex -1 for a removed one.
    public record Item(ChangeKind kind, int baseIndex, int targetIndex, boolean moved) {
    }

    private final XmiIndex base;
    private final XmiIndex target;
    private final int[] baseOf;
    private final List<Item> items;
    private final Map<NormalizationRule, Integer> counts;

    private Comparison(XmiIndex base, XmiIndex target, int[] baseOf, List<Item> items, Map<NormalizationRule, Integer> counts) {
        this.base = base;
        this.target = target;
        this.baseOf = baseOf;
        this.items = items;
        this.counts = counts;
    }

    public static Comparison compare(XmiIndex base, XmiIndex target, boolean normalize) {
        int[] baseOf = new int[target.elements().size()];
        int[] targetOf = new int[base.elements().size()];
        Arrays.fill(baseOf, -1);
        Arrays.fill(targetOf, -1);

        // The same xmi.id on both sides is the same element
        for (XmiIndex.Element element : target.elements()) {
            int baseIndex = base.indexOf(element.id());
            if (baseIndex >= 0) {
                baseOf[element.index()] = baseIndex;
                targetOf[baseIndex] = element.index();
            }
        }

        Map<NormalizationRule, Integer> counts = new EnumMap<>(NormalizationRule.class);
        if (normalize) {
            for (NormalizationRule rule : NormalizationRule.values()) {
                counts.put(rule, matchByRule(rule, base, target, baseOf, targetOf));
            }
        }

        List<Item> items = new ArrayList<>();
        for (XmiIndex.Element element : target.elements()) {
            int baseIndex = baseOf[element.index()];
            if (baseIndex < 0) {
                items.add(new Item(ChangeKind.ADDED, -1, element.index(), false));
                continue;
            }
            XmiIndex.Element before = base.get(baseIndex);
            boolean moved = !sameParent(before, element, baseOf);
            if (moved || !before.sameValuesAs(element) || !sameReferences(before, element, base, target, baseOf)) {
                items.add(new Item(ChangeKind.MODIFIED, baseIndex, element.index(), moved));
            }
        }
        for (XmiIndex.Element element : base.elements()) {
            if (targetOf[element.index()] < 0) {
                items.add(new Item(ChangeKind.REMOVED, element.index(), -1, false));
            }
        }

        return new Comparison(base, target, baseOf, Collections.unmodifiableList(items), counts);
    }

    // Pair the parts a rule covers that are left over on both sides, by their owner (already paired), their path from it
    // and their key, and only when exactly one candidate on each side remains
    private static int matchByRule(NormalizationRule rule, XmiIndex base, XmiIndex target, int[] baseOf, int[] targetOf) {
        Map<List<Object>, List<Integer>> baseCandidates = new HashMap<>();
        for (XmiIndex.Element element : base.elements()) {
            if (targetOf[element.index()] >= 0 || element.parent() < 0 || targetOf[element.parent()] < 0 || !rule.covers(base, element)) {
                continue;
            }
            baseCandidates.computeIfAbsent(List.of(element.parent(), element.ownerPath(), rule.key(element)), key -> new ArrayList<>()).add(element.index());
        }

        Map<List<Object>, List<Integer>> targetCandidates = new HashMap<>();
        for (XmiIndex.Element element : target.elements()) {
            if (baseOf[element.index()] >= 0 || element.parent() < 0 || baseOf[element.parent()] < 0 || !rule.covers(target, element)) {
                continue;
            }
            targetCandidates.computeIfAbsent(List.of(baseOf[element.parent()], element.ownerPath(), rule.key(element)), key -> new ArrayList<>()).add(element.index());
        }

        int count = 0;
        for (Map.Entry<List<Object>, List<Integer>> entry : baseCandidates.entrySet()) {
            List<Integer> targets = targetCandidates.get(entry.getKey());
            // With more than one candidate on either side there is no telling which is which, so none is paired
            if (entry.getValue().size() != 1 || targets == null || targets.size() != 1) {
                continue;
            }
            int baseIndex = entry.getValue().get(0);
            int targetIndex = targets.get(0);
            baseOf[targetIndex] = baseIndex;
            targetOf[baseIndex] = targetIndex;
            count++;
        }
        return count;
    }

    // Whether the element sits under the same element, by the same path, on both sides
    private static boolean sameParent(XmiIndex.Element before, XmiIndex.Element after, int[] baseOf) {
        int parentAfter = after.parent() < 0 ? -1 : baseOf[after.parent()];
        if (after.parent() >= 0 && parentAfter < 0) {
            // The new parent has no counterpart in the base, so the element is under a different one
            return false;
        }
        return before.parent() == parentAfter && before.ownerPath().equals(after.ownerPath());
    }

    // Whether the element refers to the same elements on both sides, reading a target reference to an element that a
    // rule paired with a base element of another xmi.id as a reference to that base element
    private static boolean sameReferences(XmiIndex.Element before, XmiIndex.Element after, XmiIndex base, XmiIndex target, int[] baseOf) {
        if (!Arrays.equals(before.refPaths(), after.refPaths())) {
            return false;
        }
        for (int i = 0; i < before.refIds().length; i++) {
            if (!Objects.equals(before.refIds()[i], baseIdOf(after.refIds()[i], base, target, baseOf))) {
                return false;
            }
        }
        return true;
    }

    // The xmi.id in the base of the element the target knows by the given xmi.id, or the given one when it has no counterpart
    private static String baseIdOf(String targetId, XmiIndex base, XmiIndex target, int[] baseOf) {
        int targetIndex = target.indexOf(targetId);
        if (targetIndex < 0 || baseOf[targetIndex] < 0) {
            return targetId;
        }
        return base.get(baseOf[targetIndex]).id();
    }

    public String baseIdOf(String targetId) {
        return baseIdOf(targetId, base, target, baseOf);
    }

    public List<Item> items() {
        return items;
    }

    // How many pairs each rule matched, in the order the rules apply; every rule appears, with 0 when not applied
    public Map<NormalizationRule, Integer> counts() {
        Map<NormalizationRule, Integer> all = new EnumMap<>(NormalizationRule.class);
        for (NormalizationRule rule : NormalizationRule.values()) {
            all.put(rule, counts.getOrDefault(rule, 0));
        }
        return all;
    }
}
