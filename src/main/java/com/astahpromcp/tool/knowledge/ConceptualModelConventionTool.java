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

// Tool that fetches conceptual model convention content and returns it in chunks
@Slf4j
public class ConceptualModelConventionTool implements ToolProvider {

    private static final int CHUNK_SIZE = 51200; // characters 50KB (50 * 1024)
    private final List<String> contentCache;
    private final Path outputDirectory;
    private final HttpClient httpClient;

    public ConceptualModelConventionTool(Path outputDirectory) {
        this(outputDirectory, HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public ConceptualModelConventionTool(Path outputDirectory, HttpClient httpClient) {
        this.outputDirectory = outputDirectory;
        this.httpClient = httpClient;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_info_of_concp_model_conv",
                            "Return the total number of chunks and the data of the first chunk of Conceptual Model Conventions. When creating or editing a conceptual model, use this tool to consult the conventions.",
                            this::getConceptualModelConventionInfo,
                            NoInputDTO.class,
                            DocumentDTO.class),

                    ToolSupport.definition(
                            "get_chunk_of_concp_model_conv",
                            "Return the chunk data of Conceptual Model Conventions. If no chunk data exists, an empty string is set.",
                            this::getConceptualModelConventionChunk,
                            ChunkDTO.class,
                            DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create Conceptual Model Conventions tools", e);
            return List.of();
        }
    }

    private DocumentDTO getConceptualModelConventionInfo(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get Conceptual Model Conventions: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("Conceptual Model Conventions already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading Conceptual Model Conventions from web pages.");
        List<String> urls = KnowledgeToolSupport.readUrlsFromResource(getClass(), "conceptual-model-conventions-url.txt");
        if (urls.isEmpty()) {
            throw new IOException("Conceptual Model Conventions URL resource not found or is empty.");
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

        String outputFileName = "conceptual_model_conventions.txt";
        Path outputPath = outputDirectory.resolve(outputFileName);
        Files.writeString(outputPath, allTextContentString, StandardCharsets.UTF_8);
        log.info("Conceptual Model Conventions saved to file: {}", outputPath.toAbsolutePath());

        List<String> chunks = KnowledgeToolSupport.splitText(allTextContentString, CHUNK_SIZE);
        if (chunks.isEmpty()) {
            chunks.add("");
        }

        contentCache.clear();
        contentCache.addAll(chunks);

        return new DocumentDTO(chunks.size(), chunks.get(0));
    }

    private DocumentChunkDTO getConceptualModelConventionChunk(McpSyncServerExchange exchange, ChunkDTO param) {
        log.debug("Get Conceptual Model Conventions chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }
}
