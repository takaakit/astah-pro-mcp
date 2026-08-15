package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.ExportedImage;
import com.astahpromcp.tool.astah.pro.image.inputdto.SvgOverlayOnDiagramDTO;
import com.astahpromcp.tool.common.ImageConvertSupport;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvgOverlayToolTest {

    // The SVG under test: a 100x100 rectangle placed at diagram coordinates (100, 100).
    private static final int SVG_X = 100;
    private static final int SVG_Y = 100;
    private static final int SVG_WIDTH = 100;
    private static final int SVG_HEIGHT = 100;

    private static final double STUB_IMAGE_EXPORT_DPI = 96.0;
    private static final double DIAGRAM_COORDINATE_DPI = 72.0;
    private static final double DIAGRAM_EXPORT_SCALE = STUB_IMAGE_EXPORT_DPI / DIAGRAM_COORDINATE_DPI;

    // The expected size is derived in diagram units and then converted to pixels, so each edge of the composed canvas may round independently by up to a pixel.
    private static final int SIZE_TOLERANCE_PIXELS = 2;

    // Same budget as ImageCaptureSupport.PngSizeTarget.LARGE.
    private static final long MAX_PNG_BYTES = 50L * 1024;

    private ProjectAccessor projectAccessor;
    private ImageCaptureSupport imageCaptureSupport;
    private SvgOverlayTool tool;
    private Method overlaySvgImageOnDiagramImage;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();

        // The tool under test never modifies the project, so open without taking the lock and fall back to
        // read-only. That keeps the test runnable while the same file is open in a running Astah.
        projectAccessor.open("src/test/resources/modelfile/image/SvgOverlayToolTest.asta", false, false, true);

        // IDiagram.exportImage() is only available inside the Astah plugin environment, so the diagram image is
        // stubbed. Everything downstream of it - the SVG rasterization, the placement and the PNG encoding - is the
        // real implementation, driven by the bound rectangles of the real diagrams in the project.
        imageCaptureSupport = mock(ImageCaptureSupport.class);
        SvgOverlaySupport svgOverlaySupport = new SvgOverlaySupport(
                imageCaptureSupport, new ImageConvertSupport(), new SystemPropertySupport());

        // Tool
        tool = new SvgOverlayTool(svgOverlaySupport);

        // overlaySvgImageOnDiagramImage() method
        overlaySvgImageOnDiagramImage = TestSupport.getAccessibleMethod(
            SvgOverlayTool.class,
            "overlaySvgImageOnDiagramImage",
            SvgOverlayOnDiagramDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void overlaySvgImageOnDiagramImage_ok_sizesThePngToTheUnionOfTheDiagramAndTheSvgOnEveryDiagram() throws Exception {
        List<IDiagram> diagrams = allDiagrams();
        assertFalse(diagrams.isEmpty(), "The test project must contain at least one diagram");

        // Every diagram in the test project has a different bound rectangle origin - zero, positive, negative and
        // fractional on both axes - so running them all exercises the offset that keeps the SVG from being simply
        // top-left aligned with the diagram image. Collect the assertions so one failing diagram cannot hide the rest.
        List<Executable> assertions = new ArrayList<>();
        for (IDiagram diagram : diagrams) {
            assertions.add(() -> assertComposedImageSize(diagram));
        }

        assertAll(assertions);
    }

    private void assertComposedImageSize(IDiagram diagram) throws Exception {
        String diagramName = diagram.getName();
        Rectangle2D boundRect = diagram.getBoundRect();

        // The exported diagram image spans the bound rectangle, which is what fixes the pixel density.
        int diagramPixelWidth = (int) Math.floor(boundRect.getWidth() * DIAGRAM_EXPORT_SCALE);
        int diagramPixelHeight = (int) Math.floor(boundRect.getHeight() * DIAGRAM_EXPORT_SCALE);
        BufferedImage diagramImage = framedImage(diagramPixelWidth, diagramPixelHeight);
        when(imageCaptureSupport.exportDiagramImage(diagram.getId()))
            .thenReturn(new ExportedImage(diagramImage, diagram.getId() + ".png", boundRect));

        double pixelsPerUnitX = diagramPixelWidth / boundRect.getWidth();
        double pixelsPerUnitY = diagramPixelHeight / boundRect.getHeight();

        // The composed image must span the union of the diagram and the SVG, measured in diagram coordinates.
        double unionWidth = Math.max(boundRect.getMaxX(), SVG_X + SVG_WIDTH) - Math.min(boundRect.getX(), SVG_X);
        double unionHeight = Math.max(boundRect.getMaxY(), SVG_Y + SVG_HEIGHT) - Math.min(boundRect.getY(), SVG_Y);
        int expectedWidth = (int) Math.round(unionWidth * pixelsPerUnitX);
        int expectedHeight = (int) Math.round(unionHeight * pixelsPerUnitY);

        byte[] pngBytes = invokeOverlay(diagram.getId());

        assertTrue(pngBytes.length <= MAX_PNG_BYTES, () -> String.format(
            "The PNG of diagram '%s' must fit within %d bytes, but was %d bytes",
            diagramName, MAX_PNG_BYTES, pngBytes.length));

        BufferedImage composed = ImageIO.read(new ByteArrayInputStream(pngBytes));
        assertNotNull(composed, () -> "The returned bytes of diagram '" + diagramName + "' must be a readable PNG");

        // These images compress far below the size budget, so no downscaling happens and the composed image must
        // come back at the full union size. A downscale would show up here as a size mismatch.
        assertSizeWithinTolerance(expectedWidth, composed.getWidth(), diagramName, "width");
        assertSizeWithinTolerance(expectedHeight, composed.getHeight(), diagramName, "height");

        // The composed image must still contain the whole diagram image, whichever side the SVG extends it on.
        assertTrue(composed.getWidth() >= diagramPixelWidth, () -> String.format(
            "The PNG of diagram '%s' must be at least as wide as the diagram image (%d px), but was %d px",
            diagramName, diagramPixelWidth, composed.getWidth()));
        assertTrue(composed.getHeight() >= diagramPixelHeight, () -> String.format(
            "The PNG of diagram '%s' must be at least as tall as the diagram image (%d px), but was %d px",
            diagramName, diagramPixelHeight, composed.getHeight()));
    }

    private byte[] invokeOverlay(String diagramId) throws Exception {
        List<McpSchema.Content> contents = TestSupport.instance().invokeToolMethodReturningContents(
            overlaySvgImageOnDiagramImage,
            tool,
            new SvgOverlayOnDiagramDTO(diagramId, rectangleSvg()));

        assertEquals(1, contents.size(), "The tool must return exactly one content");
        McpSchema.ImageContent image = assertInstanceOf(McpSchema.ImageContent.class, contents.get(0));
        assertEquals("image/png", image.mimeType());

        return Base64.getDecoder().decode(image.data());
    }

    private static void assertSizeWithinTolerance(int expected, int actual, String diagramName, String dimension) {
        assertTrue(Math.abs(expected - actual) <= SIZE_TOLERANCE_PIXELS, () -> String.format(
            "The PNG %s of diagram '%s' must be %d px (+/- %d), but was %d px",
            dimension, diagramName, expected, SIZE_TOLERANCE_PIXELS, actual));
    }

    // A 100x100 rectangle whose viewBox min-x/min-y place it at diagram coordinates (100, 100).
    private static String rectangleSvg() {
        return String.format(
            "<svg width=\"%d\" height=\"%d\" viewBox=\"%d %d %d %d\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"red\" fill-opacity=\"0.35\"/>"
                + "</svg>",
            SVG_WIDTH, SVG_HEIGHT, SVG_X, SVG_Y, SVG_WIDTH, SVG_HEIGHT,
            SVG_X, SVG_Y, SVG_WIDTH, SVG_HEIGHT);
    }

    // Stands in for an exported diagram image: a white sheet with a border, which compresses well enough that the
    // composed PNG stays inside the size budget and is therefore never downscaled.
    private static BufferedImage framedImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(0, 0, width - 1, height - 1);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static List<IDiagram> allDiagrams() {
        return TestSupport.instance().getNamedElementsByClass(IDiagram.class).stream()
                .map(IDiagram.class::cast)
                .toList();
    }
}
