package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentChunkDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

// Tool that fetches Object-Relational Impedance Mismatch knowledge content and returns it in chunks
@Slf4j
public class ORImpedanceMismatchKnowledgeTool implements ToolProvider {

    private static final int CHUNK_SIZE = 51200; // characters 50KB (50 * 1024)
    private final List<String> contentCache;
    private final Path outputDirectory;
    private final HttpClient httpClient;

    public ORImpedanceMismatchKnowledgeTool(Path outputDirectory) {
        this(outputDirectory, HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public ORImpedanceMismatchKnowledgeTool(Path outputDirectory, HttpClient httpClient) {
        this.outputDirectory = outputDirectory;
        this.httpClient = httpClient;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_object_relational_impedance_mismatch",
                    "Return the total number of chunks and the data of the first chunk of Object-Relational Impedance Mismatch knowledge. When mapping a domain model to a relational (ER) model, or when designing object-to-RDB persistence, use this tool to consult the knowledge.",
                    this::getORImpedanceMismatchKnowledgeInfo,
                    NoInputDTO.class,
                    DocumentDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "get_chunk_of_object_relational_impedance_mismatch",
                    "Return the chunk data of Object-Relational Impedance Mismatch knowledge. If no chunk data exists, an empty string is set.",
                    this::getORImpedanceMismatchKnowledgeChunk,
                    ChunkDTO.class,
                    DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create Object-Relational Impedance Mismatch knowledge tools", e);
            return List.of();
        }
    }

    private DocumentDTO getORImpedanceMismatchKnowledgeInfo(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get Object-Relational Impedance Mismatch knowledge: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("Object-Relational Impedance Mismatch knowledge already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading Object-Relational Impedance Mismatch knowledge from web pages.");
        List<String> urls = KnowledgeToolSupport.readUrlsFromResource(getClass(), "or-impedance-mismatch-knowledge-url.txt");
        if (urls.isEmpty()) {
            throw new IOException("Object-Relational Impedance Mismatch knowledge URL resource not found or is empty.");
        }

        List<CompletableFuture<String>> futures = urls.stream()
                .filter(url -> !url.trim().isEmpty())
                .map(url -> KnowledgeToolSupport.fetchAndParse(httpClient, url))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        List<String> pageContents = allFutures.thenApply(v ->
                futures.stream().map(CompletableFuture::join).collect(Collectors.toList())
        ).join();

        StringBuilder allTextContent = new StringBuilder();
        for (String content : pageContents) {
            allTextContent.append(content).append(System.lineSeparator()).append(System.lineSeparator());
        }

        String allTextContentString = allTextContent.toString();
        allTextContentString = allTextContentString.replace("?? Copied!", "");

        String outputFileName = "or_impedance_mismatch_knowledge.txt";
        Path outputPath = outputDirectory.resolve(outputFileName);
        Files.writeString(outputPath, allTextContentString, StandardCharsets.UTF_8);
        log.info("Object-Relational Impedance Mismatch knowledge saved to file: {}", outputPath.toAbsolutePath());

        List<String> chunks = KnowledgeToolSupport.splitText(allTextContentString, CHUNK_SIZE);
        if (chunks.isEmpty()) {
            chunks.add(""); // Ensure there is at least one empty chunk
        }

        contentCache.clear();
        contentCache.addAll(chunks);

        return new DocumentDTO(chunks.size(), chunks.get(0));
    }

    private DocumentChunkDTO getORImpedanceMismatchKnowledgeChunk(McpSyncServerExchange exchange, ChunkDTO param) {
        log.debug("Get Object-Relational Impedance Mismatch knowledge chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }
}
