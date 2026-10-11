package com.astahpromcp.tool.astah.pro;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Decides when two constraints, invariants, preconditions or postconditions are the same, so that the tools that add them and the tools that remove them agree.
public final class ConditionTextSupport {

    private ConditionTextSupport() {
    }

    // kind names what the text is, such as "invariant", and is used in the error messages.
    public static String normalize(String kind, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(String.format("The %s must not be empty or blank.", kind));
        }

        return text.strip();
    }

    // text must already be normalized.
    public static <T> void requireAbsent(String kind, String ownerId, String text, T[] items, Function<T, String> textOf) {
        if (!findSame(text, items, textOf).isEmpty()) {
            throw new IllegalArgumentException(String.format(
                "Failed to add the %s because the element (ID: %s) already has the %s '%s'. Existing %ss: %s.",
                kind, ownerId, kind, text, kind, textsOf(items, textOf)));
        }
    }

    // text must already be normalized.
    // Returns every matching item as stored, so that a remove can delete the duplicates and the texts stored with surrounding whitespace too.
    public static <T> List<T> requirePresent(String kind, String ownerId, String text, T[] items, Function<T, String> textOf) {
        List<T> sameItems = findSame(text, items, textOf);
        if (sameItems.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                "Failed to remove the %s because the element (ID: %s) has no %s '%s'. Existing %ss: %s.",
                kind, ownerId, kind, text, kind, textsOf(items, textOf)));
        }

        return sameItems;
    }

    private static <T> List<T> findSame(String text, T[] items, Function<T, String> textOf) {
        List<T> sameItems = new ArrayList<>();
        for (T item : items) {
            String itemText = textOf.apply(item);
            if (itemText != null && itemText.strip().equals(text)) {
                sameItems.add(item);
            }
        }

        return sameItems;
    }

    private static <T> List<String> textsOf(T[] items, Function<T, String> textOf) {
        List<String> texts = new ArrayList<>();
        for (T item : items) {
            texts.add(textOf.apply(item));
        }

        return texts;
    }
}
