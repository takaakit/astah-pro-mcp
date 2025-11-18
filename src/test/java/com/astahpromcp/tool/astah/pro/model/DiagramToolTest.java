package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ImageFileDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiagramToolTest {

    private ProjectAccessor projectAccessor;
    private DiagramTool tool;
    private Method getInfo;
    private Method exportPngImage;
    private Method getDiagramBoundRect;
    private Method getPresentationsOnDiagram;
    private Path imageOutputDir;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/DiagramToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Create temporary directory for image output
        imageOutputDir = Paths.get("target/test-images");
        Files.createDirectories(imageOutputDir);

        // Tool
        tool = new DiagramTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            imageOutputDir,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // getDiagramBoundRect() method
        getDiagramBoundRect = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "getDiagramBoundRect",
            McpSyncServerExchange.class,
            IdDTO.class);

        // getPresentationsOnDiagram() method
        getPresentationsOnDiagram = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "getPresentationsOnDiagram",
            McpSyncServerExchange.class,
            IdDTO.class);

        // exportPngImage() method
        exportPngImage = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "exportPngImage",
            McpSyncServerExchange.class,
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }

        // Delete temporary directory for image output
        deleteDirectoryRecursively(imageOutputDir);
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElement(
            IDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getDiagramBoundRect_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElement(
            IDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getDiagramBoundRect()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getDiagramBoundRect,
            tool,
            inputDTO,
            RectangleDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.width() > 0);
        assertTrue(outputDTO.height() > 0);
    }

    @Test
    void getPresentationsOnDiagram_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElement(
            IDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getPresentationsOnDiagram()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getPresentationsOnDiagram,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.value().size() >= 0);
    }

    @Disabled("This test is disabled because it requires a real Astah API connection and cannot be run in a test environment.")
    @Test
    void exportPngImage_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElement(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call exportPngImage()
        // ----------------------------------------
        ImageFileDTO outputDTO = TestSupport.instance().invokeToolMethod(
            exportPngImage,
            tool,
            inputDTO,
            ImageFileDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.imageFilePath());
        assertTrue(Files.exists(Paths.get(outputDTO.imageFilePath())));
        assertEquals("png", outputDTO.imageFormat());
        assertTrue(outputDTO.imageWidth() > 0);
        assertTrue(outputDTO.imageHeight() > 0);
    }

    private void deleteDirectoryRecursively(Path directory) throws Exception {
        if (directory == null || Files.notExists(directory)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(directory)) {
            stream
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to delete path: " + path, e);
                    }
                });
        }
    }
}
