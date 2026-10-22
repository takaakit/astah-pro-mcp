package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.SummaryDTO;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

// Makes the answer of get_proj_diff_result for one page of a result: the changes from a given position, as many as fit,
// read from the result file through the index ResultWriter wrote next to it. Reading only those changes keeps every
// page as quick to get as the first, however long the result. A change too large for a page is split across pages by
// its properties, so that every page keeps within the size limit, however large a change.
final class ResultPages {

    private static final String INDEX_FILE_NAME = "result.idx";

    // Each change has its start and its end in the result file, as two longs
    private static final int INDEX_ENTRY_BYTES = 2 * Long.BYTES;

    // Room kept for the separators between changes and for the numbers filled in once the page is known
    private static final int RESERVE_BYTES = 512;

    private ResultPages() {
    }

    static Path indexOf(Path resultFile) {
        return resultFile.resolveSibling(INDEX_FILE_NAME);
    }

    // The page that starts with the change at startIndex, from its property at startPropertyIndex. header is what
    // ResultWriter.header() gave, with the result's file. The changes that fit come whole; a change too large for a page
    // starts a page of its own and goes on over as many pages as it takes. Every page holds at least one property, so
    // that every page moves on, and keeps within maxBytes, unless one property does not fit alone even with the values
    // of its part cut to the last resort length.
    static DiffResultDTO page(DiffResultDTO header, String expiresAt, int startIndex, int startPropertyIndex, int maxBytes, int maxValueChars) throws IOException {
        checkStart(header, startIndex, startPropertyIndex);
        int total = header.summary().total();

        DiffResultDTO skeleton = new DiffResultDTO(header.diffId(), header.status(), expiresAt, header.failure(), header.base(), header.target(),
                header.normalization(), header.warnings(), header.notCompared(), header.summary(), startIndex, startPropertyIndex, -1, 0, List.of(),
                false, false, header.fullDiffPath());
        // The fields other than the changes keep at least half of the page for the changes
        skeleton = ResponseLimiter.fitHeader(skeleton, maxBytes / 2);
        long budget = maxBytes - ResponseLimiter.sizeOf(skeleton) - RESERVE_BYTES;

        List<Part> parts = new ArrayList<>();
        int nextIndex = -1;
        int nextPropertyIndex = 0;
        Path resultFile = Path.of(header.fullDiffPath());
        try (FileChannel json = FileChannel.open(resultFile, StandardOpenOption.READ);
             FileChannel index = FileChannel.open(indexOf(resultFile), StandardOpenOption.READ)) {
            long used = 0;
            for (int i = startIndex; i < total; i++) {
                ChangeDTO change = read(json, index, i);
                int from = i == startIndex ? startPropertyIndex : 0;
                int count = change.properties().size();
                Measured shown = new Measured(ResponseLimiter.show(change, maxValueChars));
                long rest = shown.bytes(from, count);
                if (used + rest <= budget) {
                    parts.add(new Part(i, shown.shown(), from, count));
                    used += rest;
                    continue;
                }
                if (!parts.isEmpty()) {
                    // It starts the next page, which has all its room for it
                    nextIndex = i;
                    nextPropertyIndex = from;
                    break;
                }

                // Alone in the page and still too large for it: as many of its properties as fit
                int to = shown.end(from, budget);
                if (to == from) {
                    // Not even one property fits: the first one, with the values of the part cut further, whatever it then takes
                    shown = new Measured(ResponseLimiter.show(change, McpServerConfig.PROJECT_DIFF_LAST_RESORT_VALUE_CHARS));
                    to = Math.min(from + 1, count);
                }
                parts.add(new Part(i, shown.shown(), from, to));
                if (to < count) {
                    nextIndex = i;
                    nextPropertyIndex = to;
                    break;
                }
                used += shown.bytes(from, to);
            }
        }

        DiffResultDTO answer = compose(skeleton, parts, nextIndex, nextPropertyIndex);
        // The budget leaves room already; should the page still not fit, it gives up what it holds from its end, down to one property
        while (!parts.isEmpty() && ResponseLimiter.sizeOf(answer) > maxBytes) {
            Part last = parts.getLast();
            if (last.to() - last.from() > 1) {
                parts.set(parts.size() - 1, new Part(last.index(), last.shown(), last.from(), last.to() - 1));
                nextIndex = last.index();
                nextPropertyIndex = last.to() - 1;
            } else if (parts.size() > 1) {
                parts.removeLast();
                nextIndex = last.index();
                nextPropertyIndex = last.from();
            } else {
                break;
            }
            answer = compose(skeleton, parts, nextIndex, nextPropertyIndex);
        }
        return answer;
    }

