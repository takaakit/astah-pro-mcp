package com.astahpromcp.tool.astah.pro.project;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ListSnapshotTest {

    private static final String INFO_TOOL = "get_info_of_all_named_elements";

    // Stands in for a DTO of the list, whose JSON is what the snapshot ID is computed from
    record Entry(String name, String id) {
    }

    private static List<Entry> entries(int count) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entries.add(new Entry("Element" + i, "id-" + i));
        }
        return entries;
    }

    @Test
    void of_ok_splitsTheListIntoChunksOfTheGivenSize() {
        ListSnapshot<Entry> snapshot = ListSnapshot.of(entries(5), 2);

        assertEquals(3, snapshot.chunks().size());
        assertEquals(entries(5).subList(0, 2), snapshot.chunks().get(0));
        assertEquals(entries(5).subList(2, 4), snapshot.chunks().get(1));
        assertEquals(entries(5).subList(4, 5), snapshot.chunks().get(2));
    }

    @Test
    void of_ok_givesAnEmptyListOneEmptyChunk() {
        ListSnapshot<Entry> snapshot = ListSnapshot.of(List.of(), 400);

        assertEquals(List.of(List.of()), snapshot.chunks());
        assertEquals(List.of(), snapshot.chunk(snapshot.id(), 0, INFO_TOOL));
    }

    @Test
    void of_ok_givesTheSameIdToTheSameContent() {
        // Built twice, as get_info and get_chunk each build the list afresh
        assertEquals(ListSnapshot.of(entries(3), 2).id(), ListSnapshot.of(entries(3), 2).id());
    }

    @Test
    void of_ok_givesAnotherIdWhenTheContentChanges() {
        String original = ListSnapshot.of(entries(3), 2).id();

        List<Entry> renamed = entries(3);
        renamed.set(1, new Entry("Renamed", "id-1"));
        List<Entry> reordered = entries(3);
        reordered.add(reordered.remove(0));

        assertNotEquals(original, ListSnapshot.of(renamed, 2).id());
        assertNotEquals(original, ListSnapshot.of(reordered, 2).id());
        assertNotEquals(original, ListSnapshot.of(entries(2), 2).id());
        assertNotEquals(original, ListSnapshot.of(entries(4), 2).id());
    }

    @Test
    void chunk_ok_returnsTheChunkOfTheListTheIdWasIssuedFor() {
        String snapshotId = ListSnapshot.of(entries(5), 2).id();

        // A later build of the unchanged list accepts the ID it issued earlier
        assertEquals(entries(5).subList(2, 4), ListSnapshot.of(entries(5), 2).chunk(snapshotId, 1, INFO_TOOL));
    }

    @Test
    void chunk_ng_rejectsTheIdOfAListThatHasChangedSince() {
        String snapshotId = ListSnapshot.of(entries(5), 2).id();
        ListSnapshot<Entry> changed = ListSnapshot.of(entries(6), 2);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> changed.chunk(snapshotId, 1, INFO_TOOL));
        assertTrue(e.getMessage().contains(snapshotId), e.getMessage());
        assertTrue(e.getMessage().contains("call '" + INFO_TOOL + "' again"), e.getMessage());
    }

    @Test
    void chunk_ng_rejectsAnIndexOutsideTheList() {
        ListSnapshot<Entry> snapshot = ListSnapshot.of(entries(5), 2);

        IllegalArgumentException below = assertThrows(IllegalArgumentException.class, () -> snapshot.chunk(snapshot.id(), -1, INFO_TOOL));
        IllegalArgumentException above = assertThrows(IllegalArgumentException.class, () -> snapshot.chunk(snapshot.id(), 3, INFO_TOOL));

        assertTrue(below.getMessage().contains("from 0 to 2"), below.getMessage());
        assertTrue(above.getMessage().contains("from 0 to 2"), above.getMessage());
    }
}
