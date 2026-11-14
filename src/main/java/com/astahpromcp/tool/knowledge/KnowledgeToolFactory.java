package com.astahpromcp.tool.knowledge;

import com.astahpromcp.info.McpServerInfo;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.knowledge.document.DocumentSource;
import com.astahpromcp.tool.knowledge.document.DocumentType;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

// Factory for creating knowledge tools
@Slf4j
public class KnowledgeToolFactory {
    
    private final Path workspaceDir;
    private final Path documentCacheDir;
    private final HttpClient httpClient;
    
    // The documents converted from PDF are kept in documentCacheDir across sessions; everything else goes to workspaceDir
    public KnowledgeToolFactory(Path workspaceDir, Path documentCacheDir) {
        this(workspaceDir, documentCacheDir, KnowledgeToolSupport.newHttpClient());
    }
    
    public KnowledgeToolFactory(Path workspaceDir, Path documentCacheDir, HttpClient httpClient) {
        this.workspaceDir = workspaceDir;
        this.documentCacheDir = documentCacheDir;
        this.httpClient = httpClient;
    }
    
    public List<ToolProvider> createToolProviders() {
        try {
            return List.of(
                    new UMLModelingArchitectureInsightsTool(workspaceDir),
                    new ArchitecturalDesignSmellsTool(workspaceDir),
                    new DocumentTool(workspaceDir, httpClient, openDocumentCache())
            );
        } catch (Exception e) {
            log.warn("Error creating knowledge tools", e);
            return List.of();
        }
    }
    
    // Opening the cache deletes what other builds left in it
    private ConvertedDocumentCache openDocumentCache() {
        return ConvertedDocumentCache.open(documentCacheDir, McpServerInfo.buildId(), pdfDocuments(), ConvertedDocumentCache::isProcessAlive);
    }
    
    private static List<DocumentSource.Pdf> pdfDocuments() {
        return Arrays.stream(DocumentType.values())
                .map(DocumentType::source)
                .filter(DocumentSource.Pdf.class::isInstance)
                .map(DocumentSource.Pdf.class::cast)
                .toList();
    }
}
