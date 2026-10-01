package com.astahpromcp.tool.common;

import com.astahpromcp.config.McpServerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void urlToImage_ok_readsLocalFileUrl(@TempDir Path tempDir) throws Exception {
        Path png = writePng(tempDir, new BufferedImage(120, 80, BufferedImage.TYPE_INT_ARGB));

        BufferedImage image = (BufferedImage) imageConvertSupport.urlToImage(png.toUri().toString());

        assertEquals(120, image.getWidth());
        assertEquals(80, image.getHeight());
    }

    @Test
    void urlToImage_ng_timesOutOnServerThatTricklesBytes(@TempDir Path tempDir) throws Exception {
        byte[] png = Files.readAllBytes(writePng(tempDir, new BufferedImage(120, 80, BufferedImage.TYPE_INT_ARGB)));

        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            Thread sender = new Thread(() -> trickle(server, png), "trickling-server");
            sender.setDaemon(true);
            sender.start();

            long start = System.nanoTime();
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> imageConvertSupport.urlToImage("http://127.0.0.1:" + server.getLocalPort() + "/image.png"));
            long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);

            assertTrue(e.getMessage().contains("Timed out"), e.getMessage());
            assertTrue(elapsedSeconds < McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS + 5, "took " + elapsedSeconds + "s");
        }
    }

    @Test
    void urlToImage_ng_rejectsImageAboveThePixelLimit(@TempDir Path tempDir) throws Exception {
        // 64 million pixels, yet only a few megabytes in memory and a few kilobytes on disk
        Path png = writePng(tempDir, new BufferedImage(8000, 8000, BufferedImage.TYPE_BYTE_BINARY));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> imageConvertSupport.urlToImage(png.toUri().toString()));

        assertTrue(e.getMessage().contains("above the limit"), e.getMessage());
    }

    @Test
    void urlToImage_ng_rejectsNetworkShareFileUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> imageConvertSupport.urlToImage("file://192.0.2.1/share/image.png"));
    }

    @Test
    void urlToImage_ng_rejectsUnsupportedScheme() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> imageConvertSupport.urlToImage("ftp://127.0.0.1/image.png"));

        assertTrue(e.getMessage().contains("Only http, https and file URLs are supported"), e.getMessage());
    }

    private Path writePng(Path dir, BufferedImage image) throws Exception {
        Path png = dir.resolve("image.png");
        ImageIO.write(image, "png", png.toFile());
        return png;
    }

    // Answers one request with a valid PNG, sent one byte per second.
    private void trickle(ServerSocket server, byte[] png) {
        try (Socket socket = server.accept()) {
            socket.getInputStream().read(new byte[4096]);
            OutputStream out = socket.getOutputStream();
            out.write("HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            for (byte b : png) {
                out.write(b);
                out.flush();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            // The client closed the connection on timeout
        }
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
