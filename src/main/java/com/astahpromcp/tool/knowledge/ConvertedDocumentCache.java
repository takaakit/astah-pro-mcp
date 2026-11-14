package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.knowledge.document.DocumentSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Reference documents converted from PDF, kept on disk across sessions so that each build converts a document only once.
// An entry belongs to one build: a new build converts again, so that a change to the conversion never meets a result of the old one.
@Slf4j
final class ConvertedDocumentCache {
    
    // The name of an entry: <outputBaseName>-<key>.md
    private static final Pattern ENTRY_NAME = Pattern.compile(".+-[0-9a-f]{16}\\.md");
    
    // The name of the file a process writes an entry to before renaming it: tmp-<process ID>-<random>.part
    private static final Pattern TEMPORARY_NAME = Pattern.compile("tmp-(\\d{1,18})-\\d+\\.part");
    
    private static final int KEY_LENGTH = 16;
    
    // Null when the cache is not used
    private final Path directory;
    private final String buildId;
    
    private ConvertedDocumentCache(Path directory, String buildId) {
        this.directory = directory;
        this.buildId = buildId;
    }
    
    // A cache that keeps nothing, so that every load converts the document
    static ConvertedDocumentCache disabled() {
        return new ConvertedDocumentCache(null, null);
    }
    
    // The cache of this build in the given directory, after deleting what this build does not use.
    static ConvertedDocumentCache open(Path directory, Optional<String> buildId, Collection<DocumentSource.Pdf> documents, LongPredicate isProcessAlive) {
        if (buildId.isEmpty()) {
            log.warn("The build of this MCP server is not known, so reference documents converted from PDF are not kept in {}", directory);
            return disabled();
        }
        
        ConvertedDocumentCache cache = new ConvertedDocumentCache(directory, buildId.get());
        cache.deleteStaleFiles(documents, isProcessAlive);
        return cache;
    }
    
    // Whether the process with the given ID is running
    static boolean isProcessAlive(long pid) {
        return ProcessHandle.of(pid)
                .map(ProcessHandle::isAlive)
                .orElse(false);
    }
    
    // The converted document, or empty when there is none to use, in which case the caller converts it again
    Optional<String> read(DocumentSource.Pdf pdf) {
        if (directory == null) {
            return Optional.empty();
        }
        
        Path entry = entryOf(pdf);
        if (!Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)) {
            return Optional.empty();
        }
        
        try {
            String markdown = Files.readString(entry, StandardCharsets.UTF_8);
            if (markdown.isBlank()) {
                log.warn("Ignore the empty cached document {}", entry);
                return Optional.empty();
            }
            return Optional.of(markdown);
            
        } catch (IOException e) {
            log.warn("Failed to read the cached document {}, so it is converted again", entry, e);
            return Optional.empty();
        }
    }
    
    // Keeps the converted document. A failure costs only a conversion in a later session, so it is logged and not thrown.
    void write(DocumentSource.Pdf pdf, String markdown) {
        if (directory == null) {
            return;
        }
        
        Path entry = entryOf(pdf);
        Path temporary = null;
        try {
            Files.createDirectories(directory);
            // Written under a name of its own and then renamed, so that another Astah reading the same entry never sees half of it
            temporary = Files.createTempFile(directory, "tmp-" + ProcessHandle.current().pid() + "-", ".part");
            Files.writeString(temporary, markdown, StandardCharsets.UTF_8);
            Files.move(temporary, entry, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            log.info("Converted document cached: {}", entry);
            
        } catch (IOException e) {
            log.warn("Failed to cache the converted document {}", entry, e);
            deleteQuietly(temporary);
        }
    }
    
    // The file that holds the given document for this build
    Path entryOf(DocumentSource.Pdf pdf) {
        return directory.resolve(pdf.outputBaseName() + "-" + keyOf(pdf) + ".md");
    }
    
    // Changes whenever the PDF, its pages or the build changes, so that an entry is never read for a different document or build
    private String keyOf(DocumentSource.Pdf pdf) {
        String identity = String.join("\n", pdf.url(), pdf.pageSelection(), buildId);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, KEY_LENGTH);
            
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256, which every Java platform provides, is not available", e);
        }
    }
    
    // Deletes the entries this build does not use and the temporary files of processes that have ended.
    // Leaves every other file alone, including the temporary files of running processes, which may be about to rename theirs.
    private void deleteStaleFiles(Collection<DocumentSource.Pdf> documents, LongPredicate isProcessAlive) {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        
        Set<String> entriesInUse = documents.stream()
                .map(pdf -> entryOf(pdf).getFileName().toString())
                .collect(Collectors.toSet());
        
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
            for (Path file : files) {
                if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) && isStale(file.getFileName().toString(), entriesInUse, isProcessAlive)) {
                    log.info("Delete the cached document this build does not use: {}", file);
                    deleteQuietly(file);
                }
            }
            
        } catch (IOException e) {
            log.warn("Failed to look for cached documents to delete in {}", directory, e);
        }
    }
    
    private static boolean isStale(String fileName, Set<String> entriesInUse, LongPredicate isProcessAlive) {
        if (ENTRY_NAME.matcher(fileName).matches()) {
            return !entriesInUse.contains(fileName);
        }
        
        Matcher temporary = TEMPORARY_NAME.matcher(fileName);
        if (temporary.matches()) {
            return !isProcessAlive.test(Long.parseLong(temporary.group(1)));
        }
        
        return false;
    }
    
    private static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Failed to delete {}", file, e);
        }
    }
}
