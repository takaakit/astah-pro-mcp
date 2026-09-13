package com.astahpromcp.tool.astah.pro.image;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class NoThumbnailImageCaptureSupportTest {

    // Both collaborators are null on purpose: reaching either of them would mean the capture was attempted after all, and the test would fail with a NullPointerException rather than quietly passing.
    private static NoThumbnailImageCaptureSupport supportThatCannotCapture(Path outputDir) {
        return new NoThumbnailImageCaptureSupport(null, null, outputDir);
    }

    @Test
    void createSmallImageContent_ok_answersWithoutCapturingAnything(@TempDir Path outputDir) throws Exception {
        McpSchema.ImageContent content = supportThatCannotCapture(outputDir).createSmallImageContent("any-diagram-id");

        assertNotNull(content, "The editing tools pass this into List.of(...), which rejects null");
        assertEquals("", content.data());
        assertEquals("image/png", content.mimeType());
    }

    // The capture writes the exported diagram, a backup and the re-encoded copy for every single edit, which is most of what it costs. Nothing of the sort may happen here.
    @Test
    void createSmallImageContent_ok_writesNothingToDisk(@TempDir Path outputDir) throws Exception {
        NoThumbnailImageCaptureSupport support = supportThatCannotCapture(outputDir);

        for (int i = 0; i < 20; i++) {
            support.createSmallImageContent("diagram-" + i);
        }

        try (var entries = Files.list(outputDir)) {
            assertEquals(0, entries.count(), "Skipping the capture must not leave files behind");
        }
    }

    @Test
    void createSmallImageContent_ok_isCheapEnoughToBeFreeInPractice(@TempDir Path outputDir) throws Exception {
        NoThumbnailImageCaptureSupport support = supportThatCannotCapture(outputDir);

        long startNanos = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            support.createSmallImageContent("diagram");
        }
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        // One real capture costs about 300 ms; a thousand of these must not come near that.
        assertTrue(elapsedMillis < 100,
                "1000 skipped captures took " + elapsedMillis + " ms, so something is still being rendered");
    }

    // Only the after-the-edit thumbnail is dropped. A capture the caller actually asked for still goes through the inherited implementation, which is what keeps capture_dgm_img and its siblings working on every profile.
    @Test
    void createSmallImageContent_ok_overridesOnlyTheThumbnail() throws Exception {
        assertEquals(NoThumbnailImageCaptureSupport.class,
                NoThumbnailImageCaptureSupport.class.getMethod("createSmallImageContent", String.class)
                        .getDeclaringClass());

        for (String inherited : new String[]{"createLargeImageContent", "createImageContent",
                "createCroppedImageContent", "createWindowImageContent"}) {
            boolean overridden = java.util.Arrays.stream(NoThumbnailImageCaptureSupport.class.getDeclaredMethods())
                    .anyMatch(method -> method.getName().equals(inherited));
            assertFalse(overridden, "'" + inherited + "' is a capture the caller asked for and must not be skipped");
        }
    }
}
