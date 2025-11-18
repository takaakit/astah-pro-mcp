package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentChunkDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

// Tool that fetches Plantuml guide content and returns it in chunks
@Slf4j
public class PlantumlGuideTool implements ToolProvider {

    private static final int CHUNK_SIZE = 51200; // characters 50KB (50 * 1024)
    private final List<String> contentCache;
    private final Path outputDirectory;
    private final HttpClient httpClient;

    public PlantumlGuideTool(Path outputDirectory) {
        this(outputDirectory, HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public PlantumlGuideTool(Path outputDirectory, HttpClient httpClient) {
        this.outputDirectory = outputDirectory;
        this.httpClient = httpClient;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_info_of_puml_guide",
                            "Return the total number of chunks and the data of the first chunk of PlantUML guide. If you want to know how to write PlantUML code, use this tool.",
                            this::getPlantumlGuideInfo,
                            NoInputDTO.class,
                            DocumentDTO.class),

                    ToolSupport.definition(
                            "get_chunk_of_puml_guide",
                            "Return the chunk data of PlantUML guide. If no chunk data exists, an empty string is set.",
                            this::getPlantumlGuideChunk,
                            ChunkDTO.class,
                            DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create plantuml guide tools", e);
            return List.of();
        }
    }

    private DocumentDTO getPlantumlGuideInfo(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get PlantUML guide: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("PlantUML guide already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading PlantUML guide from web pages.");
        List<String> urls = KnowledgeToolSupport.readUrlsFromResource(getClass(), "plantuml-guide-url.txt");
        if (urls.isEmpty()) {
            throw new IOException("PlantUML guide URL resource not found or is empty.");
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

        String outputFileName = "plantuml_guide.txt";
        Path outputPath = outputDirectory.resolve(outputFileName);
        Files.writeString(outputPath, allTextContentString, StandardCharsets.UTF_8);
        log.info("PlantUML guide saved to file: {}", outputPath.toAbsolutePath());

        List<String> chunks = KnowledgeToolSupport.splitText(allTextContentString, CHUNK_SIZE);
        if (chunks.isEmpty()) {
            chunks.add("");
        }

        contentCache.clear();
        contentCache.addAll(chunks);

        return new DocumentDTO(chunks.size(), chunks.get(0));
    }

    private DocumentChunkDTO getPlantumlGuideChunk(McpSyncServerExchange exchange, ChunkDTO param) {
        log.debug("Get PlantUML guide chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }
}
