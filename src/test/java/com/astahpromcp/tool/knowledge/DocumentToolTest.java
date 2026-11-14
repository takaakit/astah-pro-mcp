package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.knowledge.document.DocumentSource;
import com.astahpromcp.tool.knowledge.document.DocumentType;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DocumentToolTest {
    
    @TempDir
    Path outputDirectory;
    
    private DocumentTool tool() {
        // No test here fetches a document, so the client is never asked to send anything
        return new DocumentTool(outputDirectory, mock(HttpClient.class));
    }
    
    private static ToolDefinition definition(DocumentTool tool, String name) {
        return tool.createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no tool function named " + name));
    }
    
    private static McpSchema.CallToolResult call(ToolDefinition definition, Map<String, Object> arguments) {
        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(definition.toolSchema().name(), arguments, null));
    }
    
    private static Set<String> allValues() {
        return Arrays.stream(DocumentType.values()).map(DocumentType::value).collect(Collectors.toSet());
    }
    
    @Test
    void createToolDefinitions_ok_registersOneInfoAndOneChunkToolFunction() {
        List<String> names = tool().createToolDefinitions().stream().map(definition -> definition.toolSchema().name()).toList();
        
        assertEquals(List.of("get_info_of_document", "get_chunk_of_document"), names);
    }
    
    @Test
    void createToolDefinitions_ok_describesEveryDocumentAndWhenToReadIt() {
        String description = definition(tool(), "get_info_of_document").toolSchema().description();
        
        for (DocumentType type : DocumentType.values()) {
            assertTrue(description.contains("* '" + type.value() + "': " + type.title() + ". " + type.usage()),
                    "the description does not tell when to read " + type.value() + ": " + description);
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void createToolDefinitions_ok_offersEveryDocumentAsAChoiceOfDocumentType() {
        DocumentTool tool = tool();
        for (String name : List.of("get_info_of_document", "get_chunk_of_document")) {
            Map<String, Object> properties = (Map<String, Object>) definition(tool, name).toolSchema().inputSchema().get("properties");
            Map<String, Object> documentType = (Map<String, Object>) properties.get("documentType");
            
            assertEquals(allValues(), Set.copyOf((List<String>) documentType.get("enum")), name);
        }
    }
    
    @Test
    void getDocumentChunk_ng_asksForTheInfoToolFunctionFirst() {
        McpSchema.CallToolResult result = call(definition(tool(), "get_chunk_of_document"), Map.of("documentType", "ocl_spec", "chunkIndex", 0));
        
        assertTrue(Boolean.TRUE.equals(result.isError()));
        String message = String.valueOf(result.content());
        assertTrue(message.contains("'get_info_of_document' with documentType 'ocl_spec' first"), message);
    }
    
    @Test
    void getDocumentInfo_ng_rejectsADocumentThatDoesNotExist() {
        McpSchema.CallToolResult result = call(definition(tool(), "get_info_of_document"), Map.of("documentType", "no_such_document"));
        
        assertTrue(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));
    }
    
    @Test
    void value_ok_isTheNameJacksonWritesForTheConstant() {
        for (DocumentType type : DocumentType.values()) {
            assertEquals(type.value(), JsonSupport.OBJ_MAPPER.convertValue(type, String.class));
            assertEquals(type, JsonSupport.OBJ_MAPPER.convertValue(type.value(), DocumentType.class));
        }
    }
    
    @Test
    void source_ok_everyListOfWebPagesIsShippedAndNamesAPage() throws Exception {
        for (DocumentType type : DocumentType.values()) {
            if (type.source() instanceof DocumentSource.WebPages webPages) {
                assertFalse(KnowledgeToolSupport.readUrlsFromResource(DocumentTool.class, webPages.urlListResource()).isEmpty(), type.value());
            }
        }
    }
}
