package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.EncodedPng;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.ExportedImage;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport.PngSizeTarget;
import com.astahpromcp.tool.common.ImageConvertSupport;
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
    private static final long MAX_COMPOSED_PIXELS = 4096L * 4096L;

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

        if (diagramId == null || diagramId.isBlank()) {
            return List.of(encodeAsPng(renderSvgOnly(imageSvgCode)));
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
                encodeAsPng(renderSvgOnly(imageSvgCode)));
        }

        return List.of(encodeAsPng(composeOverDiagram(exported, imageSvgCode)));
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
    private BufferedImage renderSvgOnly(String imageSvgCode) {
        double scale = systemPropertySupport.imageExportDpi() / DIAGRAM_COORDINATE_DPI;
        BufferedImage svgImage = imageConvertSupport.svgToRasterizedImage(imageSvgCode, scale).image();

        BufferedImage canvas = newOpaqueCanvas(svgImage.getWidth(), svgImage.getHeight());
        drawOnto(canvas, svgImage, 0, 0);

        return canvas;
    }

    private BufferedImage composeOverDiagram(ExportedImage exported, String imageSvgCode) throws Exception {
        BufferedImage diagramImage = exported.image();
        Rectangle2D boundRect = exported.boundRect();

        // The exported image spans the diagram bound rectangle, so its pixel (0,0) is diagram coordinate
        // (boundRect.x, boundRect.y) - generally not (0,0), and possibly negative.
        double pixelsPerUnitX = pixelsPerUnit(diagramImage.getWidth(), boundRect.getWidth());
        double pixelsPerUnitY = pixelsPerUnit(diagramImage.getHeight(), boundRect.getHeight());

        // The viewBox min-x/min-y are the diagram coordinates of the image's top-left corner, and the SVG's natural size is the size insert_svg_img_on_dgm would give the inserted image in diagram coordinates.
        Point2D svgOrigin = imageConvertSupport.svgViewBoxOriginOf(imageSvgCode);
        Dimension svgDisplaySize = imageConvertSupport.svgDisplaySizeOf(imageSvgCode);
        Rectangle2D svgRect = new Rectangle2D.Double(
                svgOrigin.getX(), svgOrigin.getY(), svgDisplaySize.width, svgDisplaySize.height);

        // Size the SVG raster so that it covers exactly the diagram rectangle it declares, on each axis separately.
        int svgPixelWidth = Math.max(1, (int) Math.round(svgRect.getWidth() * pixelsPerUnitX));
        int svgPixelHeight = Math.max(1, (int) Math.round(svgRect.getHeight() * pixelsPerUnitY));

        ComposedLayout layout = layoutOf(
                boundRect, diagramImage.getWidth(), diagramImage.getHeight(),
                svgRect, svgPixelWidth, svgPixelHeight);

        log.debug("SVG overlay layout (boundRect={}, svgRect={}, svgPixelSize={}x{}, layout={})",
                boundRect, svgRect, svgPixelWidth, svgPixelHeight, layout);

        // Allocated before the SVG is rasterized
        BufferedImage canvas = newOpaqueCanvas(layout.canvasWidth(), layout.canvasHeight());

        BufferedImage svgImage = (BufferedImage) imageConvertSupport.svgToImage(imageSvgCode, svgPixelWidth, svgPixelHeight);

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
