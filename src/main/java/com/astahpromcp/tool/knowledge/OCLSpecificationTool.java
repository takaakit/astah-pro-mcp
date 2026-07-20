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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class OCLSpecificationTool implements ToolProvider {

    private final List<String> contentCache;

    private final Path outputDirectory;

    private final String oclSpecificationUrl = "https://www.omg.org/spec/OCL/2.4/PDF";

    public OCLSpecificationTool(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_ocl_spec",
                    "Return the total number of chunks and the data of the first chunk of OCL (Object Constraint Language) Specification. If you want to learn the theory of OCL, use this tool.",
                    this::getOCLSpecification,
                    NoInputDTO.class,
                    DocumentDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "get_chunk_of_ocl_spec",
                    "Return the chunk data of OCL (Object Constraint Language) Specification. If no chunk data exists, an empty string is set.",
                    this::getOCLSpecificationChunk,
                    ChunkDTO.class,
                    DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create OCL specification tools", e);
            return List.of();
        }
    }

    private DocumentDTO getOCLSpecification(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get OCL specification: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("OCL Specification already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading OCL Specification from PDF URL.");
        try (InputStream is = KnowledgeToolSupport.openUrlStream(oclSpecificationUrl)) {
            if (is == null) {
                throw new IOException("OCL Specification PDF resource not found.");
            }

            byte[] bytes = is.readAllBytes();
            String text = KnowledgeToolSupport.convertPdfToMarkdown(bytes, outputDirectory, "ocl_specification_2_4");

            return KnowledgeToolSupport.chunkAndCache(text, contentCache);

        } catch (IOException e) {
            log.error("Failed to load OCL Specification from URL: {}", oclSpecificationUrl, e);
            throw e;
        }
    }

    private DocumentChunkDTO getOCLSpecificationChunk(McpSyncServerExchange exchange, ChunkDTO param) {
        log.debug("Get OCL specification chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }
}