    // Refuses a page that does not start at one of the changes and at one of its properties: startIndex and
    // startPropertyIndex 0 for the first page, then the 'nextIndex' and the 'nextPropertyIndex' of the page before
    static void checkStart(DiffResultDTO header, int startIndex, int startPropertyIndex) throws IOException {
        int total = header.summary().total();
        if (startIndex < 0 || (startIndex >= total && !(total == 0 && startIndex == 0))) {
            throw new IllegalArgumentException("The startIndex " + startIndex + " is out of range: the result has " + total
                    + " changes, counted from 0. Pass 0 for the first page, then the 'nextIndex' of the previous answer.");
        }
        if (startPropertyIndex == 0) {
            return;
        }
        int count = startPropertyIndex < 0 || total == 0 ? 0 : propertyCountOf(header, startIndex);
        if (startPropertyIndex < 0 || startPropertyIndex >= count) {
            String has = total == 0 ? "the result has no changes" : "the change at startIndex " + startIndex + " has " + count + " properties, counted from 0";
            throw new IllegalArgumentException("The startPropertyIndex " + startPropertyIndex + " is out of range: " + has
                    + ". Pass 0 for the first page, then the 'nextPropertyIndex' of the previous answer.");
        }
    }

    private static int propertyCountOf(DiffResultDTO header, int i) throws IOException {
        Path resultFile = Path.of(header.fullDiffPath());
        try (FileChannel json = FileChannel.open(resultFile, StandardOpenOption.READ);
             FileChannel index = FileChannel.open(indexOf(resultFile), StandardOpenOption.READ)) {
            return read(json, index, i).properties().size();
        }
    }

    private static DiffResultDTO compose(DiffResultDTO skeleton, List<Part> parts, int nextIndex, int nextPropertyIndex) {
        SummaryDTO summary = skeleton.summary();
        int valuesCut = parts.stream().mapToInt(Part::valuesCut).sum();
        List<ChangeDTO> changes = parts.stream().map(Part::change).toList();
        return new DiffResultDTO(skeleton.diffId(), skeleton.status(), skeleton.expiresAt(), skeleton.failure(), skeleton.base(), skeleton.target(),
                skeleton.normalization(), skeleton.warnings(), skeleton.notCompared(),
                new SummaryDTO(summary.total(), summary.added(), summary.removed(), summary.modified(), changes.size(), valuesCut),
                skeleton.startIndex(), skeleton.startPropertyIndex(), nextIndex, nextPropertyIndex, changes, nextIndex != -1, skeleton.shortened(),
                skeleton.fullDiffPath());
    }

    // The part of the change at the given index of the result that a page holds: its properties from 'from' up to 'to', as shown
    private record Part(int index, ResponseLimiter.Shown shown, int from, int to) {

        ChangeDTO change() {
            return part(shown.change(), from, to);
        }

        int valuesCut() {
            return shown.otherValuesCut() + shown.propertyValuesCut().subList(from, to).stream().mapToInt(Integer::intValue).sum();
        }
    }

    private static ChangeDTO part(ChangeDTO change, int from, int to) {
        List<PropertyChangeDTO> properties = from == 0 && to == change.properties().size() ? change.properties() : List.copyOf(change.properties().subList(from, to));
        return new ChangeDTO(change.kind(), change.xmiId(), change.baseXmiId(), change.type(), change.stereotypes(), change.name(), change.xmlContextOwner(),
                change.beforeContext(), change.afterContext(), change.diagram(), change.model(), from, change.propertyCount(), properties);
    }

    // A shown change, with what each part of it takes in a page
    private record Measured(ResponseLimiter.Shown shown, int[] propertyBytes) {

        Measured(ResponseLimiter.Shown shown) {
            this(shown, shown.change().properties().stream().mapToInt(ResponseLimiter::sizeOf).toArray());
        }

        // What the part from 'from' up to 'to' takes, with the comma that sets it apart from the change before it
        long bytes(int from, int to) {
            long bytes = ResponseLimiter.sizeOf(part(shown.change(), from, from)) + 1;
            for (int p = from; p < to; p++) {
                bytes += propertyBytes[p] + (p > from ? 1 : 0);
            }
            return bytes;
        }

        // The end of the longest part from 'from' that takes at most 'room'; 'from' itself when not even one property fits
        int end(int from, long room) {
            long bytes = ResponseLimiter.sizeOf(part(shown.change(), from, from)) + 1;
            int to = from;
            while (to < propertyBytes.length) {
                bytes += propertyBytes[to] + (to > from ? 1 : 0);
                if (bytes > room) {
                    break;
                }
                to++;
            }
            return to;
        }
    }

    // The change at the given index of the result
    private static ChangeDTO read(FileChannel json, FileChannel index, int i) throws IOException {
        ByteBuffer entry = ByteBuffer.allocate(INDEX_ENTRY_BYTES);
        readFully(index, entry, (long) i * INDEX_ENTRY_BYTES);
        entry.flip();
        long start = entry.getLong();
        long end = entry.getLong();

        ByteBuffer bytes = ByteBuffer.allocate(Math.toIntExact(end - start));
        readFully(json, bytes, start);
        if (bytes.capacity() == 0 || bytes.get(0) != '{') {
            throw new IOException("The index of the result does not match its file at change " + i + ".");
        }
        return JsonSupport.OBJ_MAPPER.readValue(bytes.array(), ChangeDTO.class);
    }

    private static void readFully(FileChannel channel, ByteBuffer buffer, long position) throws IOException {
        long at = position;
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer, at);
            if (read < 0) {
                throw new EOFException("The result file ends before the change it was asked for.");
            }
            at += read;
        }
    }
}
