package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.ContextDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ElementRefDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.ValueEncoding;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// How long values are cut short for an answer
public class ResponseLimiterTest {

    private static ChangeDTO withProperty(PropertyChangeKind kind, String before, String after) {
        return new ChangeDTO(ChangeKind.MODIFIED, "c1", "", "UML:Class", List.of(), "C",
                ContextDTO.empty(), ContextDTO.empty(), ContextDTO.empty(), ElementRefDTO.empty(), ElementRefDTO.empty(), 0, 1,
                List.of(new PropertyChangeDTO("UML:ModelElement.definition/@xmi.value", kind, before, after, "", "", ValueEncoding.DECODED, false)));
    }

    @Test
    void cutAround_ok_showsAChangePastTheLimit() {
        // The value Sample.asta was checked with: 3,000 characters, of which only the 2,501st changed
        String before = "A".repeat(3_000);
        String after = "A".repeat(2_500) + "B" + "A".repeat(499);

        String[] shown = ResponseLimiter.cutAround(before, after, 2_000);

        assertNotEquals(shown[0], shown[1], "the two values look different");
        assertTrue(shown[1].contains("B"), shown[1]);
        assertTrue(shown[0].startsWith("(2400 characters left out, the same in 'before' and 'after') ..."), shown[0]);
        assertTrue(shown[1].startsWith("(2400 characters left out, the same in 'before' and 'after') ..."), shown[1]);
        assertTrue(shown[1].endsWith("(cut short; 3000 characters in all)"), shown[1]);
        // 100 characters of what leads up to the change, then the change
        assertEquals('B', shown[1].charAt("(2400 characters left out, the same in 'before' and 'after') ...".length() + 100));
    }

    @Test
    void cutAround_ok_startsFromTheBeginningWhenTheChangeShowsThere() {
        String before = "x" + "A".repeat(2_999);
        String after = "y" + "A".repeat(2_999);

        String[] shown = ResponseLimiter.cutAround(before, after, 2_000);

        assertTrue(shown[0].startsWith("xAAA"), shown[0]);
        assertTrue(shown[1].startsWith("yAAA"), shown[1]);
        // What is left out at the end is the same in both
        assertTrue(shown[0].endsWith("... (the last 1000 characters, the same in 'before' and 'after', left out; 3000 characters in all)"), shown[0]);
    }

    @Test
    void cutAround_ok_keepsAValueThatFitsWhole() {
        String before = "A".repeat(2_000);
        String after = "A".repeat(2_001);

        String[] shown = ResponseLimiter.cutAround(before, after, 2_000);

        assertEquals(before, shown[0], "exactly at the limit, a value is kept whole");
        assertNotEquals(after, shown[1], "one character over the limit, it is cut");
        assertTrue(shown[1].contains("2001 characters in all"), shown[1]);
    }

