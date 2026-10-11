package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.EncodedPng;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.ExportedImage;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.PngSizeTarget;
import com.astahpromcp.tool.common.ImageConvertSupport;
import com.astahpromcp.tool.common.ImageConvertSupport.ParsedSvg;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.List;

@Slf4j
public class SvgOverlaySupport {

    // Guards against an SVG viewBox placed so far from the diagram that the composed canvas exhausts memory.
    private static final long MAX_COMPOSED_PIXELS = McpServerConfig.SVG_MAX_RASTER_PIXELS;

    // Keeps an absurd viewBox origin from overflowing the int pixel offsets before the canvas guard can reject it.
    private static final long MAX_PIXEL_OFFSET = 1_000_000L;

    // Astah's diagram coordinates are points
    private static final double DIAGRAM_COORDINATE_DPI = 72.0;

    private final ImageCaptureSupport imageCaptureSupport;
    private final ImageConvertSupport imageConvertSupport;
    private final SystemPropertySupport systemPropertySupport;

    public SvgOverlaySupport(ImageCaptureSupport imageCaptureSupport, ImageConvertSupport imageConvertSupport, SystemPropertySupport systemPropertySupport) {
        this.imageCaptureSupport = imageCaptureSupport;
        this.imageConvertSupport = imageConvertSupport;
        this.systemPropertySupport = systemPropertySupport;
    }

    public List<McpSchema.Content> createSvgOverlayContents(String diagramId, String imageSvgCode) throws Exception {
        log.debug("Compose SVG overlay on diagram image as PNG: {}", diagramId);

        // Parsed and measured before the diagram is exported, and before any pixels are allocated for the SVG.
        ParsedSvg svg = imageConvertSupport.parseSvg(imageSvgCode);

        if (diagramId == null || diagramId.isBlank()) {
            return List.of(encodeAsPng(renderSvgOnly(svg)));
        }

        ExportedImage exported = imageCaptureSupport.exportDiagramImage(diagramId);
        Rectangle2D boundRect = exported.boundRect();

        // A diagram with no coordinate extent contributes no pixels, and offers no density to measure either.
        if (boundRect.getWidth() <= 0 || boundRect.getHeight() <= 0) {
            log.info("Diagram {} has an empty bound rectangle ({} x {}), rendering the SVG image on its own",
                    diagramId, boundRect.getWidth(), boundRect.getHeight());

            return List.of(
                McpSchema.TextContent.builder(
                    "The specified diagram has nothing drawn on it, so it covers no area and the SVG image is shown on its own. The image is still rendered at the resolution diagram images are exported at, so it appears at the same size it would over a diagram that does have content.").build(),
                encodeAsPng(renderSvgOnly(svg)));
        }

        return List.of(encodeAsPng(composeOverDiagram(exported, svg)));
    }

    private McpSchema.ImageContent encodeAsPng(BufferedImage composed) throws Exception {
        try {
            EncodedPng encoded = ImageCaptureSupport.encodeDownscaledPng(composed, null, PngSizeTarget.LARGE.targetBytes());
            byte[] pngBytes = encoded.bytes();

            log.info("SVG overlay PNG conversion succeeded ({} bytes, scale={}, composedSize={}x{})",
                    pngBytes.length, encoded.scale(), composed.getWidth(), composed.getHeight());

            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return McpSchema.ImageContent.builder(base64, "image/png").build();

        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Insufficient memory to compose the SVG overlay image", e);
        }
    }

    // No diagram area to match, but render at the resolution diagram images are exported at, so that the SVG comes back the same size whether or not a diagram contributed to the image.
    private BufferedImage renderSvgOnly(ParsedSvg svg) {
        double scale = systemPropertySupport.imageExportDpi() / DIAGRAM_COORDINATE_DPI;
        Dimension svgPixelSize = imageConvertSupport.scaledSizeOf(svg, scale);
        requireRenderableSvgLayer(svgPixelSize.width, svgPixelSize.height);

        BufferedImage canvas = newOpaqueCanvas(svgPixelSize.width, svgPixelSize.height);
        drawOnto(canvas, imageConvertSupport.rasterize(svg, svgPixelSize.width, svgPixelSize.height), 0, 0);

        return canvas;
    }

