package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentChunkDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

// Tool that fetches Color Palette guide content and returns it in chunks
@Slf4j
public class ColorPaletteGuideTool implements ToolProvider, RemoteDocumentTool {

    private final KnowledgeToolSupport.ContentCache contentCache;
    private final Path outputDirectory;
    private final HttpClient httpClient;

    public ColorPaletteGuideTool(Path outputDirectory) {
        this(outputDirectory, KnowledgeToolSupport.newHttpClient());
    }

    public ColorPaletteGuideTool(Path outputDirectory, HttpClient httpClient) {
        this.outputDirectory = outputDirectory;
        this.httpClient = httpClient;
        this.contentCache = new KnowledgeToolSupport.ContentCache();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_color_palette_guide",
                    "Return the total number of chunks and the data of the first chunk of Color Palette guide. If you want to learn the rules for refining the color scheme of diagrams, use this tool.",
                    this::getColorPaletteGuideInfo,
                    NoInputDTO.class,
                    DocumentDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "get_chunk_of_color_palette_guide",
                    "Return the chunk data of Color Palette guide. If no chunk data exists, an empty string is set.",
                    this::getColorPaletteGuideChunk,
                    ChunkDTO.class,
                    DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create color palette guide tools", e);
            return List.of();
        }
    }

    private DocumentDTO getColorPaletteGuideInfo(NoInputDTO param) throws IOException {
        log.debug("Get Color Palette guide: {}", param);

        DocumentDTO cached = contentCache.describe();
        if (cached != null) {
            log.info("Color Palette guide already loaded, returning from cache.");
            return cached;
        }

        log.info("Loading Color Palette guide from web pages.");
        List<String> urls = KnowledgeToolSupport.readUrlsFromResource(getClass(), "color-palette-guide-url.txt");
        if (urls.isEmpty()) {
            throw new IOException("Color Palette guide URL resource not found or is empty.");
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

        String outputFileName = "color_palette_guide.md";
        Path outputPath = outputDirectory.resolve(outputFileName);
        Files.writeString(outputPath, allTextContentString, StandardCharsets.UTF_8);
        log.info("Color Palette guide saved to file: {}", outputPath.toAbsolutePath());

        return KnowledgeToolSupport.chunkAndCache(allTextContentString, contentCache);
    }

    private DocumentChunkDTO getColorPaletteGuideChunk(ChunkDTO param) {
        log.debug("Get Color Palette guide chunk: {}", param);

        return new DocumentChunkDTO(contentCache.chunkAt(param.chunkIndex()));
    }
}
