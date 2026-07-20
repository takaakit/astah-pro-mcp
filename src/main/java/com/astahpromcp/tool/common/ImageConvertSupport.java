package com.astahpromcp.tool.common;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import javax.imageio.ImageIO;

public class ImageConvertSupport {

    private static final int MAX_RASTERIZED_IMAGE_DIMENSION = 4096;

    public record RasterizedSvgImage(
            BufferedImage image,
            int displayWidth,
            int displayHeight
    ) {
    }

    public Image svgToImage(String svgCode) {
        validateSvgCode(svgCode);
        return transcodeSvg(svgCode, null);
    }

    public RasterizedSvgImage svgToRasterizedImage(String svgCode, double scale) {
        validateScale(scale);

        validateSvgCode(svgCode);

        BufferedImage displayImage = transcodeSvg(svgCode, null);
        int displayWidth = displayImage.getWidth();
        int displayHeight = displayImage.getHeight();
        double effectiveScale = Math.min(scale, maxAdditionalScale(displayWidth, displayHeight));
        int rasterizedWidth = Math.max(1, (int) Math.round(displayWidth * effectiveScale));

        if (rasterizedWidth == displayWidth) {
            return new RasterizedSvgImage(displayImage, displayWidth, displayHeight);
        }

        // Only the width hint is passed; Batik derives the height from the SVG's aspect ratio.
        BufferedImage rasterizedImage = transcodeSvg(svgCode, (float) rasterizedWidth);

        return new RasterizedSvgImage(rasterizedImage, displayWidth, displayHeight);
    }

    private void validateScale(double scale) {
        if (scale <= 0.0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
            throw new IllegalArgumentException("Scale must be a positive finite number");
        }
    }

    private double maxAdditionalScale(int displayWidth, int displayHeight) {
        int maxDisplayDimension = Math.max(displayWidth, displayHeight);
        if (maxDisplayDimension <= 0 || maxDisplayDimension >= MAX_RASTERIZED_IMAGE_DIMENSION) {
            return 1.0;
        }

        return Math.max(1.0, (double) MAX_RASTERIZED_IMAGE_DIMENSION / maxDisplayDimension);
    }

    private void validateSvgCode(String svgCode) {
        if (svgCode == null || svgCode.isBlank()) {
            throw new IllegalArgumentException("SVG code must not be null or blank");
        }

        // Validate SVG code
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        try (StringReader reader = new StringReader(svgCode)) {
            Document document = factory.createDocument("internal:svg", reader);
            if (document == null || document.getDocumentElement() == null
                    || !"svg".equals(document.getDocumentElement().getLocalName())) {
                throw new IllegalArgumentException("SVG code must have an <svg> root element");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SVG markup", e);
        }
    }

    private BufferedImage transcodeSvg(String svgCode, Float width) {
        // Create ImageTranscoder
        final BufferedImage[] imageHolder = new BufferedImage[1];
        ImageTranscoder transcoder = new ImageTranscoder() {

            @Override
            public BufferedImage createImage(int width, int height) {
                return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }

            @Override
            public void writeImage(BufferedImage img, TranscoderOutput out) throws TranscoderException {
                imageHolder[0] = img;
            }
        };

        if (width != null) {
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width);
        }

        // Convert SVG to Image
        try (StringReader reader = new StringReader(svgCode)) {
            TranscoderInput input = new TranscoderInput(reader);
            transcoder.transcode(input, null);
            BufferedImage result = imageHolder[0];
            if (result == null) {
                throw new IllegalStateException("ImageTranscoder did not produce an image");
            }
            return result;

        } catch (TranscoderException e) {
            throw new IllegalArgumentException("Failed to convert SVG to Image", e);
        }
    }

    public Image urlToImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL must not be null or blank");
        }

        try {
            URI uri = URI.create(imageUrl);
            try (InputStream inputStream = uri.toURL().openStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new IllegalArgumentException("Failed to read image from URL: " + imageUrl);
                }
                return image;
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to load image from URL: " + imageUrl, e);
        }
    }
}