    @Test
    void cutAround_ok_neverSplitsASurrogatePair() {
        String pair = "😀";  // an emoji, two chars
        String before = pair.repeat(1_500) + "a";
        String after = pair.repeat(1_500) + "b";

        String[] shown = ResponseLimiter.cutAround(before, after, 1_001);

        for (String value : shown) {
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (Character.isHighSurrogate(c)) {
                    assertTrue(i + 1 < value.length() && Character.isLowSurrogate(value.charAt(i + 1)), "a high surrogate at " + i + " has its pair");
                } else if (Character.isLowSurrogate(c)) {
                    assertTrue(i > 0 && Character.isHighSurrogate(value.charAt(i - 1)), "a low surrogate at " + i + " has its pair");
                }
            }
        }
        assertTrue(shown[1].contains("b"), "the change shows");
    }

    @Test
    void shorten_ok_neverSplitsASurrogatePairAndKeepsJapanese() {
        // Long enough after the cut for cutting it to make it shorter
        String value = "あ".repeat(10) + "😀" + "い".repeat(100);

        String shown = ResponseLimiter.shorten(value, 11);

        assertTrue(shown.startsWith("あ".repeat(10) + "... (cut short;"), shown);
        assertEquals(value, ResponseLimiter.shorten(value, value.length()));
    }

    @Test
    void shorten_ok_keepsAValueTheNoteWouldLeaveNoShorter() {
        // Cut to 100, a value comes to 100 characters and a note of 38, "... (cut short; 138 characters in all)"
        assertEquals("a".repeat(138), ResponseLimiter.shorten("a".repeat(138), 100), "cutting would not make it shorter");
        assertTrue(ResponseLimiter.shorten("a".repeat(139), 100).length() < 139, "one character more, cutting makes it shorter");
        // The same at the usual length, with a note of 39
        assertEquals("a".repeat(2_039), ResponseLimiter.shorten("a".repeat(2_039), 2_000));
        assertTrue(ResponseLimiter.shorten("a".repeat(2_040), 2_000).length() < 2_040);
    }

    @Test
    void cutAround_ok_keepsAValueTheNotesWouldLeaveNoShorter() {
        // A change near the end of a value a little over the limit: cut around it, 55 of its 150 characters would stay,
        // between notes of 62 and 35 characters on what was left out
        String before = "A".repeat(120) + "x" + "A".repeat(29);
        String after = "A".repeat(120) + "y" + "A".repeat(29);

        String[] shown = ResponseLimiter.cutAround(before, after, 100);

        assertEquals(before, shown[0]);
        assertEquals(after, shown[1]);
    }

    @Test
    void show_ok_neverCutsThePath() {
        // As long as the longest path in a comparison with Sample.asta, and longer than the last resort length
        String path = "JUDE:UPresentation.customStyleMap/JUDE:UPresentation.styleProperty[key=font.color.subitem.2qjo-ed12edff1e795b935ef5735f30641815.alpha]/@value";
        ChangeDTO change = new ChangeDTO(ChangeKind.REMOVED, "c1", "", "JUDE:ClassifierPresentation", List.of(), "C",
                ContextDTO.empty(), ContextDTO.empty(), ContextDTO.empty(), ElementRefDTO.empty(), ElementRefDTO.empty(), 0, 1,
                List.of(new PropertyChangeDTO(path, PropertyChangeKind.REMOVED, "FF", "", "", "", ValueEncoding.RAW, false)));

        ResponseLimiter.Shown shown = ResponseLimiter.show(change, 100);

        assertEquals(change, shown.change());
        assertEquals(0, shown.valuesCut());
    }

    @Test
    void show_ok_cuttingFurtherNeverMakesAChangeLarger() {
        // Values on both sides of where cutting starts to make them shorter, at the last resort length and the usual one
        List<PropertyChangeDTO> properties = new ArrayList<>();
        for (int length : new int[] {50, 101, 120, 138, 139, 150, 1_999, 2_001, 2_039, 2_040, 2_500}) {
            for (String c : List.of("a", "あ")) {
                String value = c.repeat(length);
                properties.add(new PropertyChangeDTO("p", PropertyChangeKind.ADDED, "", value, "", "", ValueEncoding.RAW, false));
                properties.add(new PropertyChangeDTO("p", PropertyChangeKind.MODIFIED, value, c.repeat(length - 10) + "b" + c.repeat(9), "", "", ValueEncoding.RAW, false));
                properties.add(new PropertyChangeDTO("p", PropertyChangeKind.MODIFIED, value, "b" + c.repeat(length - 1), "", "", ValueEncoding.RAW, false));
            }
        }
        ChangeDTO change = new ChangeDTO(ChangeKind.MODIFIED, "c1", "", "UML:Class", List.of(), "C",
                ContextDTO.empty(), ContextDTO.empty(), ContextDTO.empty(), ElementRefDTO.empty(), ElementRefDTO.empty(), 0, properties.size(), properties);

        ChangeDTO usual = ResponseLimiter.show(change, 2_000).change();
        ChangeDTO lastResort = ResponseLimiter.show(change, 100).change();

        for (int i = 0; i < properties.size(); i++) {
            PropertyChangeDTO original = properties.get(i);
            PropertyChangeDTO cut = usual.properties().get(i);
            PropertyChangeDTO cutFurther = lastResort.properties().get(i);
            int at = i;
            assertTrue(bytes(cut.before()) <= bytes(original.before()) && bytes(cut.after()) <= bytes(original.after()), () -> "property " + at);
            assertTrue(bytes(cutFurther.before()) <= bytes(cut.before()) && bytes(cutFurther.after()) <= bytes(cut.after()), () -> "property " + at);
        }
        assertTrue(ResponseLimiter.sizeOf(usual) <= ResponseLimiter.sizeOf(change));
        assertTrue(ResponseLimiter.sizeOf(lastResort) <= ResponseLimiter.sizeOf(usual));
    }

    private static int bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    @Test
    void show_ok_marksAndCountsTheValuesCut() {
        ChangeDTO change = withProperty(PropertyChangeKind.MODIFIED, "A".repeat(3_000), "A".repeat(2_500) + "B" + "A".repeat(499));

        ResponseLimiter.Shown shown = ResponseLimiter.show(change, 2_000);

        assertEquals(2, shown.valuesCut());
        // both in the one property, none in the other fields
        assertEquals(List.of(2), shown.propertyValuesCut());
        assertEquals(0, shown.otherValuesCut());
        assertTrue(shown.change().properties().get(0).cut());
        assertTrue(shown.change().properties().get(0).after().contains("B"));
    }

    @Test
    void show_ok_cutsAnAddedValueFromItsStart() {
        ChangeDTO change = withProperty(PropertyChangeKind.ADDED, "", "B" + "A".repeat(2_999));

        ResponseLimiter.Shown shown = ResponseLimiter.show(change, 2_000);

        assertEquals(1, shown.valuesCut());
        assertTrue(shown.change().properties().get(0).after().startsWith("BAAA"));
        assertTrue(shown.change().properties().get(0).after().endsWith("(cut short; 3000 characters in all)"));
    }

    @Test
    void show_ok_leavesShortValuesAlone() {
        ChangeDTO change = withProperty(PropertyChangeKind.MODIFIED, "a", "b");

        ResponseLimiter.Shown shown = ResponseLimiter.show(change, 2_000);

        assertEquals(0, shown.valuesCut());
        assertEquals(change, shown.change());
    }
}
