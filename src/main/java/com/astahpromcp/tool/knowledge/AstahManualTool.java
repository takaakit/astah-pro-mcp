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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class AstahManualTool implements ToolProvider {

    private static final int CHUNK_SIZE = 51200; // characters 50KB (50 * 1024)
    private final List<String> contentCache;
    
    private final Path outputDirectory;
    private final ProjectAccessor projectAccessor;

    private final String pdfFilePath;

    public AstahManualTool(Path outputDirectory, ProjectAccessor projectAccessor) {
        this.outputDirectory = outputDirectory;
        this.projectAccessor = projectAccessor;
        this.contentCache = new CopyOnWriteArrayList<>();
        this.pdfFilePath = projectAccessor.getAstahInstallPath() + "ReferenceManual-astah-UML_professional.pdf";
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            // Normalize the path via File to stay cross-platform friendly
            Path pdfPath = new File(pdfFilePath).toPath();
            
            if (!Files.exists(pdfPath)) {
                log.error("Astah Professional Reference Manual PDF file not found: " + pdfPath);
                return List.of();
            }
    
            return List.of(
                    ToolSupport.definition(
                            "get_info_of_astah_man",
                            "Return the total number of chunks and the data of the first chunk of Astah Professional Reference Manual.",
                            this::getAstahManual,
                            NoInputDTO.class,
                            DocumentDTO.class),

                    ToolSupport.definition(
                            "get_chunk_of_astah_man",
                            "Return the chunk data of Astah Professional Reference Manual. If no chunk data exists, an empty string is set.",
                            this::getAstahManualChunk,
                            ChunkDTO.class,
                            DocumentChunkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create astah manual tools", e);
            return List.of();
        }
    }

    private DocumentDTO getAstahManual(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get astah manual: {}", param);

        if (!contentCache.isEmpty()) {
            log.info("Astah Professional Reference Manual already loaded, returning from cache.");
            return new DocumentDTO(contentCache.size(), contentCache.get(0));
        }

        log.info("Loading Astah Professional Reference Manual from PDF resource.");
        // Normalize the path via File to stay cross-platform friendly
        Path pdfPath = new File(pdfFilePath).toPath();
        
        try (InputStream is = Files.newInputStream(pdfPath)) {
            if (is == null) {
                throw new IOException("Astah Professional Reference Manual PDF resource not found.");
            }

            String text;
            byte[] bytes = is.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                text = new PDFTextStripper().getText(document);
            }

            // Write the extracted text to a file
            String textFileName = "astah_professional_reference_manual.txt";
            Path outputPath = outputDirectory.resolve(textFileName);
            Files.write(outputPath, text.getBytes(StandardCharsets.UTF_8));
            log.info("Astah Professional Reference Manual text saved to file: {}", outputPath.toAbsolutePath());

            List<String> chunks = splitText(text, CHUNK_SIZE);
            if (chunks.isEmpty()) {
                chunks.add(""); // Ensure there is at least one empty chunk
            }

            contentCache.clear();
            contentCache.addAll(chunks);

            return new DocumentDTO(chunks.size(), chunks.get(0));
        }
    }

    private DocumentChunkDTO getAstahManualChunk(McpSyncServerExchange exchange, ChunkDTO param) {
        log.debug("Get astah manual chunk: {}", param);

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
