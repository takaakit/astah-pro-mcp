package com.astahpromcp.tool.projectdiff.outputdto.assembler;

import com.astahpromcp.tool.projectdiff.xmi.XmiProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// The values of an element, split into its lists of references and the rest.
// A list of references is the run of child elements under the same path and with the same tag that hold nothing but an
// xmi.idref, such as the 'UML:Namespace.ownedElement/UML:AssociationEnd' of a package or the
// 'JUDE:UPresentation.clients/JUDE:AssociationPresentation' of a presentation. Its members are told apart only by their
// position ('[n]'), so one removed from the middle shifts every one after it. Compared as lists of the elements they
// refer to, only the references removed and added show.
final class ReferenceLists {

    private static final String IDREF = "@xmi.idref";

    // A value that may be a member of a list: the parent path, the tag of the child element, and its index among the children of the same tag (none for the first)
    private static final Pattern MEMBER = Pattern.compile("^(?:(?<parent>.*)/)?(?<tag>[^/\\[\\]]+)(?:\\[(?<index>\\d+)])?/" + Pattern.quote(IDREF) + "$");

    // Beyond this many cells, two lists are not aligned by their longest common subsequence, which needs a table of the product of their lengths, but compared by how many times each element is referred to
    private static final long MAX_ALIGNMENT_CELLS = 4_000_000;

    private final List<XmiProperty> all;
    private final Map<String, List<Member>> lists = new LinkedHashMap<>();
    private final Set<XmiProperty> inLists = new HashSet<>();

    private record Member(int index, XmiProperty property) {
    }

    // One step of turning the base's list into the target's: a reference removed, at its index in the base's list, or a reference added, at its index in the target's
    record Edit(boolean removed, int index) {
    }

    private ReferenceLists(List<XmiProperty> all) {
        this.all = all;
    }

    static ReferenceLists of(List<XmiProperty> properties) {
        ReferenceLists split = new ReferenceLists(properties);

        // How many values sit under each child element, to tell the children that hold nothing but an xmi.idref
        Map<String, Integer> valuesUnder = new HashMap<>();
        for (XmiProperty property : properties) {
            String path = property.path();
            for (int i = path.indexOf('/'); i >= 0; i = path.indexOf('/', i + 1)) {
                valuesUnder.merge(path.substring(0, i + 1), 1, Integer::sum);
            }
        }

        for (XmiProperty property : properties) {
            if (!property.reference()) {
                continue;
            }
            Matcher matcher = MEMBER.matcher(property.path());
            if (!matcher.matches()) {
                continue;
            }
            String child = property.path().substring(0, property.path().length() - IDREF.length());
            if (valuesUnder.getOrDefault(child, 0) != 1) {
                continue;
            }
            String parent = matcher.group("parent");
            String listPath = (parent == null ? "" : parent + "/") + matcher.group("tag");
            int index = matcher.group("index") == null ? 0 : Integer.parseInt(matcher.group("index"));
            split.lists.computeIfAbsent(listPath, key -> new ArrayList<>()).add(new Member(index, property));
            split.inLists.add(property);
        }
        for (List<Member> members : split.lists.values()) {
            members.sort(Comparator.comparingInt(Member::index));
        }
        return split;
    }

    // A single reference, which each side has at most one of under the same path, stays a value of its own, so that one that refers to another element shows as modified rather than as removed and added
    static void keepSingleReferencesApart(ReferenceLists base, ReferenceLists target) {
        Set<String> paths = new HashSet<>(base.lists.keySet());
        paths.addAll(target.lists.keySet());
        for (String path : paths) {
            if (base.members(path).size() <= 1 && target.members(path).size() <= 1) {
                base.release(path);
                target.release(path);
            }
        }
    }

    private void release(String listPath) {
        List<Member> members = lists.remove(listPath);
        if (members != null) {
            for (Member member : members) {
                inLists.remove(member.property());
            }
        }
    }

    // The values that are not members of a list, in the order the XMI has them
    List<XmiProperty> others() {
        List<XmiProperty> others = new ArrayList<>(all.size());
        for (XmiProperty property : all) {
            if (!inLists.contains(property)) {
                others.add(property);
            }
        }
        return others;
    }

    // The paths of the lists, without the index of their members, in the order the XMI has them
    Set<String> listPaths() {
        return lists.keySet();
    }

    // The references of a list, in their order; empty when there is no such list
    List<XmiProperty> members(String listPath) {
        List<Member> members = lists.get(listPath);
        if (members == null) {
            return List.of();
        }
        List<XmiProperty> properties = new ArrayList<>(members.size());
        for (Member member : members) {
            properties.add(member.property());
        }
        return properties;
    }

    // The removals and additions that turn the base's list of IDs into the target's, in the order of the lists, keeping
    // their longest common subsequence. The target's IDs are given as the base knows them.
    static List<Edit> align(List<String> base, List<String> target) {
        int start = 0;
        while (start < base.size() && start < target.size() && base.get(start).equals(target.get(start))) {
            start++;
        }
        int baseEnd = base.size();
        int targetEnd = target.size();
        while (baseEnd > start && targetEnd > start && base.get(baseEnd - 1).equals(target.get(targetEnd - 1))) {
            baseEnd--;
            targetEnd--;
        }
        int n = baseEnd - start;
        int m = targetEnd - start;

        List<Edit> edits = new ArrayList<>();
        if ((long) (n + 1) * (m + 1) > MAX_ALIGNMENT_CELLS) {
            alignByCount(base.subList(start, baseEnd), target.subList(start, targetEnd), start, edits);
            return edits;
        }

        int[][] common = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                common[i][j] = base.get(start + i).equals(target.get(start + j))
                        ? common[i + 1][j + 1] + 1
                        : Math.max(common[i + 1][j], common[i][j + 1]);
            }
        }
        int i = 0;
        int j = 0;
        while (i < n && j < m) {
            if (base.get(start + i).equals(target.get(start + j))) {
                i++;
                j++;
            } else if (common[i + 1][j] >= common[i][j + 1]) {
                edits.add(new Edit(true, start + i++));
            } else {
                edits.add(new Edit(false, start + j++));
            }
        }
        while (i < n) {
            edits.add(new Edit(true, start + i++));
        }
        while (j < m) {
            edits.add(new Edit(false, start + j++));
        }
        return edits;
    }

    // For lists too long to align: the references each side has more of, removed ones in the base's order, then added ones in the target's
    private static void alignByCount(List<String> base, List<String> target, int offset, List<Edit> edits) {
        Map<String, Integer> inTarget = new HashMap<>();
        for (String id : target) {
            inTarget.merge(id, 1, Integer::sum);
        }
        Map<String, Integer> inBase = new HashMap<>();
        for (String id : base) {
            inBase.merge(id, 1, Integer::sum);
        }
        Map<String, Integer> matched = new HashMap<>();
        for (int i = 0; i < base.size(); i++) {
            String id = base.get(i);
            if (matched.getOrDefault(id, 0) < inTarget.getOrDefault(id, 0)) {
                matched.merge(id, 1, Integer::sum);
            } else {
                edits.add(new Edit(true, offset + i));
            }
        }
        Map<String, Integer> kept = new HashMap<>();
        for (int j = 0; j < target.size(); j++) {
            String id = target.get(j);
            if (kept.getOrDefault(id, 0) < inBase.getOrDefault(id, 0)) {
                kept.merge(id, 1, Integer::sum);
            } else {
                edits.add(new Edit(false, offset + j));
            }
        }
    }
}
