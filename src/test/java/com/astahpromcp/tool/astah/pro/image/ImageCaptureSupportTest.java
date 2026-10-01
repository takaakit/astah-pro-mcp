package com.astahpromcp.tool.astah.pro.image;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.change_vision.jude.api.inf.model.IDiagram;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageCaptureSupportTest {

    private static final String DIAGRAM_ID = "diagram-id";

    @Test
    void cropRectangleOf_ok_fullCoversWholeImage() {
        Rectangle crop = ImageCaptureSupport.cropRectangleOf(ImageRegion.FULL, 800, 600);

        assertEquals(new Rectangle(0, 0, 800, 600), crop);
    }

    @Test
    void cropRectangleOf_ok_quadrantsTileTheImageExactlyEvenForOddSizes() {
        // Odd width/height: the right/bottom halves must take the remainder pixel
        int width = 801;
        int height = 601;

        Rectangle topLeft = ImageCaptureSupport.cropRectangleOf(ImageRegion.TOP_LEFT, width, height);
        Rectangle topRight = ImageCaptureSupport.cropRectangleOf(ImageRegion.TOP_RIGHT, width, height);
        Rectangle bottomLeft = ImageCaptureSupport.cropRectangleOf(ImageRegion.BOTTOM_LEFT, width, height);
        Rectangle bottomRight = ImageCaptureSupport.cropRectangleOf(ImageRegion.BOTTOM_RIGHT, width, height);

        // The right column starts exactly where the left column ends (no gap, no overlap)
        assertEquals(topLeft.x + topLeft.width, topRight.x);
        assertEquals(bottomLeft.x + bottomLeft.width, bottomRight.x);

        // The bottom row starts exactly where the top row ends
        assertEquals(topLeft.y + topLeft.height, bottomLeft.y);
        assertEquals(topRight.y + topRight.height, bottomRight.y);

        // The four quadrants together cover the whole image
        assertEquals(width, topLeft.width + topRight.width);
        assertEquals(width, bottomLeft.width + bottomRight.width);
        assertEquals(height, topLeft.height + bottomLeft.height);
        assertEquals(height, topRight.height + bottomRight.height);
        assertEquals(width, topRight.x + topRight.width);
        assertEquals(height, bottomLeft.y + bottomLeft.height);
    }

    @Test
    void encodeDownscaledPng_ok_keepsFullResolutionWhenPngFitsTarget() throws Exception {
        // A large but uniform image compresses to a tiny PNG, so it must be kept at full resolution
        // even though its raw RGBA size (2000*2000*4 ≈ 15 MB) far exceeds the 50 KB target.
        BufferedImage image = uniformImage(2000, 2000, Color.WHITE);

        ImageCaptureSupport.EncodedPng encoded = ImageCaptureSupport.encodeDownscaledPng(image, null, 50L * 1024);

        assertEquals(1.0, encoded.scale(), "A well-compressing image must not be downscaled");
        BufferedImage decoded = decode(encoded.bytes());
        assertEquals(2000, decoded.getWidth());
        assertEquals(2000, decoded.getHeight());
    }

    @Test
    void encodeDownscaledPng_ok_downscalesWhenPngExceedsTarget() throws Exception {
        // Random noise is incompressible, so a 1000x1000 PNG comfortably exceeds the 50 KB target
        // and must be downscaled.
        BufferedImage image = noiseImage(1000, 1000);

        ImageCaptureSupport.EncodedPng encoded = ImageCaptureSupport.encodeDownscaledPng(image, null, 50L * 1024);

        assertTrue(encoded.scale() < 1.0, "An oversized PNG must be downscaled, but scale was " + encoded.scale());
        assertTrue(encoded.scale() >= 0.1, "Scale must not fall below the 0.1 floor");
        BufferedImage decoded = decode(encoded.bytes());
        assertTrue(decoded.getWidth() < 1000 && decoded.getHeight() < 1000,
                "Downscaled image must be smaller than the source");
    }

    @Test
    void encodeDownscaledPng_ok_honorsSourceRegionAtFullResolution() throws Exception {
        // A uniform image stays at full resolution; the output must span exactly the requested region.
        BufferedImage image = uniformImage(800, 600, Color.WHITE);
        Rectangle region = new Rectangle(100, 50, 300, 200);

        ImageCaptureSupport.EncodedPng encoded = ImageCaptureSupport.encodeDownscaledPng(image, region, 50L * 1024);

        assertEquals(1.0, encoded.scale());
        BufferedImage decoded = decode(encoded.bytes());
        assertEquals(300, decoded.getWidth());
        assertEquals(200, decoded.getHeight());
    }

    @Test
    void scaledImagePathOf_ok_prefixesTheFileNameAndKeepsTheNamespaceDirectories(@TempDir Path outputDir) {
        // exportImage returns the diagram's namespace as directories, so the prefix must not touch them:
        // "scaled_" + the whole path would name a directory ("scaled_State") that was never created.
        Path scaledImagePath = ImageCaptureSupport.scaledImagePathOf(
                outputDir, relativePath("State", "Statemachine", "Statemachine Diagram.png"), 1.0);

        assertEquals(outputDir.resolve("State").resolve("Statemachine").resolve("scaled_Statemachine Diagram_scale1.00.png"),
                scaledImagePath);
    }

    @Test
    void scaledImagePathOf_ok_keepsARootLevelDiagramDirectlyInTheOutputDir() {
        // A diagram owned by the root package has no directory component at all.
        Path outputDir = Path.of("out");

        Path scaledImagePath = ImageCaptureSupport.scaledImagePathOf(outputDir, "Class Diagram.png", 0.76);

        assertEquals(outputDir.resolve("scaled_Class Diagram_scale0.76.png"), scaledImagePath);
    }

    @Test
    void scaledImagePathOf_ok_writesADecimalPointEvenInACommaDecimalLocale() {
        // German and friends format 1.0 as "1,00", which would make the file name vary by machine.
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);

            Path scaledImagePath = ImageCaptureSupport.scaledImagePathOf(Path.of("out"), "Class Diagram.png", 1.0);

            assertEquals("scaled_Class Diagram_scale1.00.png", scaledImagePath.getFileName().toString());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void saveScaledImage_ok_createsTheMissingNamespaceDirectories(@TempDir Path outputDir) throws Exception {
        byte[] pngBytes = {1, 2, 3};

        ImageCaptureSupport.saveScaledImage(
                outputDir, relativePath("State", "Statemachine", "Statemachine Diagram.png"), 1.0, pngBytes);

        Path expected = outputDir.resolve("State").resolve("Statemachine").resolve("scaled_Statemachine Diagram_scale1.00.png");
        assertTrue(Files.exists(expected), "The scaled image must be written next to the exported one");
        assertArrayEquals(pngBytes, Files.readAllBytes(expected));
    }

    @Test
    void saveScaledImage_ok_overwritesWhenTheParentDirectoryAlreadyExists(@TempDir Path outputDir) throws Exception {
        Files.createDirectories(outputDir.resolve("Deployment"));
        String relativeImagePath = relativePath("Deployment", "Deployment Diagram.png");
        ImageCaptureSupport.saveScaledImage(outputDir, relativeImagePath, 1.0, new byte[]{1, 2, 3});

        ImageCaptureSupport.saveScaledImage(outputDir, relativeImagePath, 1.0, new byte[]{4, 5});

        Path expected = outputDir.resolve("Deployment").resolve("scaled_Deployment Diagram_scale1.00.png");
        assertArrayEquals(new byte[]{4, 5}, Files.readAllBytes(expected), "The second write must replace the first");
    }

    @Test
    void saveScaledImage_ng_swallowsTheFailureAndNamesTheExceptionClass(@TempDir Path outputDir) throws Exception {
        // A plain file where the namespace directory must go: createDirectories then fails deterministically
        // on every platform, unlike a read-only directory.
        Files.createFile(outputDir.resolve("State"));

        ListAppender<ILoggingEvent> captured = attachAppenderTo(ImageCaptureSupport.class);
        try {
            assertDoesNotThrow(() -> ImageCaptureSupport.saveScaledImage(
                    outputDir, relativePath("State", "Statemachine Diagram.png"), 1.0, new byte[]{1, 2, 3}),
                    "Saving the troubleshooting copy must never break the tool call");

            List<ILoggingEvent> warnings = captured.list.stream()
                    .filter(event -> event.getLevel() == Level.WARN)
                    .toList();
            assertEquals(1, warnings.size(), "The failure must be reported exactly once");
            assertTrue(warnings.getFirst().getFormattedMessage().contains("FileAlreadyExistsException"),
                    "The warning must name the exception class, not just the path: "
                            + warnings.getFirst().getFormattedMessage());
        } finally {
            detachAppenderFrom(ImageCaptureSupport.class, captured);
        }
    }

    @Test
    void createThumbnailContent_ng_answersWithANoticeWhenTheOutputDirectoryCannotBeCreated(@TempDir Path workspace) throws Exception {
        // The edit is committed before its thumbnail is taken, so a throw here would report a done edit as failed,
        // and the client would repeat it. A plain file where the output directory must go fails on every platform.
        Path imageOutputDir = Files.createFile(workspace.resolve("images"));
        ImageCaptureSupport support = supportFor(mock(IDiagram.class), imageOutputDir);

        ListAppender<ILoggingEvent> captured = attachAppenderTo(ImageCaptureSupport.class);
        try {
            McpSchema.Content content = support.createThumbnailContent(DIAGRAM_ID);

            String notice = assertInstanceOf(McpSchema.TextContent.class, content).text();
            assertTrue(notice.contains("do not repeat it"), "The notice must keep the client from repeating the edit: " + notice);
            assertTrue(notice.contains("Failed to create output directory"), "The notice must say what went wrong: " + notice);

            long errors = captured.list.stream().filter(event -> event.getLevel() == Level.ERROR).count();
            assertEquals(1, errors, "The failure must be reported exactly once, as an error");
        } finally {
            detachAppenderFrom(ImageCaptureSupport.class, captured);
        }
    }

    @Test
    void createThumbnailContent_ng_answersWithANoticeWhenTheExportRunsOutOfMemory(@TempDir Path outputDir) throws Exception {
        // exportImage is wrapped for Exception only, so an OutOfMemoryError there reaches the thumbnail as it is. Its
        // message, "Java heap space", does not say that memory ran out, so the notice must name the error as well.
        IDiagram diagram = mock(IDiagram.class);
        when(diagram.exportImage(anyString(), anyString(), anyDouble())).thenThrow(new OutOfMemoryError("Java heap space"));

        McpSchema.Content content;
        try {
            content = supportFor(diagram, outputDir).createThumbnailContent(DIAGRAM_ID);
        } catch (OutOfMemoryError e) {
            // Left to escape, the error would abort the whole test run instead of failing this test.
            throw new AssertionError("The OutOfMemoryError must be answered with a notice, not thrown", e);
        }

        String notice = assertInstanceOf(McpSchema.TextContent.class, content).text();
        assertTrue(notice.contains("OutOfMemoryError"), "The notice must name the error: " + notice);
        assertTrue(notice.contains("Java heap space"), "The notice must keep the error's message: " + notice);
    }

    // Only the thumbnail an edit returns is spared. An image the caller asked for, as capture_dgm_img does, has nothing
    // committed behind it, so its failure is still the call's failure.
    @Test
    void createLargeImageContent_ng_stillThrowsWhenTheOutputDirectoryCannotBeCreated(@TempDir Path workspace) throws Exception {
        Path imageOutputDir = Files.createFile(workspace.resolve("images"));
        ImageCaptureSupport support = supportFor(mock(IDiagram.class), imageOutputDir);

        Exception thrown = assertThrows(Exception.class, () -> support.createLargeImageContent(DIAGRAM_ID, ImageRegion.FULL));
        assertTrue(thrown.getMessage().contains("Failed to create output directory"),
                "The capture must fail for the output directory, not for anything else: " + thrown);
    }

    @Test
    void createThumbnailContent_ok_answersWithASmallPictureOfTheWholeDiagram(@TempDir Path outputDir) throws Exception {
        // Noise does not compress, so the size a picture comes out at is decided by the size target it was encoded for,
        // and the thumbnail's must leave it well below what capture_dgm_img returns for the same diagram. A marker in
        // two opposite corners shows that the thumbnail covers the whole diagram rather than one quarter of it.
        ImageCaptureSupport support = supportFor(diagramExporting(noiseImageWithCornerMarkers(400, 200, 80)), outputDir);

        McpSchema.Content content = support.createThumbnailContent(DIAGRAM_ID);
        McpSchema.ImageContent capture = support.createLargeImageContent(DIAGRAM_ID, ImageRegion.FULL);

        byte[] thumbnailBytes = Base64.getDecoder().decode(assertInstanceOf(McpSchema.ImageContent.class, content).data());
        byte[] captureBytes = Base64.getDecoder().decode(capture.data());
        assertTrue(thumbnailBytes.length * 3 < captureBytes.length,
                "The thumbnail must be encoded for its own size target: " + thumbnailBytes.length + " bytes against "
                        + captureBytes.length + " bytes for capture_dgm_img");

        BufferedImage thumbnail = decode(thumbnailBytes);
        assertColorNear(Color.RED, thumbnail.getRGB(0, 0), "top-left corner");
        assertColorNear(Color.BLUE, thumbnail.getRGB(thumbnail.getWidth() - 1, thumbnail.getHeight() - 1), "bottom-right corner");
    }

    private static ImageCaptureSupport supportFor(IDiagram diagram, Path imageOutputDir) {
        AstahProToolSupport astahProToolSupport = mock(AstahProToolSupport.class);
        when(astahProToolSupport.getDiagram(DIAGRAM_ID)).thenReturn(diagram);
        return new ImageCaptureSupport(astahProToolSupport, new SystemPropertySupport(), imageOutputDir);
    }

    // A diagram whose export writes the given picture into the output directory and answers its path, as Astah does.
    private static IDiagram diagramExporting(BufferedImage picture) throws Exception {
        IDiagram diagram = mock(IDiagram.class);
        when(diagram.exportImage(anyString(), anyString(), anyDouble())).thenAnswer(invocation -> {
            String outputDir = invocation.getArgument(0);
            ImageIO.write(picture, "png", Path.of(outputDir).resolve("Class Diagram.png").toFile());
            return "Class Diagram.png";
        });
        return diagram;
    }

    // Noise with a solid red block in the top-left corner and a solid blue one in the bottom-right corner.
    private static BufferedImage noiseImageWithCornerMarkers(int width, int height, int markerSize) {
        BufferedImage image = noiseImage(width, height);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.RED);
            graphics.fillRect(0, 0, markerSize, markerSize);
            graphics.setColor(Color.BLUE);
            graphics.fillRect(width - markerSize, height - markerSize, markerSize, markerSize);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    // Downscaling blends neighboring pixels, so a marker is recognized by being close to its color, not equal to it.
    private static void assertColorNear(Color expected, int actualRgb, String where) {
        Color actual = new Color(actualRgb);
        int distance = Math.abs(expected.getRed() - actual.getRed())
                + Math.abs(expected.getGreen() - actual.getGreen())
                + Math.abs(expected.getBlue() - actual.getBlue());
        assertTrue(distance < 90, "The " + where + " must show its marker " + expected + ", but shows " + actual);
    }

    // exportImage hands back a path in the platform's own separators, and Paths.get only splits on those.
    private static String relativePath(String... names) {
        return String.join(File.separator, names);
    }

    private static ListAppender<ILoggingEvent> attachAppenderTo(Class<?> type) {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(type);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private static void detachAppenderFrom(Class<?> type, ListAppender<ILoggingEvent> appender) {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(type);
        logger.detachAppender(appender);
        appender.stop();
    }

    private static BufferedImage uniformImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static BufferedImage noiseImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(42);  // fixed seed for a deterministic test
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, random.nextInt(0xFFFFFF + 1));
            }
        }
        return image;
    }

    private static BufferedImage decode(byte[] pngBytes) throws Exception {
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(pngBytes));
        assertNotNull(decoded, "The encoded bytes must be a readable PNG");
        return decoded;
    }
}
