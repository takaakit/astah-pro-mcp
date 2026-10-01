package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.PointDoubleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ImageFileDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationGeometryDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationGeometryDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationGeometriesDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class DiagramToolTest {

    private ProjectAccessor projectAccessor;
    private DiagramTool tool;
    private Method getInfo;
    private Method exportPngImage;
    private Method getDiagramBoundRect;
    private Method getPresentationsOnDiagram;
    private Method getPresentationGeometriesOnDiagram;
    private Path imageOutputDir;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/DiagramToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Create temporary directory for image output
        imageOutputDir = Paths.get("target/test-images");
        Files.createDirectories(imageOutputDir);

        // Tool
        tool = new DiagramTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            new SystemPropertySupport(),
            imageOutputDir);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "getInfo",
            IdDTO.class);

        // getDiagramBoundRect() method
        getDiagramBoundRect = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "getDiagramBoundRect",
            IdDTO.class);

        // getPresentationsOnDiagram() method
        getPresentationsOnDiagram = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "getPresentationsOnDiagram",
            IdDTO.class);

        // getPresentationGeometriesOnDiagram() method
        getPresentationGeometriesOnDiagram = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "getPresentationGeometriesOnDiagram",
            IdDTO.class);

        // exportPngImage() method
        exportPngImage = TestSupport.getAccessibleMethod(
            DiagramTool.class,
            "exportPngImage",
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
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getDiagramBoundRect()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getPresentationsOnDiagram()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getPresentationsOnDiagram,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.value().size() >= 0);
    }

    @Test
    void getPresentationGeometriesOnDiagram_ok_classDiagram() throws Exception {
        // Get diagram
        //   Class Diagram0 holds a frame, the classes Foo and Bar, the package subPackage, and a containment line from subPackage to a class.
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getPresentationGeometriesOnDiagram()
        // ----------------------------------------
        PresentationGeometriesDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getPresentationGeometriesOnDiagram,
            tool,
            inputDTO,
            PresentationGeometriesDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertMatchesAstahPresentations(diagram, outputDTO);

        assertEquals(4, outputDTO.nodes().size());
        assertEquals(1, outputDTO.links().size());

        NodePresentationGeometryDTO foo = findNodeByTypeAndLabel(outputDTO, "Class", "Foo");
        assertEquals("Foo", foo.correspondingModelElement().name());

        NodePresentationGeometryDTO subPackage = findNodeByTypeAndLabel(outputDTO, "Package", "subPackage");
        LinkPresentationGeometryDTO containment = outputDTO.links().get(0);
        assertEquals("Containment", containment.type());
        assertEquals(subPackage.id(), containment.sourceNodeEnd().id());
        assertEquals("Class", containment.targetNodeEnd().type());
        assertTrue(containment.drawnPoints().size() >= 2);
    }

    @Test
    void getPresentationGeometriesOnDiagram_ng_presentationIdGiven() throws Exception {
        // Get presentation
        //   A presentation ID names no diagram, even though the presentation is drawn on one.
        IPresentation presentation = TestSupport.instance().getPresentationByTypeAndLabel("Class", "Foo");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(presentation.getID());

        // ----------------------------------------
        // Call getPresentationGeometriesOnDiagram()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            getPresentationGeometriesOnDiagram,
            tool,
            inputDTO,
            PresentationGeometriesDTO.class));
    }

    @Disabled("This test is disabled because it requires a real Astah API connection and cannot be run in a test environment.")
    @Test
    void exportPngImage_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call exportPngImage()
        // ----------------------------------------
        ImageFileDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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

    // Check that the output lists every presentation on the diagram exactly once, with the geometry the Astah API reports for it.
    private static void assertMatchesAstahPresentations(IDiagram diagram, PresentationGeometriesDTO outputDTO) throws Exception {
        assertEquals(diagram.getId(), outputDTO.diagram().id());
        assertEquals(diagram.getName(), outputDTO.diagram().name());
        assertEquals(toRectangleDTO(diagram.getBoundRect()), outputDTO.diagramRectangle());

        Map<String, INodePresentation> astahNodes = new HashMap<>();
        Map<String, ILinkPresentation> astahLinks = new HashMap<>();
        for (IPresentation presentation : diagram.getPresentations()) {
            if (presentation instanceof INodePresentation astahNode) {
                astahNodes.put(astahNode.getID(), astahNode);
            } else if (presentation instanceof ILinkPresentation astahLink) {
                astahLinks.put(astahLink.getID(), astahLink);
            }
        }
        assertFalse(astahNodes.isEmpty());
        assertFalse(astahLinks.isEmpty());
        assertEquals(astahNodes.size(), outputDTO.nodes().size());
        assertEquals(astahLinks.size(), outputDTO.links().size());

        // Removing each matched presentation makes a duplicated entry fail as well as an unknown one.
        for (NodePresentationGeometryDTO node : outputDTO.nodes()) {
            INodePresentation astahNode = astahNodes.remove(node.id());
            assertNotNull(astahNode, "unexpected node presentation: " + node.id());
            assertEquals(astahNode.getType(), node.type());
            assertEquals(toRectangleDTO(astahNode.getRectangle()), node.drawnRectangle());
        }

        for (LinkPresentationGeometryDTO link : outputDTO.links()) {
            ILinkPresentation astahLink = astahLinks.remove(link.id());
            assertNotNull(astahLink, "unexpected link presentation: " + link.id());
            assertEquals(astahLink.getType(), link.type());
            assertEquals(astahLink.getSourceEnd().getID(), link.sourceNodeEnd().id());
            assertEquals(astahLink.getTargetEnd().getID(), link.targetNodeEnd().id());
            assertEquals(toPointDoubleDTOs(astahLink.getPoints()), link.drawnPoints());
        }
    }

    private static NodePresentationGeometryDTO findNodeByTypeAndLabel(PresentationGeometriesDTO outputDTO, String type, String label) {
        List<NodePresentationGeometryDTO> nodes = outputDTO.nodes().stream()
            .filter(node -> node.type().equals(type) && node.label().equals(label))
            .toList();
        assertEquals(1, nodes.size(), "node presentations of type " + type + " labeled " + label);
        return nodes.get(0);
    }

    private static RectangleDTO toRectangleDTO(Rectangle2D rectangle) {
        return new RectangleDTO(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    private static List<PointDoubleDTO> toPointDoubleDTOs(Point2D[] points) {
        List<PointDoubleDTO> pointDTOs = new ArrayList<>();
        for (Point2D point : points) {
            pointDTOs.add(new PointDoubleDTO(point.getX(), point.getY()));
        }
        return pointDTOs;
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
