package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.knowledge.document.DocumentSource;
import com.astahpromcp.tool.knowledge.document.DocumentType;
import com.astahpromcp.tool.knowledge.document.ReferenceDocuments;
import com.astahpromcp.tool.knowledge.inputdto.DocumentChunkIndexDTO;
import com.astahpromcp.tool.knowledge.inputdto.DocumentTypeDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentChunkDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DocumentTool implements ToolProvider {
    
    // Fetches the bytes of a PDF
    @FunctionalInterface
    interface PdfFetcher {
        byte[] fetch(String url) throws IOException;
    }
    
    // One cache per document, made up front and never added to, so that it is only ever read concurrently
    private final Map<DocumentType, KnowledgeToolSupport.ContentCache> contentCaches;
    
    // The load under way of each document. A call that comes while its document is loading waits for that load instead of fetching and converting the document a second time.
    private final ConcurrentMap<DocumentType, CompletableFuture<DocumentDTO>> loads = new ConcurrentHashMap<>();
    
    private final Path outputDirectory;
    
    private final HttpClient httpClient;
    
    private final ConvertedDocumentCache convertedDocuments;
    
    private final PdfFetcher pdfFetcher;
    
    // Without a cache on disk: every session fetches and converts the documents again
    public DocumentTool(Path outputDirectory, HttpClient httpClient) {
        this(outputDirectory, httpClient, ConvertedDocumentCache.disabled());
    }
    
    DocumentTool(Path outputDirectory, HttpClient httpClient, ConvertedDocumentCache convertedDocuments) {
        this(outputDirectory, httpClient, convertedDocuments, DocumentTool::fetchPdf);
    }
    
    DocumentTool(Path outputDirectory, HttpClient httpClient, ConvertedDocumentCache convertedDocuments, PdfFetcher pdfFetcher) {
        this.outputDirectory = outputDirectory;
        this.httpClient = httpClient;
        this.convertedDocuments = convertedDocuments;
        this.pdfFetcher = pdfFetcher;
        Map<DocumentType, KnowledgeToolSupport.ContentCache> caches = new EnumMap<>(DocumentType.class);
        for (DocumentType type : DocumentType.values()) {
            caches.put(type, new KnowledgeToolSupport.ContentCache());
        }
        this.contentCaches = Collections.unmodifiableMap(caches);
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_document",
                    "Return the total number of chunks and the data of the first chunk of the reference document named in 'documentType'. Read the remaining chunks with 'get_chunk_of_document'. The reference documents, and when to read each:\n"
                        + ReferenceDocuments.bulletListOfAll(),
                    this::getDocumentInfo,
                    DocumentTypeDTO.class,
                    DocumentDTO.class),
                
                ToolSupport.toolDefinitionReturningDto(
                    "get_chunk_of_document",
                    "Return the chunk data of the reference document named in 'documentType'. The chunk index must be in the range 0 to totalChunks - 1 (totalChunks is returned by 'get_info_of_document' for the same document, which must be called first); otherwise an error is returned.",
                    this::getDocumentChunk,
                    DocumentChunkIndexDTO.class,
                    DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create document tools", e);
            return List.of();
        }
    }
    
    private DocumentDTO getDocumentInfo(DocumentTypeDTO param) throws IOException {
        DocumentType type = requireDocumentType(param.documentType());
        KnowledgeToolSupport.ContentCache contentCache = contentCaches.get(type);
        
        DocumentDTO cached = contentCache.describe();
        if (cached != null) {
            log.info("{} already loaded, returning from cache.", type.title());
            return cached;
        }
        
        CompletableFuture<DocumentDTO> load = new CompletableFuture<>();
        CompletableFuture<DocumentDTO> loadUnderWay = loads.putIfAbsent(type, load);
        if (loadUnderWay != null) {
            log.info("{} is being loaded by another call; waiting for that load.", type.title());
            return await(loadUnderWay);
        }
        
        try {
            DocumentDTO loaded = loadIntoCache(type, contentCache);
            load.complete(loaded);
            return loaded;
            
        } catch (Throwable t) {
            // Every call that waited fails with it, and the next call loads the document again
            load.completeExceptionally(t);
            throw t;
            
        } finally {
            loads.remove(type, load);
        }
    }
    
    // A load that ended between the look at the cache and the registration of this load has already filled the cache
    private DocumentDTO loadIntoCache(DocumentType type, KnowledgeToolSupport.ContentCache contentCache) throws IOException {
        DocumentDTO loaded = contentCache.describe();
        if (loaded != null) {
            return loaded;
        }
        return KnowledgeToolSupport.chunkAndCache(load(type), contentCache);
    }
    
    // Waits for the load of another call and ends as it ended
    private static DocumentDTO await(CompletableFuture<DocumentDTO> load) throws IOException {
        try {
            return load.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Stopped waiting for the document to be loaded by another call.", e);
            
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            // A new exception, so that each call has its own stack trace, with the message the loading call reported
            throw cause.getMessage() != null ? new IOException(cause.getMessage(), cause) : new IOException(cause);
        }
    }
    
    private DocumentChunkDTO getDocumentChunk(DocumentChunkIndexDTO param) {
        DocumentType type = requireDocumentType(param.documentType());
        KnowledgeToolSupport.ContentCache contentCache = contentCaches.get(type);
        
        // A loaded document is never unloaded, so it is still loaded when chunkAt reads it
        if (!contentCache.isLoaded()) {
            throw new IllegalStateException("The document '" + type.value() + "' has not been loaded yet. Call 'get_info_of_document' with documentType '" + type.value() + "' first.");
        }
        return new DocumentChunkDTO(contentCache.chunkAt(param.chunkIndex()));
    }
    
    // A null that the binder lets through for an enum must not reach the cache lookup
    private static DocumentType requireDocumentType(DocumentType type) {
        if (type == null) {
            throw new IllegalArgumentException("'documentType' is required. Name one of the reference documents listed in the description of 'get_info_of_document'.");
        }
        return type;
    }
    
    private String load(DocumentType type) throws IOException {
        return switch (type.source()) {
            case DocumentSource.Pdf pdf -> loadPdf(type, pdf);
            case DocumentSource.WebPages webPages -> loadWebPages(type, webPages);
        };
    }
    
    private String loadPdf(DocumentType type, DocumentSource.Pdf pdf) throws IOException {
        Optional<String> cached = convertedDocuments.read(pdf);
        if (cached.isPresent()) {
            log.info("{} read from the converted document cache.", type.title());
            return cached.get();
        }
        
        log.info("Loading {} from PDF URL{}.", type.title(), pdf.pages().isEmpty() ? "" : " (pages " + pdf.pageSelection() + ")");
        try {
            byte[] bytes = pdfFetcher.fetch(pdf.url());
            String markdown = KnowledgeToolSupport.convertPdfToMarkdown(bytes, outputDirectory, pdf.outputBaseName(), pdf.pageSelection());
            convertedDocuments.write(pdf, markdown);
            return markdown;
            
        } catch (IOException e) {
            log.error("Failed to load {} from URL: {}", type.title(), pdf.url(), e);
            throw e;
        }
    }
    
    private static byte[] fetchPdf(String url) throws IOException {
        try (InputStream is = KnowledgeToolSupport.openUrlStream(url)) {
            if (is == null) {
                throw new IOException("No PDF was found at " + url);
            }
            return is.readAllBytes();
        }
    }
    
    private String loadWebPages(DocumentType type, DocumentSource.WebPages webPages) throws IOException {
        log.info("Loading {} from web pages.", type.title());
        List<String> urls = KnowledgeToolSupport.readUrlsFromResource(getClass(), webPages.urlListResource());
        if (urls.isEmpty()) {
            throw new IOException(type.title() + " URL resource not found or is empty.");
        }
        
        String allTextContentString = KnowledgeToolSupport.fetchAllOrFail(httpClient, urls, type.title());
        
        Path outputPath = outputDirectory.resolve(webPages.outputFileName());
        Files.writeString(outputPath, allTextContentString, StandardCharsets.UTF_8);
        log.info("{} saved to file: {}", type.title(), outputPath.toAbsolutePath());
        
        return allTextContentString;
    }
}
