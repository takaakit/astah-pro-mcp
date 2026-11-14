package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.knowledge.document.DocumentSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ConvertedDocumentCacheTest {
    
    private static final DocumentSource.Pdf OCL = new DocumentSource.Pdf("https://example.org/ocl.pdf", "ocl");
    private static final DocumentSource.Pdf EXCERPT = new DocumentSource.Pdf("https://example.org/odm.pdf", "odm", List.of(new DocumentSource.PageRange(2, 3)));
    private static final List<DocumentSource.Pdf> DOCUMENTS = List.of(OCL, EXCERPT);
    
    private static final LongPredicate NO_PROCESS_ALIVE = pid -> false;
    
    @TempDir
    Path directory;
    
    private ConvertedDocumentCache cacheOf(String buildId) {
        return ConvertedDocumentCache.open(directory, Optional.of(buildId), DOCUMENTS, NO_PROCESS_ALIVE);
    }
    
    private Set<String> fileNames() throws Exception {
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(file -> file.getFileName().toString()).collect(Collectors.toSet());
        }
    }
    
    @Test
    void write_ok_isReadBackInALaterSessionOfTheSameBuild() throws Exception {
        cacheOf("0.3.6+1").write(OCL, "# OCL");
        
        assertEquals(Optional.of("# OCL"), cacheOf("0.3.6+1").read(OCL));
        
        Set<String> names = fileNames();
        assertEquals(1, names.size(), () -> "Only the entry may be left, not a temporary file: " + names);
        assertTrue(names.iterator().next().matches("ocl-[0-9a-f]{16}\\.md"), () -> "The entry should be named after the document: " + names);
    }
    
    @Test
    void read_ok_isEmptyForADocumentNotWrittenYet() {
        assertEquals(Optional.empty(), cacheOf("0.3.6+1").read(OCL));
    }
    
    @Test
    void read_ok_isEmptyForAnotherBuild() {
        cacheOf("0.3.6+1").write(OCL, "# OCL");
        
        assertEquals(Optional.empty(), cacheOf("0.3.6+2").read(OCL), "A new build must convert again, since its conversion may differ");
    }
    
    @Test
    void entryOf_ok_differsForAnotherPdfOrOtherPages() {
        ConvertedDocumentCache cache = cacheOf("0.3.6+1");
        Path entry = cache.entryOf(EXCERPT);
        
        assertEquals(entry, cacheOf("0.3.6+1").entryOf(EXCERPT));
        assertNotEquals(entry, cache.entryOf(new DocumentSource.Pdf("https://example.org/odm-1.2.pdf", "odm", EXCERPT.pages())));
        assertNotEquals(entry, cache.entryOf(new DocumentSource.Pdf(EXCERPT.url(), "odm", List.of(new DocumentSource.PageRange(2, 4)))));
    }
    
    @Test
    void read_ng_treatsAnEmptyEntryAsMissing() throws Exception {
        ConvertedDocumentCache cache = cacheOf("0.3.6+1");
        Files.writeString(cache.entryOf(OCL), " \n");
        
        assertEquals(Optional.empty(), cache.read(OCL));
    }
    
    @Test
    void read_ng_treatsAnEntryThatIsNotUtf8AsMissing() throws Exception {
        ConvertedDocumentCache cache = cacheOf("0.3.6+1");
        Files.write(cache.entryOf(OCL), new byte[] {(byte) 0xC3, (byte) 0x28});
        
        assertEquals(Optional.empty(), cache.read(OCL));
    }
    
    @Test
    void open_ok_deletesTheEntriesOfOtherBuildsAndLeavesOtherFilesAlone() throws Exception {
        cacheOf("0.3.6+1").write(OCL, "# old build");
        Files.writeString(directory.resolve("notes.txt"), "not an entry");
        Files.writeString(directory.resolve("ocl.md"), "not an entry either");
        
        ConvertedDocumentCache cache = cacheOf("0.3.6+2");
        cache.write(OCL, "# new build");
        
        assertEquals(Set.of(cache.entryOf(OCL).getFileName().toString(), "notes.txt", "ocl.md"), fileNames());
    }
    
    @Test
    void open_ok_deletesTheTemporaryFilesOfEndedProcessesOnly() throws Exception {
        Files.writeString(directory.resolve("tmp-100-1.part"), "being written by a running process");
        Files.writeString(directory.resolve("tmp-200-2.part"), "left behind by an ended process");
        
        ConvertedDocumentCache.open(directory, Optional.of("0.3.6+1"), DOCUMENTS, pid -> pid == 100);
        
        assertEquals(Set.of("tmp-100-1.part"), fileNames());
    }
    
    @Test
    void open_ok_neitherReadsNorWritesWithoutABuildId() throws Exception {
        Path cacheDirectory = directory.resolve("documents");
        ConvertedDocumentCache cache = ConvertedDocumentCache.open(cacheDirectory, Optional.empty(), DOCUMENTS, NO_PROCESS_ALIVE);
        
        cache.write(OCL, "# OCL");
        
        assertEquals(Optional.empty(), cache.read(OCL));
        assertFalse(Files.exists(cacheDirectory), "Without a build ID, different builds would share the entries, so nothing may be written");
    }
    
    @Test
    void open_ok_deletesNothingWithoutABuildId() throws Exception {
        cacheOf("0.3.6+1").write(OCL, "# OCL");
        Set<String> before = fileNames();
        
        ConvertedDocumentCache.open(directory, Optional.empty(), DOCUMENTS, NO_PROCESS_ALIVE);
        
        assertEquals(before, fileNames(), "Without a build ID, no entry can be told to belong to another build");
    }
    
    @Test
    void write_ng_keepsGoingWhenTheDirectoryCannotBeMade() throws Exception {
        Path file = Files.writeString(directory.resolve("a-file"), "");
        ConvertedDocumentCache cache = ConvertedDocumentCache.open(file.resolve("documents"), Optional.of("0.3.6+1"), DOCUMENTS, NO_PROCESS_ALIVE);
        
        assertDoesNotThrow(() -> cache.write(OCL, "# OCL"), "The caller already has the document, so a cache that cannot be written must not fail the load");
        assertEquals(Optional.empty(), cache.read(OCL));
    }
}
