package com.astahpromcp.tool.common;

import com.astahpromcp.config.McpServerConfig;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageConvertSupport {

    // Upscaling an SVG for sharpness stops once the longer side of the raster reaches this many pixels.
    private static final int MAX_RASTERIZED_IMAGE_DIMENSION = 4096;

    // The fix every refusal of an oversized SVG points to.
    public static final String SHRINK_SVG_ADVICE = "Reduce the width and height attributes, and keep (or add) a viewBox covering the drawing so that its contents scale down with them.";

    // An SVG parsed once, so that measuring it and rasterizing it - possibly more than once - reuse the same document.
    // The natural size is the size the SVG renders at without any scaling, i.e. the size insert_svg_img_on_dgm gives the inserted image in diagram coordinates.
    public record ParsedSvg(
            Document document,
            int naturalWidth,
            int naturalHeight,
            Point2D.Double viewBoxOrigin
    ) {
    }

    public record RasterizedSvgImage(
            BufferedImage image,
            int displayWidth,
            int displayHeight
    ) {
    }

    // The scale is 1.0 when the SVG fits within the pixel limit at its natural size.
    public record FittedSvgImage(
            BufferedImage image,
            double scale
    ) {
    }

    // Measures the SVG without allocating any pixels, so that callers can refuse an oversized SVG before rasterizing it.
    public ParsedSvg parseSvg(String svgCode) {
        if (svgCode == null || svgCode.isBlank()) {
            throw new IllegalArgumentException("SVG code must not be null or blank");
        }

        Document document = parseDocument(svgCode);
        if (document == null || document.getDocumentElement() == null
                || !"svg".equals(document.getDocumentElement().getLocalName())) {
            throw new IllegalArgumentException("SVG code must have an <svg> root element");
        }

        long[] size = measure(document);
        if (size[0] <= 0 || size[1] <= 0) {
            throw new IllegalArgumentException(String.format(
                    "The SVG renders at %dx%d pixels, which leaves nothing to draw. Give it positive width and height attributes.",
                    size[0], size[1]));
        }

        return new ParsedSvg(document, saturatedInt(size[0]), saturatedInt(size[1]), viewBoxOriginOf(document));
    }

    // An inserted image is displayed at the natural size of the SVG in diagram coordinates.
    public void requireInsertableSize(ParsedSvg svg) {
        int maxDimension = McpServerConfig.SVG_MAX_DISPLAY_DIMENSION;
        if (svg.naturalWidth() > maxDimension || svg.naturalHeight() > maxDimension) {
            throw new IllegalArgumentException(String.format(
                    "The SVG renders at %dx%d, which would be the size of the inserted image on the diagram, above the limit of %d on each side. %s",
                    svg.naturalWidth(), svg.naturalHeight(), maxDimension, SHRINK_SVG_ADVICE));
        }
    }

    public BufferedImage rasterize(ParsedSvg svg, int width, int height) {
        validateSize(width, height);

        // The natural size is rendered without size hints, exactly as Batik draws the SVG by itself.
        if (width == svg.naturalWidth() && height == svg.naturalHeight()) {
            return transcode(svg.document(), null, null);
        }

        // With the width alone, Batik keeps its default viewport height of 400 for an SVG sized in percentages or by
        // its viewBox only, and the raster comes out with a different aspect ratio than the natural size.
        return transcode(svg.document(), (float) width, (float) height);
    }

    // The size rasterizeScaled renders at: the natural size times the scale, except that enlarging stops once the longer side reaches MAX_RASTERIZED_IMAGE_DIMENSION.
    public Dimension scaledSizeOf(ParsedSvg svg, double scale) {
        validateScale(scale);

        int naturalWidth = svg.naturalWidth();
        int naturalHeight = svg.naturalHeight();
        double effectiveScale = Math.min(scale, maxAdditionalScale(naturalWidth, naturalHeight));
        if (effectiveScale == 1.0) {
            return new Dimension(naturalWidth, naturalHeight);
        }

        return new Dimension(scaledLength(naturalWidth, effectiveScale), scaledLength(naturalHeight, effectiveScale));
    }

    public RasterizedSvgImage rasterizeScaled(ParsedSvg svg, double scale) {
        Dimension size = scaledSizeOf(svg, scale);
        BufferedImage image = rasterize(svg, size.width, size.height);

        return new RasterizedSvgImage(image, svg.naturalWidth(), svg.naturalHeight());
    }

    // For an image the AI agent only looks at, shrinking serves better than refusing.
    public FittedSvgImage rasterizeWithinPixelLimit(ParsedSvg svg) {
        long maxPixels = McpServerConfig.SVG_MAX_RASTER_PIXELS;
        long naturalPixels = (long) svg.naturalWidth() * svg.naturalHeight();
        if (naturalPixels <= maxPixels) {
            return new FittedSvgImage(rasterize(svg, svg.naturalWidth(), svg.naturalHeight()), 1.0);
        }

        // Rounded down, and an axis that rounds down to nothing gives its share back to the other, so that the product never exceeds the limit.
        double scale = Math.sqrt((double) maxPixels / naturalPixels);
        long width = Math.max(1L, (long) (svg.naturalWidth() * scale));
        long height = Math.max(1L, Math.min((long) (svg.naturalHeight() * scale), maxPixels / width));
        width = Math.min(width, maxPixels / height);

        return new FittedSvgImage(rasterize(svg, (int) width, (int) height), scale);
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

    private static int scaledLength(int length, double scale) {
        return saturatedInt(Math.max(1L, Math.round(length * scale)));
    }

    private static int saturatedInt(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    // Returns the viewBox min-x and min-y of the root <svg> element, or (0, 0) when no usable viewBox is declared.
    private static Point2D.Double viewBoxOriginOf(Document document) {
        double[] viewBox = parseNumbers(document.getDocumentElement().getAttribute("viewBox"));
        if (viewBox == null || viewBox.length != 4) {
            return new Point2D.Double(0, 0);
        }

        return new Point2D.Double(viewBox[0], viewBox[1]);
    }

    private Document parseDocument(String svgCode) {
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

    // Measuring with the transcoder itself, rather than with a BridgeContext of our own, keeps percentages resolving against the same default 400x400 viewport that rasterizing uses.
    private long[] measure(Document document) {
        SvgTranscoder transcoder = new SvgTranscoder(true);
        try {
            run(transcoder, document);
        } catch (SvgTranscoder.Measured measured) {
            return new long[] { measured.width, measured.height };
        }

        throw new IllegalStateException("ImageTranscoder did not report the size of the SVG");
    }

    private BufferedImage transcode(Document document, Float width, Float height) {
        SvgTranscoder transcoder = new SvgTranscoder(false);
        if (width != null) {
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width);
        }
        if (height != null) {
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height);
        }

        run(transcoder, document);
        if (transcoder.image == null) {
            throw new IllegalStateException("ImageTranscoder did not produce an image");
        }

        return transcoder.image;
    }

    private static void run(SvgTranscoder transcoder, Document document) {
        boolean completed = false;
        try {
            transcoder.transcode(new TranscoderInput(document), null);
            completed = true;

        } catch (TranscoderException e) {
            throw new IllegalArgumentException("Failed to convert SVG to Image", e);

        } finally {
            // SVGAbstractTranscoder disposes its bridge context only when transcoding completes, and the document
            // is rasterized again after it has been measured.
            if (!completed) {
                transcoder.disposeBridgeContext();
            }
        }
    }

    private static final class SvgTranscoder extends ImageTranscoder {

        // Leaves transcode() once the size is known. No error, so it carries no stack trace.
        static final class Measured extends RuntimeException {
            final long width;
            final long height;

            Measured(long width, long height) {
                super(null, null, false, false);
                this.width = width;
                this.height = height;
            }
        }

        private final boolean measureOnly;
        private BufferedImage image;

        SvgTranscoder(boolean measureOnly) {
            this.measureOnly = measureOnly;
        }

        // SVGAbstractTranscoder calls this once the GVT tree is built, and ImageTranscoder allocates its working buffer
        // and the output image right after it, so this is the last point at which an oversized SVG costs no pixels.
        @Override
        protected void setImageSize(float docWidth, float docHeight) {
            super.setImageSize(docWidth, docHeight);

            // Rounded the way ImageTranscoder rounds the size of the image it allocates
            long imageWidth = (long) (width + 0.5);
            long imageHeight = (long) (height + 0.5);
            if (measureOnly) {
                throw new Measured(imageWidth, imageHeight);
            }

            if ((double) imageWidth * imageHeight > McpServerConfig.SVG_MAX_RASTER_PIXELS) {
                throw new IllegalArgumentException(String.format(
                        "The SVG would be rasterized at %dx%d pixels, above the limit of %d pixels. %s",
                        imageWidth, imageHeight, McpServerConfig.SVG_MAX_RASTER_PIXELS, SHRINK_SVG_ADVICE));
            }
        }

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput out) {
            image = img;
        }

        void disposeBridgeContext() {
            if (ctx != null) {
                ctx.dispose();
                ctx = null;
            }
        }
    }

    public Image urlToImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL must not be null or blank");
        }

        try {
            URI uri = URI.create(imageUrl);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
            return switch (scheme) {
                case "http", "https" -> readHttpImage(uri);
                case "file" -> readFileImage(uri);
                default -> throw new IllegalArgumentException("Only http, https and file URLs are supported");
            };

        } catch (Exception e) {
            // Only the top-level message reaches the client, so the reason goes into it.
            throw new IllegalArgumentException("Failed to load image from URL: " + imageUrl + " (" + e.getMessage() + ")", e);
        }
    }

    // The whole fetch, not each read, is bounded: a server that trickles bytes defeats a read timeout alone.
    private BufferedImage readHttpImage(URI uri) throws IOException {
        int timeoutMillis = (int) TimeUnit.SECONDS.toMillis(McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);

        // A blocked socket read ignores interrupts, so closing the connection is the only way to end it on time.
        AtomicBoolean timedOut = new AtomicBoolean();
        CompletableFuture<Void> watchdog = CompletableFuture.runAsync(() -> {
            timedOut.set(true);
            connection.disconnect();
        }, CompletableFuture.delayedExecutor(McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        try (InputStream inputStream = connection.getInputStream()) {
            return readImage(inputStream);
        } catch (IOException e) {
            if (timedOut.get()) {
                throw new IOException("Timed out after " + McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS + " seconds", e);
            }
            throw e;
        } finally {
            watchdog.cancel(false);
        }
    }

    // A UNC path would wait on the network share with no timeout at all.
    private BufferedImage readFileImage(URI uri) throws IOException {
        Path path = Path.of(uri);
        if (path.toString().startsWith("\\\\")) {
            throw new IllegalArgumentException("File URLs on a network share are not supported");
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            return readImage(inputStream);
        }
    }

    // Checks the size in the header before decoding: a few megabytes of PNG can decode into gigabytes of pixels.
    private BufferedImage readImage(InputStream inputStream) throws IOException {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new IOException("Not a supported image format");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > McpServerConfig.IMAGE_URL_MAX_PIXELS) {
                    throw new IOException(String.format("Image is %dx%d pixels, above the limit of %d pixels",
                            reader.getWidth(0), reader.getHeight(0), McpServerConfig.IMAGE_URL_MAX_PIXELS));
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }
}
