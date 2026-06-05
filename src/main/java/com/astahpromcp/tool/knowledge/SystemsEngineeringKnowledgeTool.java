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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class SystemsEngineeringKnowledgeTool implements ToolProvider {

    private static final int CHUNK_SIZE = 51200; // characters 50KB (50 * 1024)
    private final List<String> contentCache;

    private final Path outputDirectory;
    private final ProjectAccessor projectAccessor;

    private final String systemsEngineeringHandbookUrl = "https://www.nasa.gov/wp-content/uploads/2018/09/nasa_systems_engineering_handbook_0.pdf";

    public SystemsEngineeringKnowledgeTool(Path outputDirectory, ProjectAccessor projectAccessor) {
        this.outputDirectory = outputDirectory;
        this.projectAccessor = projectAccessor;
        this.contentCache = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_systems_engineering",
                    "Return the total number of chunks and the data of the first chunk of NASA Systems Engineering Handbook. If you want to learn the theory of Systems Engineering, use this tool.",
                    this::getSystemsEngineeringKnowledge,
                    NoInputDTO.class,
                    DocumentDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "get_chunk_of_systems_engineering",
                    "Return the chunk data of NASA Systems Engineering Handbook. If no chunk data exists, an empty string is set.",
                    this::getSystemsEngineeringKnowledgeChunk,
                    ChunkDTO.class,
                    DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create Systems Engineering knowledge tools", e);
            return List.of();
        }
    }

    private DocumentDTO getSystemsEngineeringKnowledge(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get Systems Engineering knowledge: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("NASA Systems Engineering Handbook already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading NASA Systems Engineering Handbook from PDF URL.");
        try (InputStream is = URI.create(systemsEngineeringHandbookUrl).toURL().openStream()) {
            if (is == null) {
                throw new IOException("NASA Systems Engineering Handbook PDF resource not found.");
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
            String textFileName = "nasa_systems_engineering_handbook.txt";
            Path outputPath = outputDirectory.resolve(textFileName);
            Files.write(outputPath, text.getBytes(StandardCharsets.UTF_8));
            log.info("NASA Systems Engineering Handbook text saved to file: {}", outputPath.toAbsolutePath());

            List<String> chunks = KnowledgeToolSupport.splitText(text, CHUNK_SIZE);
            if (chunks.isEmpty()) {
                chunks.add(""); // Ensure there is at least one empty chunk
            }

            contentCache.clear();
            contentCache.addAll(chunks);

            return new DocumentDTO(chunks.size(), chunks.get(0));

        } catch (IOException e) {
            log.error("Failed to load NASA Systems Engineering Handbook from URL: {}", systemsEngineeringHandbookUrl, e);
            throw e;
        }
    }

    private DocumentChunkDTO getSystemsEngineeringKnowledgeChunk(McpSyncServerExchange exchange, ChunkDTO param) {
        log.debug("Get Systems Engineering knowledge chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        if (chunkIndex < 0 || chunkIndex >= contentCache.size()) {
            throw new IllegalArgumentException("Invalid chunk index: " + chunkIndex);
        }

        return new DocumentChunkDTO(contentCache.get(chunkIndex));
    }
}
