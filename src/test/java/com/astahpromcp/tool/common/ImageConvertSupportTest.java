package com.astahpromcp.tool.common;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageConvertSupportTest {

    private final ImageConvertSupport imageConvertSupport = new ImageConvertSupport();

    @Test
    void svgToImage_ok_preservesSvgDisplaySize() {
        BufferedImage image = (BufferedImage) imageConvertSupport.svgToImage(circleSvg(100, 80));

        assertEquals(100, image.getWidth());
        assertEquals(80, image.getHeight());
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

    private String circleSvg(int width, int height) {
        return "<svg width=\"" + width + "\" height=\"" + height + "\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<circle cx=\"" + (width / 2) + "\" cy=\"" + (height / 2)
                + "\" r=\"30\" fill=\"red\" stroke=\"black\" stroke-width=\"2\"/>"
                + "</svg>";
    }
}
