package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ContextDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ElementRefDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.ReferenceProjectDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ReferenceProjectStatus;

import java.util.ArrayList;
import java.util.List;

// Keeps the answer of get_proj_diff_result within its size limit, measured as UTF-8 after serialization.
final class ResponseLimiter {

    // How long a value of the fields other than the changes may stay when those have to be shortened
    private static final int SHORT_VALUE_CHARS = 200;

    // How many warnings and referenced projects to keep when those have to be shortened
    private static final int SHORT_LIST_SIZE = 3;

    // At most how much of the text that leads up to the part of a modified value that changed is shown before it
    private static final int CONTEXT_CHARS = 100;

    private ResponseLimiter() {
    }

    static int sizeOf(Object dto) {
        return JsonSupport.OBJ_MAPPER.writeValueAsBytes(dto).length;
    }

    // A value cut to at most the given length from its start, saying how long it was. A value that this would not make shorter is kept whole.
    static String shorten(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return shorterOf(value.substring(0, endBefore(value, maxChars)) + "... (cut short; " + value.length() + " characters in all)", value);
    }

    // The before and after values of a modified property, each cut to at most maxChars around the part that changed,
    // rather than from the start, so that a change past the first maxChars characters still shows. Both are cut at the
    // same place at the start, where they are the same; what is left out at the end is said to be the same in both when
    // it lies in the part both end with. A value that this would not make shorter is kept whole.
    static String[] cutAround(String before, String after, int maxChars) {
        int prefix = commonPrefix(before, after);
        int suffix = commonSuffix(before, after, prefix);
        int context = Math.min(CONTEXT_CHARS, maxChars / 4);
        // From the start when the change shows there anyway, with some of what changed; otherwise a little before the change
        int start = prefix + context <= maxChars ? 0 : prefix - context;
        if (start > 0 && Character.isLowSurrogate(before.charAt(start))) {
            start--;
        }
        return new String[] {window(before, start, suffix, maxChars), window(after, start, suffix, maxChars)};
    }

    private static String window(String value, int start, int suffix, int maxChars) {
        if (value.length() <= maxChars) {
            return value;
        }
        int end = Math.min(value.length(), start + maxChars);
        if (end < value.length()) {
            end = endBefore(value, end);
        }
        StringBuilder shown = new StringBuilder();
        if (start > 0) {
            shown.append("(").append(start).append(" characters left out, the same in 'before' and 'after') ...");
        }
        shown.append(value, start, end);
        if (end == value.length()) {
            shown.append(" (cut short; ").append(value.length()).append(" characters in all)");
        } else if (end >= value.length() - suffix) {
            shown.append("... (the last ").append(value.length() - end).append(" characters, the same in 'before' and 'after', left out; ")
                    .append(value.length()).append(" characters in all)");
        } else {
            shown.append("... (cut short; ").append(value.length()).append(" characters in all)");
        }
        return shorterOf(shown.toString(), value);
    }

    // The cut form of a value, or the value itself when the notes the cut adds leave it no shorter.
    private static String shorterOf(String cut, String value) {
        return cut.length() < value.length() ? cut : value;
    }

    // How many characters the two values start with in common, never ending inside a surrogate pair
    private static int commonPrefix(String a, String b) {
        int length = Math.min(a.length(), b.length());
        int prefix = 0;
        while (prefix < length && a.charAt(prefix) == b.charAt(prefix)) {
            prefix++;
        }
        if (prefix > 0 && prefix < length && Character.isHighSurrogate(a.charAt(prefix - 1))) {
            prefix--;
        }
        return prefix;
    }

    // How many characters the two values end with in common, not counting any of the common start
    private static int commonSuffix(String a, String b, int prefix) {
        int length = Math.min(a.length(), b.length()) - prefix;
        int suffix = 0;
        while (suffix < length && a.charAt(a.length() - 1 - suffix) == b.charAt(b.length() - 1 - suffix)) {
            suffix++;
        }
        return suffix;
    }

    // The end of a cut at most at the given position that does not split a surrogate pair
    private static int endBefore(String value, int end) {
        return end > 0 && Character.isHighSurrogate(value.charAt(end - 1)) ? end - 1 : end;
    }

    // A change as an answer shows it, and how many of its values were cut short to show it: in the fields other than its properties, and in each of its properties
    record Shown(ChangeDTO change, int otherValuesCut, List<Integer> propertyValuesCut) {

        int valuesCut() {
            return otherValuesCut + propertyValuesCut.stream().mapToInt(Integer::intValue).sum();
        }
    }

