package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.projectdiff.engine.ProjectDiffEngine;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffFailureDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStatus;
import com.astahpromcp.tool.projectdiff.outputdto.NormalizationDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.astahpromcp.tool.projectdiff.outputdto.SummaryDTO;
import tools.jackson.core.JsonGenerator;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

// Writes the full result of a comparison to its file one change at a time, so that the changes are never all held in
// memory, and next to it an index of where each change starts and ends in the file, from which get_proj_diff_result
// reads the changes of a page (ResultPages)
final class ResultWriter implements ProjectDiffEngine.ChangeSink, AutoCloseable {

    private final Path resultFile;
    private final Path partFile;
    private final Path indexFile;
    private final Path indexPartFile;
    private final String diffId;
    private final ProjectSideDTO base;
    private final ProjectSideDTO target;
    private final List<String> warnings;
    private final List<String> notCompared;

    private CountingOutputStream out;
    private JsonGenerator generator;
    private DataOutputStream index;
    private SummaryDTO summary = SummaryDTO.empty();
    private NormalizationDTO normalization;
    private List<String> allWarnings = List.of();
    private int written;

    ResultWriter(Path resultFile, String diffId, ProjectSideDTO base, ProjectSideDTO target, List<String> warnings, List<String> notCompared) {
        this.resultFile = resultFile;
        this.partFile = resultFile.resolveSibling(resultFile.getFileName() + ".part");
        this.indexFile = ResultPages.indexOf(resultFile);
        this.indexPartFile = indexFile.resolveSibling(indexFile.getFileName() + ".part");
        this.diffId = diffId;
        this.base = base;
        this.target = target;
        this.warnings = warnings;
        this.notCompared = notCompared;
    }

    @Override
    public void begin(ProjectDiffEngine.Outcome outcome) throws IOException {
        summary = new SummaryDTO(outcome.summary().total(), outcome.summary().added(), outcome.summary().removed(), outcome.summary().modified(), outcome.summary().total(), 0);
        normalization = outcome.normalization();
        List<String> merged = new ArrayList<>(warnings);
        merged.addAll(outcome.warnings());
        allWarnings = List.copyOf(merged);

        out = new CountingOutputStream(new BufferedOutputStream(Files.newOutputStream(partFile)));
        generator = JsonSupport.OBJ_MAPPER.createGenerator(out);
        generator.writeStartObject();
        generator.writeStringProperty("diffId", diffId);
        generator.writePOJOProperty("status", DiffStatus.DONE);
        generator.writePOJOProperty("base", base);
        generator.writePOJOProperty("target", target);
        generator.writePOJOProperty("normalization", normalization);
        generator.writePOJOProperty("warnings", allWarnings);
        generator.writePOJOProperty("notCompared", notCompared);
        generator.writePOJOProperty("summary", summary);
        generator.writeName("changes");
        generator.writeStartArray();

        index = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(indexPartFile)));
    }

    @Override
    public void accept(ChangeDTO change) throws IOException {
        // The generator writes a comma before every change but the first, and no other whitespace
        generator.flush();
        long start = out.count() + (written > 0 ? 1 : 0);
        generator.writePOJO(change);
        generator.flush();
        long end = out.count();

        index.writeLong(start);
        index.writeLong(end);
        written++;
    }

    // Finish the files, and make them the result
    void finish() throws IOException {
        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();
        out.close();
        out = null;
        index.close();
        index = null;
        // The index first, so that a result file is never there without its index
        Files.move(indexPartFile, indexFile, StandardCopyOption.REPLACE_EXISTING);
        Files.move(partFile, resultFile, StandardCopyOption.REPLACE_EXISTING);
    }

    // Everything of the answer of get_proj_diff_result but the changes of a page and expiresAt, which ResultPages fills in
    DiffResultDTO header() {
        SummaryDTO counts = new SummaryDTO(summary.total(), summary.added(), summary.removed(), summary.modified(), 0, 0);
        return new DiffResultDTO(diffId, DiffStatus.DONE, "", DiffFailureDTO.empty(), base, target, normalization, allWarnings, notCompared,
                counts, 0, 0, -1, 0, List.of(), false, false, resultFile.toString());
    }

    @Override
    public void close() {
        if (out != null) {
            try {
                if (generator != null) {
                    generator.close();
                }
                out.close();
            } catch (Exception e) {
                // The result is being abandoned; nothing more to do with it
            }
        }
        if (index != null) {
            try {
                index.close();
            } catch (IOException e) {
                // Likewise
            }
        }
        try {
            Files.deleteIfExists(partFile);
            Files.deleteIfExists(indexPartFile);
        } catch (IOException e) {
            // Deleted with the folder of the job
        }
    }

    // Counts the bytes written through it. A flush does not reach the stream below, so that flushing the generator
    // after each change only hands its buffer over, rather than writing to the disk each time.
    private static final class CountingOutputStream extends FilterOutputStream {
        private long count;
        private boolean closed;

        CountingOutputStream(OutputStream out) {
            super(out);
        }

        long count() {
            return count;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            count += len;
        }

        @Override
        public void flush() {
            // Not passed on; close() flushes
        }

        // The generator closes it too; only the first close does anything
        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            out.flush();
            out.close();
        }
    }
}
