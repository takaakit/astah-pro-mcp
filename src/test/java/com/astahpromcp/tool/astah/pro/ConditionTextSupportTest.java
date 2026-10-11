package com.astahpromcp.tool.astah.pro;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionTextSupportTest {

    @Test
    void normalize_ok_stripsSurroundingWhitespace() {
        assertEquals("x > 0", ConditionTextSupport.normalize("invariant", "  x > 0 \n"));
    }

    @Test
    void normalize_ok_keepsInnerWhitespace() {
        assertEquals("a > 0\n  b > 0", ConditionTextSupport.normalize("invariant", " a > 0\n  b > 0 "));
    }

    @Test
    void normalize_ng_blank() {
        for (String blank : new String[] { null, "", "   ", "\n" }) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                ConditionTextSupport.normalize("precondition", blank);
            });

            assertEquals("The precondition must not be empty or blank.", exception.getMessage());
        }
    }

    @Test
    void requireAbsent_ok_noSameText() {
        assertDoesNotThrow(() -> {
            ConditionTextSupport.requireAbsent("invariant", "id0", "x > 0", new String[] { "x >= 0", "y > 0" }, Function.identity());
        });
    }

    @Test
    void requireAbsent_ok_caseSensitive() {
        assertDoesNotThrow(() -> {
            ConditionTextSupport.requireAbsent("invariant", "id0", "a > 0", new String[] { "A > 0" }, Function.identity());
        });
    }

    @Test
    void requireAbsent_ok_ignoresNullText() {
        assertDoesNotThrow(() -> {
            ConditionTextSupport.requireAbsent("invariant", "id0", "x > 0", new String[] { null }, Function.identity());
        });
    }

    @Test
    void requireAbsent_ng_sameText() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ConditionTextSupport.requireAbsent("invariant", "id0", "x > 0", new String[] { "y > 0", "x > 0" }, Function.identity());
        });

        assertEquals(
            "Failed to add the invariant because the element (ID: id0) already has the invariant 'x > 0'. Existing invariants: [y > 0, x > 0].",
            exception.getMessage());
    }

    @Test
    void requireAbsent_ng_sameTextStoredWithSurroundingWhitespace() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ConditionTextSupport.requireAbsent("precondition", "id0", "x > 0", new String[] { " x > 0 \n" }, Function.identity());
        });

        assertTrue(exception.getMessage().contains("already has the precondition 'x > 0'"));
    }

    @Test
    void requirePresent_ok_returnsEveryMatchAsStored() {
        List<String> sameTexts = ConditionTextSupport.requirePresent(
            "precondition", "id0", "x > 0", new String[] { "x > 0", "y > 0", " x > 0 " }, Function.identity());

        assertEquals(List.of("x > 0", " x > 0 "), sameTexts);
    }

    @Test
    void requirePresent_ng_noSameText() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ConditionTextSupport.requirePresent("postcondition", "id0", "x > 0", new String[] { "x >= 0" }, Function.identity());
        });

        assertEquals(
            "Failed to remove the postcondition because the element (ID: id0) has no postcondition 'x > 0'. Existing postconditions: [x >= 0].",
            exception.getMessage());
    }

    @Test
    void requirePresent_ng_noItems() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ConditionTextSupport.requirePresent("invariant", "id0", "x > 0", new String[0], Function.identity());
        });

        assertTrue(exception.getMessage().endsWith("Existing invariants: []."));
    }
}
