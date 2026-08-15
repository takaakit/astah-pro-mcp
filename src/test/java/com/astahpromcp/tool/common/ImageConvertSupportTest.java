package com.astahpromcp.tool.common;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageConvertSupportTest {

    private final ImageConvertSupport imageConvertSupport = new ImageConvertSupport();

    @Test
    void svgToImage_ok_preservesSvgDisplaySize() {
        BufferedImage image = (BufferedImage) imageConvertSupport.svgToImage(circleSvg(100, 80));

        assertEquals(100, image.getWidth());
        assertEquals(80, image.getHeight());
    }

    @Test
    void svgToImage_ok_rasterizesAtGivenSize() {
        BufferedImage image = (BufferedImage) imageConvertSupport.svgToImage(circleSvg(100, 80), 160, 120);

        assertEquals(160, image.getWidth());
        assertEquals(120, image.getHeight());
    }

    @Test
    void svgToImage_ng_rejectsZeroWidth() {
        assertThrows(IllegalArgumentException.class,
                () -> imageConvertSupport.svgToImage(circleSvg(100, 80), 0, 120));
    }

    @Test
    void svgToImage_ng_rejectsNegativeHeight() {
        assertThrows(IllegalArgumentException.class,
                () -> imageConvertSupport.svgToImage(circleSvg(100, 80), 160, -1));
    }

    @Test
    void svgToRasterizedImage_ok_upscalesImageAndKeepsDisplaySize() {
        ImageConvertSupport.RasterizedSvgImage result = imageConvertSupport.svgToRasterizedImage(
                circleSvg(100, 80),
                4.0);

        assertEquals(400, result.image().getWidth());
        assertEquals(320, result.image().getHeight());
        assertEquals(100, result.displayWidth());
        assertEquals(80, result.displayHeight());
    }

    @Test
    void svgViewBoxOriginOf_ok_readsViewBoxMinXAndMinY() {
        Point2D.Double origin = imageConvertSupport.svgViewBoxOriginOf(circleSvg(100, 80, "300 -40 100 80"));

        assertEquals(300.0, origin.getX());
        assertEquals(-40.0, origin.getY());
    }

    @Test
    void svgViewBoxOriginOf_ok_acceptsCommaSeparatedViewBox() {
        Point2D.Double origin = imageConvertSupport.svgViewBoxOriginOf(circleSvg(100, 80, "300, -40, 100, 80"));

        assertEquals(300.0, origin.getX());
        assertEquals(-40.0, origin.getY());
    }

    @Test
    void svgViewBoxOriginOf_ok_fallsBackToOriginWithoutViewBox() {
        Point2D.Double origin = imageConvertSupport.svgViewBoxOriginOf(circleSvg(100, 80));

        assertEquals(0.0, origin.getX());
        assertEquals(0.0, origin.getY());
    }

    @Test
    void svgViewBoxOriginOf_ok_fallsBackToOriginOnMalformedViewBox() {
        Point2D.Double origin = imageConvertSupport.svgViewBoxOriginOf(circleSvg(100, 80, "300 -40"));

        assertEquals(0.0, origin.getX());
        assertEquals(0.0, origin.getY());
    }

    @Test
    void svgViewBoxOriginOf_ng_rejectsBlankSvgCode() {
        assertThrows(IllegalArgumentException.class, () -> imageConvertSupport.svgViewBoxOriginOf("  "));
    }

    private String circleSvg(int width, int height) {
        return circleSvg(width, height, null);
    }

    private String circleSvg(int width, int height, String viewBox) {
        return "<svg width=\"" + width + "\" height=\"" + height + "\""
                + (viewBox == null ? "" : " viewBox=\"" + viewBox + "\"")
                + " xmlns=\"http://www.w3.org/2000/svg\">"
                + "<circle cx=\"" + (width / 2) + "\" cy=\"" + (height / 2)
                + "\" r=\"30\" fill=\"red\" stroke=\"black\" stroke-width=\"2\"/>"
                + "</svg>";
    }
}