    // A change with its values cut to at most the given length. The values of a modified property are cut around the part that changed (cutAround); every other value is cut from its start.
    static Shown show(ChangeDTO change, int maxChars) {
        Counter cuts = new Counter();
        List<PropertyChangeDTO> properties = new ArrayList<>(change.properties().size());
        List<Integer> propertyValuesCut = new ArrayList<>(change.properties().size());
        for (PropertyChangeDTO property : change.properties()) {
            int before = cuts.count;
            String shownBefore;
            String shownAfter;
            if (property.change() == PropertyChangeKind.MODIFIED && !property.before().isEmpty() && !property.after().isEmpty()) {
                String[] around = cutAround(property.before(), property.after(), maxChars);
                shownBefore = cuts.note(property.before(), around[0]);
                shownAfter = cuts.note(property.after(), around[1]);
            } else {
                shownBefore = cuts.shorten(property.before(), maxChars);
                shownAfter = cuts.shorten(property.after(), maxChars);
            }
            String beforeRefName = cuts.shorten(property.beforeRefName(), maxChars);
            String afterRefName = cuts.shorten(property.afterRefName(), maxChars);
            properties.add(new PropertyChangeDTO(property.path(), property.change(), shownBefore, shownAfter, beforeRefName, afterRefName, property.encoding(),
                    property.cut() || cuts.count > before));
            propertyValuesCut.add(cuts.count - before);
        }
        int inProperties = cuts.count;
        List<String> stereotypes = new ArrayList<>();
        for (String stereotype : change.stereotypes()) {
            stereotypes.add(cuts.shorten(stereotype, maxChars));
        }
        ChangeDTO shown = new ChangeDTO(change.kind(), change.xmiId(), change.baseXmiId(), change.type(), stereotypes, cuts.shorten(change.name(), maxChars),
                shorten(change.xmlContextOwner(), maxChars, cuts), shorten(change.beforeContext(), maxChars, cuts), shorten(change.afterContext(), maxChars, cuts),
                shorten(change.diagram(), maxChars, cuts), shorten(change.model(), maxChars, cuts), change.propertyStart(), change.propertyCount(), properties);
        return new Shown(shown, cuts.count - inProperties, propertyValuesCut);
    }

    private static ContextDTO shorten(ContextDTO context, int maxChars, Counter cuts) {
        return new ContextDTO(context.xmiId(), context.type(), cuts.shorten(context.name(), maxChars), cuts.shorten(context.participant(), maxChars),
                cuts.shorten(context.ownerPath(), maxChars));
    }

    private static ElementRefDTO shorten(ElementRefDTO ref, int maxChars, Counter cuts) {
        return new ElementRefDTO(ref.xmiId(), cuts.shorten(ref.name(), maxChars));
    }

    // Counts the values cut short
    private static final class Counter {
        int count;

        String shorten(String value, int maxChars) {
            return note(value, ResponseLimiter.shorten(value, maxChars));
        }

        String note(String value, String shown) {
            if (value != null && !value.equals(shown)) {
                count++;
            }
            return shown;
        }
    }

    // An answer without changes whose other fields are shortened, step by step, until it takes at most maxBytes
    static DiffResultDTO fitHeader(DiffResultDTO answer, int maxBytes) {
        DiffResultDTO current = answer;
        if (sizeOf(current) <= maxBytes) {
            return current;
        }

        // 1. Long values in the other fields
        current = withHeader(current, shorten(current.base(), SHORT_VALUE_CHARS, Integer.MAX_VALUE), shorten(current.target(), SHORT_VALUE_CHARS, Integer.MAX_VALUE),
                shortenEach(current.warnings(), SHORT_VALUE_CHARS), current.notCompared());
        if (sizeOf(current) <= maxBytes) {
            return current;
        }

        // 2. Only the first few warnings and referenced projects
        current = withHeader(current, shorten(current.base(), SHORT_VALUE_CHARS, SHORT_LIST_SIZE), shorten(current.target(), SHORT_VALUE_CHARS, SHORT_LIST_SIZE),
                firstOf(current.warnings(), SHORT_LIST_SIZE), current.notCompared());
        if (sizeOf(current) <= maxBytes) {
            return current;
        }

        // 3. Only what says where the result is
        return withHeader(current, emptied(current.base()), emptied(current.target()), List.of(), List.of());
    }

    private static DiffResultDTO withHeader(DiffResultDTO answer, ProjectSideDTO base, ProjectSideDTO target, List<String> warnings, List<String> notCompared) {
        return new DiffResultDTO(answer.diffId(), answer.status(), answer.expiresAt(), answer.failure(), base, target,
                answer.normalization(), warnings, notCompared, answer.summary(), answer.startIndex(), answer.startPropertyIndex(), answer.nextIndex(),
                answer.nextPropertyIndex(), answer.changes(), answer.truncated(), true, answer.fullDiffPath());
    }

    private static ProjectSideDTO shorten(ProjectSideDTO side, int maxChars, int maxReferences) {
        List<ReferenceProjectDTO> references = new ArrayList<>();
        for (ReferenceProjectDTO reference : side.referenceProjects()) {
            if (references.size() >= maxReferences) {
                references.add(new ReferenceProjectDTO("... and " + (side.referenceProjects().size() - maxReferences) + " more", ReferenceProjectStatus.UNKNOWN));
                break;
            }
            references.add(new ReferenceProjectDTO(shorten(reference.path(), maxChars), reference.status()));
        }
        return new ProjectSideDTO(side.kind(), shorten(side.path(), maxChars), shorten(side.projectName(), maxChars), side.projectRootId(),
                side.captureStartedAt(), side.captureCompletedAt(), side.astahVersion(), side.projectModelVersion(), references);
    }

    private static ProjectSideDTO emptied(ProjectSideDTO side) {
        return new ProjectSideDTO(side.kind(), "", "", "", "", "", "", side.projectModelVersion(), List.of());
    }

    private static List<String> shortenEach(List<String> values, int maxChars) {
        List<String> shortened = new ArrayList<>();
        for (String value : values) {
            shortened.add(shorten(value, maxChars));
        }
        return shortened;
    }

    private static List<String> firstOf(List<String> values, int count) {
        if (values.size() <= count) {
            return values;
        }
        List<String> first = new ArrayList<>(values.subList(0, count));
        first.add("... and " + (values.size() - count) + " more");
        return first;
    }
}
