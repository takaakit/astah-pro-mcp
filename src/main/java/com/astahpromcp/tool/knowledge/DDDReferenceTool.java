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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DDDReferenceTool implements ToolProvider {

    private final List<String> contentCache;
    
    private final Path outputDirectory;

    private final String dddReferenceUrl = "https://www.domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf";

    public DDDReferenceTool(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_ddd_reference",
                    "Return the total number of chunks and the data of the first chunk of Domain-Driven Design Reference. If you want to learn the theory of Domain-Driven Design (DDD), use this tool.",
                    this::getDDDReference,
                    NoInputDTO.class,
                    DocumentDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "get_chunk_of_ddd_reference",
                    "Return the chunk data of Domain-Driven Design Reference. If no chunk data exists, an empty string is set.",
                    this::getDDDReferenceChunk,
                    ChunkDTO.class,
                    DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create DDD reference tools", e);
            return List.of();
        }
    }

    private DocumentDTO getDDDReference(NoInputDTO param) throws IOException {
        log.debug("Get DDD reference: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("Domain-Driven Design Reference already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading Domain-Driven Design Reference from PDF URL.");
        try (InputStream is = KnowledgeToolSupport.openUrlStream(dddReferenceUrl)) {
            if (is == null) {
                throw new IOException("Domain-Driven Design Reference PDF resource not found.");
            }

            byte[] bytes = is.readAllBytes();
            String text = KnowledgeToolSupport.convertPdfToMarkdown(bytes, outputDirectory, "ddd_reference_2015_03");

            return KnowledgeToolSupport.chunkAndCache(text, contentCache);

        } catch (IOException e) {
            log.error("Failed to load Domain-Driven Design Reference from URL: {}", dddReferenceUrl, e);
            throw e;
        }
    }

    private DocumentChunkDTO getDDDReferenceChunk(ChunkDTO param) {
        log.debug("Get DDD reference chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }
}
