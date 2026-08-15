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
import java.awt.geom.Point2D;
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
        return transcodeSvg(svgCode, null, null);
    }

    public Image svgToImage(String svgCode, int width, int height) {
        validateSize(width, height);

        validateSvgCode(svgCode);

        return transcodeSvg(svgCode, (float) width, (float) height);
    }

    public RasterizedSvgImage svgToRasterizedImage(String svgCode, double scale) {
        validateScale(scale);

        validateSvgCode(svgCode);

        BufferedImage displayImage = transcodeSvg(svgCode, null, null);
        int displayWidth = displayImage.getWidth();
        int displayHeight = displayImage.getHeight();
        double effectiveScale = Math.min(scale, maxAdditionalScale(displayWidth, displayHeight));
        int rasterizedWidth = Math.max(1, (int) Math.round(displayWidth * effectiveScale));

        if (rasterizedWidth == displayWidth) {
            return new RasterizedSvgImage(displayImage, displayWidth, displayHeight);
        }

        // Only the width hint is passed; Batik derives the height from the SVG's aspect ratio.
        BufferedImage rasterizedImage = transcodeSvg(svgCode, (float) rasterizedWidth, null);

        return new RasterizedSvgImage(rasterizedImage, displayWidth, displayHeight);
    }

    private void validateSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive numbers");
        }
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

    // The size the SVG renders at without any scaling, i.e. the size insert_svg_img_on_dgm gives the inserted image
    // in diagram coordinates. Unlike svgToRasterizedImage this applies no cap, so callers that need an exact size may
    // multiply it themselves and rasterize through svgToImage(svgCode, width, height).
    public Dimension svgDisplaySizeOf(String svgCode) {
        BufferedImage displayImage = (BufferedImage) svgToImage(svgCode);

        return new Dimension(displayImage.getWidth(), displayImage.getHeight());
    }

    // Returns the viewBox min-x and min-y of the root <svg> element, or (0, 0) when no usable viewBox is declared.
    public Point2D.Double svgViewBoxOriginOf(String svgCode) {
        validateSvgCode(svgCode);

        double[] viewBox = parseNumbers(parseSvg(svgCode).getDocumentElement().getAttribute("viewBox"));
        if (viewBox == null || viewBox.length != 4) {
            return new Point2D.Double(0, 0);
        }

        return new Point2D.Double(viewBox[0], viewBox[1]);
    }

    private void validateSvgCode(String svgCode) {
        if (svgCode == null || svgCode.isBlank()) {
            throw new IllegalArgumentException("SVG code must not be null or blank");
        }

        // Validate SVG code
        Document document = parseSvg(svgCode);
        if (document == null || document.getDocumentElement() == null
                || !"svg".equals(document.getDocumentElement().getLocalName())) {
            throw new IllegalArgumentException("SVG code must have an <svg> root element");
        }
    }

    private Document parseSvg(String svgCode) {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        try (StringReader reader = new StringReader(svgCode)) {
            return factory.createDocument("internal:svg", reader);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SVG markup", e);
        }
    }

    // The viewBox values are whitespace separated, but commas are allowed as separators too.
    private static double[] parseNumbers(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String[] tokens = value.trim().split("[\\s,]+");
        double[] numbers = new double[tokens.length];
        try {
            for (int i = 0; i < tokens.length; i++) {
                numbers[i] = Double.parseDouble(tokens[i]);
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return numbers;
    }

    private BufferedImage transcodeSvg(String svgCode, Float width, Float height) {
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
        if (height != null) {
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height);
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
