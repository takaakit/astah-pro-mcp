package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.JsonSupport;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

// A list of the whole project, split into chunks and identified by a hash of its content.
// Nothing is kept between calls: a chunk is read by building the list again and comparing the hash, so a list that changed in between (the model was edited, or another project was opened) is reported instead of being mixed with the chunks read before.
record ListSnapshot<T>(String id, List<List<T>> chunks) {

    // 128 bits of SHA-256 are plenty to tell two lists apart, and keep the ID short for the agent to pass back
    private static final int ID_BYTES = 16;

    // Split the list into chunks and identify it by its content. An empty list still has one, empty, chunk.
    static <T> ListSnapshot<T> of(List<T> items, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < items.size(); i += chunkSize) {
            chunks.add(List.copyOf(items.subList(i, Math.min(items.size(), i + chunkSize))));
        }
        if (chunks.isEmpty()) {
            chunks.add(List.of());
        }

        // The JSON of the DTOs covers exactly what the list shows, so a change the list does not show leaves the ID as it was
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(JsonSupport.OBJ_MAPPER.writeValueAsBytes(items));

        } catch (NoSuchAlgorithmException e) {
            // Every Java platform is required to provide SHA-256
            throw new IllegalStateException("SHA-256 is not available.", e);
        }

        return new ListSnapshot<>(HexFormat.of().formatHex(digest, 0, ID_BYTES), List.copyOf(chunks));
    }

    // The chunk at the index, provided this is still the list the snapshot ID was issued for
    List<T> chunk(String snapshotId, int chunkIndex, String infoToolName) {
        if (!id.equals(snapshotId)) {
            throw new IllegalStateException(String.format(
                    "The list has changed since '%s' returned the snapshot ID '%s': the model was edited, or another project was opened. A chunk of the changed list cannot be combined with the chunks read before, so call '%s' again and read every chunk with the snapshot ID it returns.",
                    infoToolName, snapshotId, infoToolName));
        }

        if (chunkIndex < 0 || chunkIndex >= chunks.size()) {
            String range = chunks.size() == 1
                    ? "the list fits in one chunk, so it must be 0"
                    : "the list is split into " + chunks.size() + " chunks, so it must be from 0 to " + (chunks.size() - 1);
            throw new IllegalArgumentException("'chunkIndex' is " + chunkIndex + ", but " + range + ".");
        }

        return chunks.get(chunkIndex);
    }
}
