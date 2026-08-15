package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.ExportedImage;
import com.astahpromcp.tool.astah.pro.image.SvgOverlaySupport.ComposedLayout;
import com.astahpromcp.tool.common.ImageConvertSupport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvgOverlaySupportTest {

    @Test
    void layoutOf_ok_anchorsBothLayersToTheUnionTopLeft() {
        // The diagram bound rectangle starts at (-20, 10) and the SVG at (300, -40), so the union top-left is (-20, -40) and neither layer may be placed at a negative offset.
        ComposedLayout layout = SvgOverlaySupport.layoutOf(
                new Rectangle2D.Double(-20, 10, 800, 600), 800, 600,
                new Rectangle2D.Double(300, -40, 200, 150), 200, 150);

        assertEquals(0, layout.diagramX());
        assertEquals(50, layout.diagramY());
        assertEquals(320, layout.svgX());
        assertEquals(0, layout.svgY());
        assertEquals(800, layout.canvasWidth());
        assertEquals(650, layout.canvasHeight());
    }

    @Test
    void layoutOf_ok_doesNotAlignTheTopLeftCornersOfTheTwoLayers() {
        // Regression guard: naively stacking both layers at (0, 0) would misplace the SVG by the diagram bound rectangle origin.
        ComposedLayout layout = SvgOverlaySupport.layoutOf(
                new Rectangle2D.Double(100, 200, 400, 300), 400, 300,
                new Rectangle2D.Double(100, 200, 50, 50), 50, 50);

        assertEquals(0, layout.diagramX());
        assertEquals(0, layout.diagramY());

        // The SVG sits at the same diagram coordinates as the bound rectangle origin, which is the diagram image's pixel (0, 0) - not diagram coordinates (0, 0).
        assertEquals(0, layout.svgX());
        assertEquals(0, layout.svgY());
    }

    @Test
    void layoutOf_ok_growsTheCanvasWhenTheSvgOverflowsTheDiagram() {
        // The SVG extends past the diagram's right and bottom edges, so the canvas must contain both.
        ComposedLayout layout = SvgOverlaySupport.layoutOf(
                new Rectangle2D.Double(0, 0, 400, 300), 400, 300,
                new Rectangle2D.Double(350, 250, 200, 200), 200, 200);

        assertEquals(0, layout.diagramX());
        assertEquals(0, layout.diagramY());
        assertEquals(350, layout.svgX());
        assertEquals(250, layout.svgY());
        assertEquals(550, layout.canvasWidth());
        assertEquals(450, layout.canvasHeight());
    }

    @Test
    void layoutOf_ok_scalesOffsetsByTheDiagramImagePixelDensity() {
        // The exported image is twice the size of the bound rectangle, so one diagram unit is two pixels.
        ComposedLayout layout = SvgOverlaySupport.layoutOf(
                new Rectangle2D.Double(0, 0, 400, 300), 800, 600,
                new Rectangle2D.Double(100, 50, 200, 150), 400, 300);

        assertEquals(0, layout.diagramX());
        assertEquals(0, layout.diagramY());
        assertEquals(200, layout.svgX());
        assertEquals(100, layout.svgY());
        assertEquals(800, layout.canvasWidth());
        assertEquals(600, layout.canvasHeight());
    }

    @Test
    void layoutOf_ok_fallsBackToOneToOneForAnEmptyBoundRectangle() {
        // A diagram holding no presentations reports an empty bound rectangle; the offsets must stay finite.
        ComposedLayout layout = SvgOverlaySupport.layoutOf(
                new Rectangle2D.Double(0, 0, 0, 0), 1, 1,
                new Rectangle2D.Double(40, 30, 100, 100), 100, 100);

        assertEquals(0, layout.diagramX());
        assertEquals(0, layout.diagramY());
        assertEquals(40, layout.svgX());
        assertEquals(30, layout.svgY());
        assertEquals(140, layout.canvasWidth());
        assertEquals(130, layout.canvasHeight());
    }

    @Test
    void layoutOf_ok_clampsAbsurdOffsetsInsteadOfOverflowing() {
        // A viewBox origin far beyond the diagram must not wrap the int offsets around into negative values.
        ComposedLayout layout = SvgOverlaySupport.layoutOf(
                new Rectangle2D.Double(0, 0, 400, 300), 400, 300,
                new Rectangle2D.Double(1e12, 1e12, 100, 100), 100, 100);

        assertTrue(layout.svgX() > 0, "The clamped offset must stay positive, but was " + layout.svgX());
        assertTrue(layout.svgY() > 0, "The clamped offset must stay positive, but was " + layout.svgY());
        assertTrue(layout.canvasWidth() > 0, "The canvas width must stay positive, but was " + layout.canvasWidth());
        assertTrue(layout.canvasHeight() > 0, "The canvas height must stay positive, but was " + layout.canvasHeight());
    }

    @Test
    void layoutOf_ok_containsBothLayersDespiteRounding() {
        // Fractional bounds make the offsets round independently; the canvas must still contain both layers.
        Rectangle2D diagramRect = new Rectangle2D.Double(-13.7, 4.2, 333.3, 221.9);
        Rectangle2D svgRect = new Rectangle2D.Double(150.4, -60.8, 99.5, 77.1);

        ComposedLayout layout = SvgOverlaySupport.layoutOf(diagramRect, 501, 333, svgRect, 150, 116);

        assertTrue(layout.diagramX() >= 0 && layout.diagramY() >= 0);
        assertTrue(layout.svgX() >= 0 && layout.svgY() >= 0);
        assertTrue(layout.diagramX() + 501 <= layout.canvasWidth());
        assertTrue(layout.diagramY() + 333 <= layout.canvasHeight());
        assertTrue(layout.svgX() + 150 <= layout.canvasWidth());
        assertTrue(layout.svgY() + 116 <= layout.canvasHeight());
    }

    @Test
    void createSvgOverlayContents_ok_rasterizesALargeSvgAtTheFullDiagramDensity() throws Exception {
        BufferedImage diagramImage = whiteImage(2000, 1000);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.exportDiagramImage("dgm"))
            .thenReturn(new ExportedImage(diagramImage, "dgm.png", new Rectangle2D.Double(0, 0, 1000, 500)));

        List<McpSchema.Content> contents = newSvgOverlaySupport(imageCaptureSupport)
                .createSvgOverlayContents("dgm", wideSvg(2500, 80));

        assertEquals(1, contents.size(), "A diagram with content needs no explanatory text");
        BufferedImage composed = decodeOnlyImage(contents);

        // Union in diagram units is 2500 x 500, at two pixels per unit: 5000 x 1000, a ratio of 5.
        double ratio = composed.getWidth() / (double) composed.getHeight();
        assertEquals(5.0, ratio, 0.05,
                "The SVG must be rasterized at the diagram density, but the composed image was " + composed.getWidth() + "x" + composed.getHeight());
    }

    @Test
    void createSvgOverlayContents_ng_rejectsAnSvgPlacedFarOutsideTheDiagram() throws Exception {
        // The canvas is allocated before the SVG is rasterized, so an outsized composition is refused up front instead of building a huge raster first.
        SvgOverlaySupport svgOverlaySupport = newSvgOverlaySupport(
                mockedExport(new Rectangle2D.Double(0, 0, 200, 200), 200, 200));

        assertThrows(IllegalArgumentException.class,
                () -> svgOverlaySupport.createSvgOverlayContents("dgm", svgAt(500_000, 500_000, 100, 100)));
    }

    @Test
    void createSvgOverlayContents_ok_explainsThatADiagramWithoutContentCoversNoArea() throws Exception {
        List<McpSchema.Content> contents = newSvgOverlaySupport(mockedExport(new Rectangle2D.Double(0, 0, 0, 0), 1, 1))
                .createSvgOverlayContents("dgm", svgAt(100, 100, 100, 100));

        assertEquals(2, contents.size(), "An empty diagram must be explained alongside the image");
        McpSchema.TextContent note = assertInstanceOf(McpSchema.TextContent.class, contents.get(0));
        assertTrue(note.text().contains("nothing drawn on it"), "Unexpected note: " + note.text());

        // Standalone rendering uses the export resolution, which is 96 dpi over the 72 dpi diagram coordinates here.
        BufferedImage composed = decodeOnlyImage(contents);
        assertEquals(133, composed.getWidth());
        assertEquals(133, composed.getHeight());
    }

    @Test
    void createSvgOverlayContents_ok_treatsASingleDegenerateAxisAsAnEmptyDiagram() throws Exception {
        // Only the height collapses, which still leaves no usable density on that axis.
        List<McpSchema.Content> contents = newSvgOverlaySupport(mockedExport(new Rectangle2D.Double(0, 0, 400, 0), 533, 1))
                .createSvgOverlayContents("dgm", svgAt(100, 100, 100, 100));

        assertEquals(2, contents.size(), "A degenerate axis must be handled like an empty diagram");
        assertInstanceOf(McpSchema.TextContent.class, contents.get(0));
    }

    @Test
    void createSvgOverlayContents_ok_rendersTheStandaloneSvgAtTheExportResolution() throws Exception {
        List<McpSchema.Content> contents = newSvgOverlaySupport(mock(ImageCaptureSupport.class))
                .createSvgOverlayContents("", svgAt(0, 0, 300, 200));

        assertEquals(1, contents.size(), "An explicit standalone request needs no explanatory text");
        BufferedImage composed = decodeOnlyImage(contents);
        assertEquals(400, composed.getWidth());
        assertEquals(267, composed.getHeight());
    }

    private static SvgOverlaySupport newSvgOverlaySupport(ImageCaptureSupport imageCaptureSupport) {
        return new SvgOverlaySupport(imageCaptureSupport, new ImageConvertSupport(), new SystemPropertySupport());
    }

    private static ImageCaptureSupport mockedExport(Rectangle2D boundRect, int imageWidth, int imageHeight) throws Exception {
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.exportDiagramImage("dgm"))
            .thenReturn(new ExportedImage(whiteImage(imageWidth, imageHeight), "dgm.png", boundRect));
        return imageCaptureSupport;
    }

    private static BufferedImage decodeOnlyImage(List<McpSchema.Content> contents) throws Exception {
        McpSchema.ImageContent image = assertInstanceOf(
                McpSchema.ImageContent.class, contents.get(contents.size() - 1));
        assertEquals("image/png", image.mimeType());

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(image.data())));
        assertNotNull(decoded, "The returned bytes must be a readable PNG");
        return decoded;
    }

    // A white sheet stands in for an exported diagram image and compresses well enough to avoid a downscale.
    private static BufferedImage whiteImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    // An SVG anchored at diagram coordinates (0, 0) through its viewBox.
    private static String wideSvg(int width, int height) {
        return svgAt(0, 0, width, height);
    }

    private static String svgAt(int x, int y, int width, int height) {
        return String.format(
            "<svg width=\"%d\" height=\"%d\" viewBox=\"%d %d %d %d\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"red\"/></svg>",
            width, height, x, y, width, height, x, y, width, height);
    }
}
