package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.config.McpServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

// The files of the comparisons: one folder per comparison, holding the copies of the project files, the XMIs, the logs
// of the separate processes and the result
@Slf4j
public final class ProjectDiffStore {

    static final String BASE = "base";
    static final String TARGET = "target";
    private static final String RESULT_FILE = "result.json";

    // How much to copy between checks of the time and of a request to stop
    private static final long COPY_CHUNK_BYTES = 4L * 1024 * 1024;

    private final Path root;

    ProjectDiffStore(Path root) {
        this.root = root;
    }

    Path directoryOf(String diffId) {
        return root.resolve(diffId);
    }

    public static Path baseOf(Path jobDirectory) {
        return jobDirectory.resolve(BASE);
    }

    public static Path targetOf(Path jobDirectory) {
        return jobDirectory.resolve(TARGET);
    }

    static Path resultOf(Path jobDirectory) {
        return jobDirectory.resolve(RESULT_FILE);
    }

    // Copy a project file, giving up when it takes too long or when stopping is requested.
    // The copy runs while diff_proj holds the Astah API lock, so a slow disk must not hold every other tool for long.
    public static void copy(Path source, Path target, BooleanSupplier stopRequested) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(McpServerConfig.PROJECT_DIFF_COPY_TIMEOUT_SECONDS);
        try {
            long size = Files.size(source);
            if (size > McpServerConfig.PROJECT_DIFF_MAX_PROJECT_FILE_BYTES) {
                throw new IllegalStateException(String.format("The project file %s is %,d bytes, which is more than the %,d bytes this server compares.",
                        source, size, McpServerConfig.PROJECT_DIFF_MAX_PROJECT_FILE_BYTES));
            }

            Files.createDirectories(target.getParent());
            try (FileChannel in = FileChannel.open(source, StandardOpenOption.READ);
                 FileChannel out = FileChannel.open(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                long position = 0;
                while (position < size) {
                    if (stopRequested.getAsBoolean()) {
                        throw new IllegalStateException("The comparison was stopped.");
                    }
                    if (System.nanoTime() > deadline) {
                        throw new IllegalStateException("Copying the project file " + source + " took longer than " + McpServerConfig.PROJECT_DIFF_COPY_TIMEOUT_SECONDS + " seconds.");
                    }
                    long copied = in.transferTo(position, Math.min(COPY_CHUNK_BYTES, size - position), out);
                    if (copied <= 0) {
                        break;
                    }
                    position += copied;
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy the project file " + source + ": " + e.getMessage(), e);
        }
    }

    // How many bytes the files under the folder take
    static long sizeOf(Path directory) {
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            return 0;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return 0;
                }
            }).sum();
        } catch (IOException e) {
            log.warn("Failed to measure {}", directory, e);
            return 0;
        }
    }

    // Delete a folder and everything in it; true when it is gone.
    static boolean delete(Path directory) {
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            return true;
        }
        try {
            FileUtils.deleteDirectory(directory.toFile());
            return true;
        } catch (IOException e) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.debug("Failed to delete {}: {}", directory, root.toString());
            return false;
        }
    }
}
