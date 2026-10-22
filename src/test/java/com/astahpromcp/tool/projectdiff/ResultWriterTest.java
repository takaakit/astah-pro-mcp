package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.projectdiff.engine.ProjectDiffEngine;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.ContextDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ElementRefDTO;
import com.astahpromcp.tool.projectdiff.outputdto.NormalizationDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectKind;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.SummaryDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ValueEncoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResultWriterTest {

    private static final int LIMIT = 4_096;

    private static final int VALUE_CHARS = 200;

    private static final String EXPIRES_AT = "2026-10-15T10:00:00.000000+09:00";

    @TempDir
    Path dir;

    private static ProjectSideDTO side(String path) {
        return new ProjectSideDTO(ProjectKind.FILE, path, "プロジェクト", "", "", "", "", 44, List.of());
    }

    private static ChangeDTO change(int i, String value) {
        return new ChangeDTO(ChangeKind.MODIFIED, "id" + i, "", "UML:Class", List.of(), "名前 \"" + i + "\"\n改行",
                ContextDTO.empty(), ContextDTO.empty(), ContextDTO.empty(), ElementRefDTO.empty(), ElementRefDTO.empty(), 0, 1,
                List.of(new PropertyChangeDTO("UML:ModelElement.definition/@xmi.value", PropertyChangeKind.MODIFIED, "before", value, "", "", ValueEncoding.DECODED, false)));
    }

    // Write a result, and return the header ResultWriter gives for it
    private DiffResultDTO write(List<ChangeDTO> changes, List<String> warnings) throws Exception {
        Path file = dir.resolve("result.json");
        try (ResultWriter writer = new ResultWriter(file, "d1-abcdef", side("C:/a.asta"), side("C:/b.asta"), warnings, ProjectDiffRunner.NOT_COMPARED)) {
            writer.begin(new ProjectDiffEngine.Outcome(new SummaryDTO(changes.size(), 0, 0, changes.size(), 0, 0), new NormalizationDTO(1, true, List.of()), List.of()));
            for (ChangeDTO change : changes) {
                writer.accept(change);
            }
            writer.finish();
            return writer.header();
        }
    }

    // A change of many properties, each the given value at its own path
    private static ChangeDTO big(String id, int count, PropertyChangeKind kind, String before, String after) {
        List<PropertyChangeDTO> properties = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            properties.add(new PropertyChangeDTO("p" + i, kind, before, after, "", "", ValueEncoding.RAW, false));
        }
        return withProperties(new ChangeDTO(ChangeKind.MODIFIED, id, "", "UML:Class", List.of(), "Big", ContextDTO.empty(), ContextDTO.empty(),
                ContextDTO.empty(), ElementRefDTO.empty(), ElementRefDTO.empty(), 0, count, List.of()), properties);
    }

    private static ChangeDTO withProperties(ChangeDTO change, List<PropertyChangeDTO> properties) {
        return new ChangeDTO(change.kind(), change.xmiId(), change.baseXmiId(), change.type(), change.stereotypes(), change.name(), change.xmlContextOwner(),
                change.beforeContext(), change.afterContext(), change.diagram(), change.model(), 0, change.propertyCount(), properties);
    }

    private static DiffResultDTO page(DiffResultDTO header, int startIndex) throws Exception {
        return page(header, startIndex, 0);
    }

    private static DiffResultDTO page(DiffResultDTO header, int startIndex, int startPropertyIndex) throws Exception {
        return ResultPages.page(header, EXPIRES_AT, startIndex, startPropertyIndex, LIMIT, VALUE_CHARS);
    }

    // Every change of a result, read page by page as a client does, with the parts of a change split across pages joined.
    private static List<ChangeDTO> readAll(DiffResultDTO header, List<DiffResultDTO> pages) throws Exception {
        List<ChangeDTO> changes = new ArrayList<>();
        int startIndex = 0;
        int startPropertyIndex = 0;
        do {
            DiffResultDTO answer = page(header, startIndex, startPropertyIndex);
            assertTrue(ResponseLimiter.sizeOf(answer) <= LIMIT, () -> "size " + ResponseLimiter.sizeOf(answer));
            assertEquals(startIndex, answer.startIndex());
            assertEquals(startPropertyIndex, answer.startPropertyIndex());
            assertEquals(answer.nextIndex() != -1, answer.truncated());
            assertEquals(answer.changes().size(), answer.summary().returned());
            assertNotNull(reparse(answer));
            if (answer.nextPropertyIndex() > 0) {
                // It ends inside its last change, which the next page goes on with
                ChangeDTO last = answer.changes().getLast();
                assertEquals(last.propertyStart() + last.properties().size(), answer.nextPropertyIndex());
            }
            for (ChangeDTO part : answer.changes()) {
                if (part.propertyStart() == 0) {
                    changes.add(part);
                    continue;
                }
                ChangeDTO begun = changes.removeLast();
                assertEquals(begun.xmiId(), part.xmiId());
                assertEquals(begun.properties().size(), part.propertyStart());
                List<PropertyChangeDTO> joined = new ArrayList<>(begun.properties());
                joined.addAll(part.properties());
                changes.add(withProperties(part, joined));
            }
            pages.add(answer);
            startIndex = answer.nextIndex();
            startPropertyIndex = answer.nextPropertyIndex();
        } while (startIndex != -1);
        assertEquals(header.summary().total(), changes.size());
        return changes;
    }

    private static JsonNode reparse(Object dto) {
        return JsonSupport.OBJ_MAPPER.readTree(JsonSupport.OBJ_MAPPER.writeValueAsString(dto));
    }

    @Test
    void page_ok_returnsEveryChangeThatFits() throws Exception {
        DiffResultDTO header = write(List.of(change(1, "a"), change(2, "b")), List.of());

        DiffResultDTO answer = page(header, 0);

        assertEquals(2, answer.changes().size());
        assertEquals(0, answer.startIndex());
        assertEquals(-1, answer.nextIndex());
        assertFalse(answer.truncated());
        assertEquals(2, answer.summary().returned());
        assertEquals(0, answer.summary().valuesCut());
        assertEquals(EXPIRES_AT, answer.expiresAt());
        assertEquals(dir.resolve("result.json").toString(), answer.fullDiffPath());
    }

    @Test
    void page_ok_readsTheWholeResultPageByPageAndTheFileHoldsItAll() throws Exception {
        List<ChangeDTO> changes = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            changes.add(change(i, "value " + i));
        }
        DiffResultDTO header = write(changes, List.of());

        List<DiffResultDTO> pages = new ArrayList<>();
        List<ChangeDTO> read = readAll(header, pages);

        assertTrue(pages.size() > 1, "the result takes more than one page");
        // Small changes come whole
        assertTrue(pages.stream().allMatch(answer -> answer.nextPropertyIndex() == 0));
        assertEquals(changes, read);
        // The same page twice is the same
        assertEquals(page(header, 7), page(header, 7));

        JsonNode full = JsonSupport.OBJ_MAPPER.readTree(Files.readString(dir.resolve("result.json"), StandardCharsets.UTF_8));
        assertEquals(200, full.get("changes").size());
        assertEquals("名前 \"5\"\n改行", full.get("changes").get(5).get("name").asString());
    }

    @Test
    void page_ok_cutsALongValueAndSaysSo() throws Exception {
        String huge = "x".repeat(100_000);
        DiffResultDTO header = write(List.of(change(1, huge), change(2, "b")), List.of());

        DiffResultDTO answer = page(header, 0);

        assertEquals(2, answer.changes().size());
        PropertyChangeDTO property = answer.changes().get(0).properties().get(0);
        assertTrue(property.after().length() < 1_000, "the value is cut short");
        assertTrue(property.after().contains("100000 characters"), property.after());
        assertTrue(property.cut());
        assertFalse(answer.changes().get(1).properties().get(0).cut());
        assertEquals(1, answer.summary().valuesCut());
        assertTrue(ResponseLimiter.sizeOf(answer) <= LIMIT);

        JsonNode full = JsonSupport.OBJ_MAPPER.readTree(Files.readString(dir.resolve("result.json"), StandardCharsets.UTF_8));
        assertEquals(huge, full.get("changes").get(0).get("properties").get(0).get("after").asString());
        assertFalse(full.get("changes").get(0).get("properties").get(0).get("cut").asBoolean());
    }

    @Test
    void page_ok_splitsAChangeTooLargeForAPageByItsProperties() throws Exception {
        ChangeDTO big = big("big", 20, PropertyChangeKind.MODIFIED, "a".repeat(300), "b".repeat(300));
        DiffResultDTO header = write(List.of(change(1, "a"), big, change(2, "b")), List.of());

        List<DiffResultDTO> pages = new ArrayList<>();
        List<ChangeDTO> read = readAll(header, pages);

        // The change that fits comes alone in the first page, and the large one starts a page of its own
        assertEquals(1, pages.get(0).changes().size());
        assertEquals(1, pages.get(0).nextIndex());
        assertEquals(0, pages.get(0).nextPropertyIndex());
        assertTrue(pages.stream().filter(answer -> answer.changes().stream().anyMatch(change -> change.xmiId().equals("big"))).count() > 1,
                "the large change takes more than one page");
        // Joined, its parts are the whole change with its values cut as usual, not further
        ResponseLimiter.Shown usual = ResponseLimiter.show(big, VALUE_CHARS);
        assertEquals(List.of(change(1, "a"), usual.change(), change(2, "b")), read);
        assertEquals(usual.valuesCut(), pages.stream().mapToInt(answer -> answer.summary().valuesCut()).sum());
    }

    @Test
    void page_ok_splitsAChangeOfManyShortValuesWithoutCuttingThem() throws Exception {
        // Like the style properties of a diagram: many values, each too short for cutting it to make it shorter, at paths
        // longer than the last resort length
        List<PropertyChangeDTO> properties = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            String path = "JUDE:Diagram.customStyleMap/JUDE:Diagram.styleProperty[key=" + "k".repeat(60) + "." + i + "]/@value";
            properties.add(new PropertyChangeDTO(path, PropertyChangeKind.REMOVED, "v".repeat(120), "", "", "", ValueEncoding.RAW, false));
        }
        ChangeDTO big = withProperties(big("big", 40, PropertyChangeKind.REMOVED, "", ""), properties);
        DiffResultDTO header = write(List.of(big, change(2, "b")), List.of());

        List<DiffResultDTO> pages = new ArrayList<>();
        List<ChangeDTO> read = readAll(header, pages);

        assertTrue(pages.size() > 2, "the large change takes more than one page");
        assertEquals(List.of(big, change(2, "b")), read);
        assertTrue(pages.stream().allMatch(answer -> answer.summary().valuesCut() == 0));
    }

    @Test
    void page_ok_cutsAPropertyThatDoesNotFitAloneToTheLastResortLength() throws Exception {
        // Three values of 3 bytes a character, each still 200 characters long cut as usual
        PropertyChangeDTO property = new PropertyChangeDTO("p0", PropertyChangeKind.MODIFIED, "あ".repeat(300), "い".repeat(300), "う".repeat(300),
                "", ValueEncoding.RAW, false);
        ChangeDTO huge = withProperties(big("huge", 1, PropertyChangeKind.MODIFIED, "", ""), List.of(property));
        DiffResultDTO header = write(List.of(huge, change(2, "b")), List.of());

        List<DiffResultDTO> pages = new ArrayList<>();
        List<ChangeDTO> read = readAll(header, pages);

        PropertyChangeDTO shown = read.get(0).properties().get(0);
        assertTrue(shown.before().startsWith("あ".repeat(100) + "... (cut short; 300"), shown.before());
        assertTrue(shown.beforeRefName().startsWith("う".repeat(100) + "... (cut short; 300"), shown.beforeRefName());
        assertTrue(shown.cut());
        assertEquals(3, pages.get(0).summary().valuesCut());
    }

    @Test
    void page_ok_shortensTheOtherFieldsWhenTheyAloneTakeTooMuch() throws Exception {
        List<String> warnings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            warnings.add("warning " + i + " " + "w".repeat(1_000));
        }
        DiffResultDTO header = write(List.of(change(1, "a")), warnings);

        DiffResultDTO answer = page(header, 0);

        assertTrue(ResponseLimiter.sizeOf(answer) <= LIMIT, () -> "size " + ResponseLimiter.sizeOf(answer));
        assertTrue(answer.shortened());
        assertEquals(1, answer.changes().size());
        assertFalse(answer.truncated());
        assertEquals(dir.resolve("result.json").toString(), answer.fullDiffPath());
        assertNotNull(reparse(answer));
    }

    @Test
    void page_ok_answersAResultWithoutChangesFromZero() throws Exception {
        DiffResultDTO header = write(List.of(), List.of());

        DiffResultDTO answer = page(header, 0);

        assertTrue(answer.changes().isEmpty());
        assertEquals(-1, answer.nextIndex());
        assertEquals(0, answer.nextPropertyIndex());
        assertFalse(answer.truncated());
    }

    @Test
    void page_ng_refusesAStartIndexOutOfRange() throws Exception {
        DiffResultDTO header = write(List.of(change(1, "a"), change(2, "b")), List.of());

        assertThrows(IllegalArgumentException.class, () -> page(header, 2));
        assertThrows(IllegalArgumentException.class, () -> page(header, -1));
        DiffResultDTO none = write(List.of(), List.of());
        assertThrows(IllegalArgumentException.class, () -> page(none, 1));
    }

    @Test
    void page_ng_refusesAStartPropertyIndexOutOfRange() throws Exception {
        DiffResultDTO header = write(List.of(change(1, "a"), big("big", 3, PropertyChangeKind.ADDED, "", "v")), List.of());

        // Each change has its properties counted from 0: the first has one, the second three
        assertThrows(IllegalArgumentException.class, () -> page(header, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> page(header, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> page(header, 1, 3));
        assertEquals(2, page(header, 1, 2).changes().get(0).propertyStart());
        DiffResultDTO none = write(List.of(), List.of());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> page(none, 0, 1));
        assertTrue(e.getMessage().contains("no changes"), e.getMessage());
    }
}
