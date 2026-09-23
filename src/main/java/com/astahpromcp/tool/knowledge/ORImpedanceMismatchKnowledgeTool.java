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

import lombok.extern.slf4j.Slf4j;

// Tool that fetches Object-Relational Impedance Mismatch knowledge content and returns it in chunks
@Slf4j
public class ORImpedanceMismatchKnowledgeTool implements ToolProvider, RemoteDocumentTool {

    private final KnowledgeToolSupport.ContentCache contentCache;
    private final Path outputDirectory;
    private final HttpClient httpClient;

    public ORImpedanceMismatchKnowledgeTool(Path outputDirectory) {
        this(outputDirectory, KnowledgeToolSupport.newHttpClient());
    }

    public ORImpedanceMismatchKnowledgeTool(Path outputDirectory, HttpClient httpClient) {
        this.outputDirectory = outputDirectory;
        this.httpClient = httpClient;
        this.contentCache = new KnowledgeToolSupport.ContentCache();
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

    private DocumentDTO getORImpedanceMismatchKnowledgeInfo(NoInputDTO param) throws IOException {
        log.debug("Get Object-Relational Impedance Mismatch knowledge: {}", param);

        DocumentDTO cached = contentCache.describe();
        if (cached != null) {
            log.info("Object-Relational Impedance Mismatch knowledge already loaded, returning from cache.");
            return cached;
        }

        log.info("Loading Object-Relational Impedance Mismatch knowledge from web pages.");
        List<String> urls = KnowledgeToolSupport.readUrlsFromResource(getClass(), "or-impedance-mismatch-knowledge-url.txt");
        if (urls.isEmpty()) {
            throw new IOException("Object-Relational Impedance Mismatch knowledge URL resource not found or is empty.");
        }

        String allTextContentString = KnowledgeToolSupport.fetchAllOrFail(httpClient, urls, "Object-Relational Impedance Mismatch knowledge");

        String outputFileName = "or_impedance_mismatch_knowledge.md";
        Path outputPath = outputDirectory.resolve(outputFileName);
        Files.writeString(outputPath, allTextContentString, StandardCharsets.UTF_8);
        log.info("Object-Relational Impedance Mismatch knowledge saved to file: {}", outputPath.toAbsolutePath());

        return KnowledgeToolSupport.chunkAndCache(allTextContentString, contentCache);
    }

    private DocumentChunkDTO getORImpedanceMismatchKnowledgeChunk(ChunkDTO param) {
        log.debug("Get Object-Relational Impedance Mismatch knowledge chunk: {}", param);

        return new DocumentChunkDTO(contentCache.chunkAt(param.chunkIndex()));
    }
}