    private BufferedImage composeOverDiagram(ExportedImage exported, ParsedSvg svg) throws Exception {
        BufferedImage diagramImage = exported.image();
        Rectangle2D boundRect = exported.boundRect();

        // The exported image spans the diagram bound rectangle, so its pixel (0,0) is diagram coordinate
        // (boundRect.x, boundRect.y) - generally not (0,0), and possibly negative.
        double pixelsPerUnitX = pixelsPerUnit(diagramImage.getWidth(), boundRect.getWidth());
        double pixelsPerUnitY = pixelsPerUnit(diagramImage.getHeight(), boundRect.getHeight());

        // The viewBox min-x/min-y are the diagram coordinates of the image's top-left corner, and the SVG's natural size is the size insert_svg_img_on_dgm would give the inserted image in diagram coordinates.
        Point2D svgOrigin = svg.viewBoxOrigin();
        Rectangle2D svgRect = new Rectangle2D.Double(
                svgOrigin.getX(), svgOrigin.getY(), svg.naturalWidth(), svg.naturalHeight());

        // Size the SVG raster so that it covers exactly the diagram rectangle it declares, on each axis separately.
        // Kept as long until checked, since the natural size of an SVG that was never rasterized can be anything.
        long svgPixelWidthLong = Math.max(1L, Math.round(svgRect.getWidth() * pixelsPerUnitX));
        long svgPixelHeightLong = Math.max(1L, Math.round(svgRect.getHeight() * pixelsPerUnitY));

        // Checked before the canvas, so that an SVG too large by itself is not blamed on its placement.
        requireRenderableSvgLayer(svgPixelWidthLong, svgPixelHeightLong);
        int svgPixelWidth = (int) svgPixelWidthLong;
        int svgPixelHeight = (int) svgPixelHeightLong;

        ComposedLayout layout = layoutOf(
                boundRect, diagramImage.getWidth(), diagramImage.getHeight(),
                svgRect, svgPixelWidth, svgPixelHeight);

        log.debug("SVG overlay layout (boundRect={}, svgRect={}, svgPixelSize={}x{}, layout={})",
                boundRect, svgRect, svgPixelWidth, svgPixelHeight, layout);

        // Allocated before the SVG is rasterized
        BufferedImage canvas = newOpaqueCanvas(layout.canvasWidth(), layout.canvasHeight());

        BufferedImage svgImage = imageConvertSupport.rasterize(svg, svgPixelWidth, svgPixelHeight);

        drawOnto(canvas, diagramImage, layout.diagramX(), layout.diagramY());
        drawOnto(canvas, svgImage, layout.svgX(), layout.svgY());

        return canvas;
    }

    // Pixel-space placement of the two layers on the composed canvas.
    record ComposedLayout(
        int canvasWidth,
        int canvasHeight,
        int diagramX,
        int diagramY,
        int svgX,
        int svgY) {
    }

    // Anchors both layers to the top-left of the union of their diagram-coordinate rectangles, so each layer keeps
    // a non-negative pixel offset even when the SVG sits above or to the left of the diagram.
    static ComposedLayout layoutOf(Rectangle2D diagramRect, int diagramPixelWidth, int diagramPixelHeight,
                                   Rectangle2D svgRect, int svgPixelWidth, int svgPixelHeight) {

        double pixelsPerUnitX = pixelsPerUnit(diagramPixelWidth, diagramRect.getWidth());
        double pixelsPerUnitY = pixelsPerUnit(diagramPixelHeight, diagramRect.getHeight());

        double unionX = Math.min(diagramRect.getX(), svgRect.getX());
        double unionY = Math.min(diagramRect.getY(), svgRect.getY());

        int diagramX = toPixelOffset((diagramRect.getX() - unionX) * pixelsPerUnitX);
        int diagramY = toPixelOffset((diagramRect.getY() - unionY) * pixelsPerUnitY);
        int svgX = toPixelOffset((svgRect.getX() - unionX) * pixelsPerUnitX);
        int svgY = toPixelOffset((svgRect.getY() - unionY) * pixelsPerUnitY);

        // Size the canvas from the placed layers rather than from the union rectangle, so that rounding can never clip either layer.
        int canvasWidth = Math.max(diagramX + diagramPixelWidth, svgX + svgPixelWidth);
        int canvasHeight = Math.max(diagramY + diagramPixelHeight, svgY + svgPixelHeight);

        return new ComposedLayout(canvasWidth, canvasHeight, diagramX, diagramY, svgX, svgY);
    }

    // Defensive only: createSvgOverlayContents diverts an empty bound rectangle to the standalone render before any
    // composition starts, so this fallback exists to keep layoutOf's arithmetic - and its unit tests - free of a
    // division by zero.
    private static double pixelsPerUnit(int pixels, double units) {
        return units > 0 ? pixels / units : 1.0;
    }

    // Offsets are measured from the union top-left and are therefore never negative; the upper clamp lets the
    // canvas size guard reject an absurd viewBox origin instead of the offset silently wrapping around.
    private static int toPixelOffset(double pixels) {
        return (int) Math.min(Math.max(Math.round(pixels), 0L), MAX_PIXEL_OFFSET);
    }

    // The canvas guard would blame an SVG that is too large by itself on its placement and advise moving it.
    private static void requireRenderableSvgLayer(long width, long height) {
        if ((double) width * height > McpServerConfig.SVG_MAX_RASTER_PIXELS) {
            throw new IllegalArgumentException(String.format(
                "The SVG would be rendered at %dx%d pixels at the resolution diagram images are exported at, above the limit of %d pixels. %s Shrinking does not move the image: the viewBox min-x and min-y still give the diagram coordinates of its top-left corner.",
                width, height, McpServerConfig.SVG_MAX_RASTER_PIXELS, ImageConvertSupport.SHRINK_SVG_ADVICE));
        }
    }

    // An opaque white canvas keeps the composed image readable and drops the alpha channel from the size budget;
    // the SVG layer still blends over the diagram through normal alpha compositing.
    private static BufferedImage newOpaqueCanvas(int width, int height) {
        if ((long) width * (long) height > MAX_COMPOSED_PIXELS) {
            throw new IllegalArgumentException(String.format(
                "The composed image would be %dx%d pixels, which is too large to render. The SVG viewBox min-x/min-y place the image far away from the diagram bound rectangle; move it closer to the diagram.",
                width, height));
        }

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }

        return canvas;
    }

    private static void drawOnto(BufferedImage canvas, BufferedImage layer, int x, int y) {
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.drawImage(layer, x, y, null);
        } finally {
            graphics.dispose();
        }
    }
}
