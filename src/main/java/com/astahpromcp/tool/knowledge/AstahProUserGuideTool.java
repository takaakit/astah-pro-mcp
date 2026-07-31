package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentChunkDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
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

// Tool that fetches the astah pro user guide web pages and returns them in chunks
@Slf4j
public class AstahProUserGuideTool implements ToolProvider {

    private final List<String> contentCache;
    private final Path outputDirectory;
    private final HttpClient httpClient;

    public AstahProUserGuideTool(Path outputDirectory) {
        this(outputDirectory, KnowledgeToolSupport.newHttpClient());
    }

    public AstahProUserGuideTool(Path outputDirectory, HttpClient httpClient) {
        this.outputDirectory = outputDirectory;
        this.httpClient = httpClient;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_astah_pro_user_guide",
                    "Return the total number of chunks and the data of the first chunk of astah pro user guide. If you want to learn how to use Astah, use this tool.",
                    this::getAstahProUserGuide,
                    NoInputDTO.class,
                    DocumentDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "get_chunk_of_astah_pro_user_guide",
                    "Return the chunk data of astah pro user guide. If no chunk data exists, an empty string is set.",
                    this::getAstahProUserGuideChunk,
                    ChunkDTO.class,
                    DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create astah pro user guide tools", e);
            return List.of();
        }
    }

    private DocumentDTO getAstahProUserGuide(NoInputDTO param) throws IOException {
        log.debug("Get astah pro user guide: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("astah pro user guide already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading astah pro user guide from web pages.");
        List<String> urls = KnowledgeToolSupport.readUrlsFromResource(getClass(), "astah-pro-and-uml-user-guide-url.txt");
        if (urls.isEmpty()) {
            throw new IOException("astah pro user guide URL resource not found or is empty.");
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

        String outputFileName = "astah_pro_and_uml_user_guide.md";
        Path outputPath = outputDirectory.resolve(outputFileName);
        Files.writeString(outputPath, allTextContentString, StandardCharsets.UTF_8);
        log.info("astah pro user guide saved to file: {}", outputPath.toAbsolutePath());

        return KnowledgeToolSupport.chunkAndCache(allTextContentString, contentCache);
    }

    private DocumentChunkDTO getAstahProUserGuideChunk(ChunkDTO param) {
        log.debug("Get astah pro user guide chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }
}
