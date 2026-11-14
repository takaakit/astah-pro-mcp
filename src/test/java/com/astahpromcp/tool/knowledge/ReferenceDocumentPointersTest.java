package com.astahpromcp.tool.knowledge;

import com.astahpromcp.server.ToolCatalogAssembler;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.astah.pro.common.DiagramType;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.common.outputdto.GuideDTO;
import com.astahpromcp.tool.knowledge.document.DocumentType;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ReferenceDocumentPointersTest {
    
    // The tool function whose description lists every document, which is not a pointer
    private static final String DOCUMENT_LIST_TOOL = "get_info_of_document";
    
    // The tool functions whose descriptions point at a document, and the document each one points at
    private static final Map<String, DocumentType> POINTING_TOOLS = Map.of(
            "create_constraint", DocumentType.OCL_SPEC,
            "set_specification_of_constraint", DocumentType.OCL_SPEC,
            "get_all_constraints_and_conditions", DocumentType.OCL_SPEC,
            "generate_dgm_img_from_puml", DocumentType.PUML_GUIDE);
    
    // The diagram guides that point at a document, and the document each one points at
    private static final Map<DiagramType, DocumentType> POINTING_DIAGRAM_GUIDES = Map.of(
            DiagramType.ER, DocumentType.OBJECT_RELATIONAL_IMPEDANCE_MISMATCH);
    
    @TempDir
    Path workspaceDir;
    
    // The project comparisons the tools of the catalog share
    private ProjectDiffJobs projectDiffJobs;
    
    @BeforeEach
    void setUp() {
        projectDiffJobs = new ProjectDiffJobs(workspaceDir.resolve("project-diff"));
    }
    
    @AfterEach
    void tearDown() {
        projectDiffJobs.shutdown();
    }
    
    private ToolCatalog catalog() {
        return ToolCatalogAssembler.build(DiagramThumbnails.INCLUDE, workspaceDir.resolve("images"), workspaceDir, workspaceDir.resolve("document-cache"), projectDiffJobs);
    }
    
    private static String diagramGuide(ToolCatalog catalog, DiagramType diagramType) {
        ToolDefinition definition = catalog.find("dgm_guide");
        String value = JsonSupport.OBJ_MAPPER.convertValue(diagramType, String.class);
        McpSchema.CallToolResult result = definition.toolHandler().apply(null, new McpSchema.CallToolRequest("dgm_guide", Map.of("diagramType", value), null));
        assertFalse(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));
        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), GuideDTO.class).contents();
    }
    
    // How a guide or a tool description points at a document
    private static String pointer(DocumentType type) {
        return "the " + type.title();
    }
    
    @Test
    void description_ok_toolFunctionsPointAtTheDocumentWhereItIsNeeded() {
        ToolCatalog catalog = catalog();
        
        POINTING_TOOLS.forEach((name, type) -> {
            assertTrue(catalog.contains(name), name);
            String description = catalog.find(name).toolSchema().description();
            assertTrue(description.contains(pointer(type)), name + ": " + description);
        });
    }
    
    @Test
    void dgmGuide_ok_diagramGuidesPointAtTheDocumentWhereItIsNeeded() {
        ToolCatalog catalog = catalog();
        
        POINTING_DIAGRAM_GUIDES.forEach((diagramType, type) -> {
            String guide = diagramGuide(catalog, diagramType);
            assertTrue(guide.contains(pointer(type)), diagramType.name() + ": " + guide);
        });
    }
    
    // Pointers are written by hand, so every mention of a document must be one that the tests above check against the document's current title.
    @Test
    void pointers_ok_everyMentionOfADocumentIsCheckedAbove() {
        ToolCatalog catalog = catalog();
        
        Map<String, String> texts = new LinkedHashMap<>();
        for (ToolDefinition definition : catalog.all()) {
            if (!definition.toolSchema().name().equals(DOCUMENT_LIST_TOOL)) {
                texts.put(definition.toolSchema().name(), definition.toolSchema().description());
            }
        }
        for (DiagramType diagramType : DiagramType.values()) {
            texts.put("dgm_guide " + diagramType.name(), diagramGuide(catalog, diagramType));
        }
        
        Set<String> mentioned = new TreeSet<>();
        texts.forEach((where, text) -> {
            for (DocumentType type : DocumentType.values()) {
                if (text != null && text.contains(type.title())) {
                    mentioned.add(where + " -> " + type.value());
                }
            }
        });
        
        Set<String> checked = new TreeSet<>();
        POINTING_TOOLS.forEach((name, type) -> checked.add(name + " -> " + type.value()));
        POINTING_DIAGRAM_GUIDES.forEach((diagramType, type) -> checked.add("dgm_guide " + diagramType.name() + " -> " + type.value()));
        
        assertEquals(checked, mentioned, "every guide and tool description that mentions a document must be listed in POINTING_TOOLS or POINTING_DIAGRAM_GUIDES");
    }
}
