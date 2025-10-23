package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentChunkDTO;
import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DDDReferenceTool implements ToolProvider {

    private static final int CHUNK_SIZE = 51200; // characters 50KB (50 * 1024)
    private final List<String> contentCache;
    
    private final Path outputDirectory;
    private final ProjectAccessor projectAccessor;

    private final String dddReferenceUrl = "https://www.domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf";

    public DDDReferenceTool(Path outputDirectory, ProjectAccessor projectAccessor) {
        this.outputDirectory = outputDirectory;
        this.projectAccessor = projectAccessor;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            // Check if the DDD Reference URL is accessible
            try {
                URI.create(dddReferenceUrl).toURL().openConnection().connect();
            } catch (Exception e) {
                log.error("DDD Reference URL is not accessible: " + dddReferenceUrl, e);
                return List.of();
            }
    
            return List.of(
                    ToolSupport.definition(
                            "get_info_of_ddd_ref",
                            "Return the total number of chunks and the data of the first chunk of Domain-Driven Design Reference. If you want to learn the theory of Domain-Driven Design (DDD), use this tool.",
                            this::getDDDReference,
                            NoInputDTO.class,
                            DocumentDTO.class),

                    ToolSupport.definition(
                            "get_chunk_of_ddd_ref",
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

    private DocumentDTO getDDDReference(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get DDD reference: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("Domain-Driven Design Reference already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading Domain-Driven Design Reference from PDF URL.");
        
        try (InputStream is = URI.create(dddReferenceUrl).toURL().openStream()) {
            if (is == null) {
                throw new IOException("Domain-Driven Design Reference PDF resource not found.");
            }

            String text;
            byte[] bytes = is.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                text = new PDFTextStripper().getText(document);
            }

            // Replace multiple spaces with a single space
            text = text.replaceAll("\t\r", " ");
            text = text.replaceAll(" {2,}", " ");

            // Write the extracted text to a file
            String textFileName = "ddd_reference_2015_03.txt";
            Path outputPath = outputDirectory.resolve(textFileName);
            Files.write(outputPath, text.getBytes(StandardCharsets.UTF_8));
            log.info("Domain-Driven Design Reference text saved to file: {}", outputPath.toAbsolutePath());

            List<String> chunks = splitText(text, CHUNK_SIZE);
            if (chunks.isEmpty()) {
                chunks.add(""); // Ensure there is at least one empty chunk
            }

            contentCache.clear();
            contentCache.addAll(chunks);

            return new DocumentDTO(chunks.size(), chunks.get(0));
        }
    }

    private DocumentChunkDTO getDDDReferenceChunk(McpSyncServerExchange exchange, ChunkDTO param) {
        log.debug("Get DDD reference chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }

    private List<String> splitText(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }
        
        return chunks;
    }
}
