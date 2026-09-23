package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.OptionalLong;
import java.util.function.LongPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Prepares the workspace directory and deletes the ones left behind
@Slf4j
final class WorkspaceDirectories {

    private static final Pattern WORKSPACE_DIR_NAME = Pattern.compile(
            Pattern.quote(McpServerConfig.WORKSPACE_DIR_NAME_PREFIX) + "(\\d{1,18})");

    private WorkspaceDirectories() {
    }

    // Delete the workspace directories next to the given one whose process no longer exists, then create the given one empty.
    static void prepare(Path workspaceDir, LongPredicate isProcessAlive) throws IOException {
        Path rootDir = workspaceDir.getParent();
        FileUtils.forceMkdir(rootDir.toFile());

        deleteStaleWorkspaceDirectories(rootDir, workspaceDir, isProcessAlive);

        // A directory that already has this name was left behind by an earlier process that had the same ID, or by an earlier start of this server.
        if (Files.exists(workspaceDir, LinkOption.NOFOLLOW_LINKS)) {
            log.info("Delete the workspace directory left behind by an earlier session: {}", workspaceDir);
            delete(workspaceDir);
        }

        FileUtils.forceMkdir(workspaceDir.toFile());
    }

    // Whether the process with the given ID is running
    static boolean isProcessAlive(long pid) {
        return ProcessHandle.of(pid)
                .map(ProcessHandle::isAlive)
                .orElse(false);
    }

    // ID of the process that owns the given workspace directory, or empty for anything that is not a workspace directory
    static OptionalLong ownerPidOf(Path entry) {
        Matcher matcher = WORKSPACE_DIR_NAME.matcher(entry.getFileName().toString());
        if (!matcher.matches() || !Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(Long.parseLong(matcher.group(1)));
    }

    private static void deleteStaleWorkspaceDirectories(Path rootDir, Path workspaceDir, LongPredicate isProcessAlive) {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(rootDir)) {
            for (Path entry : entries) {
                OptionalLong ownerPid = ownerPidOf(entry);
                if (ownerPid.isEmpty() || entry.equals(workspaceDir) || isProcessAlive.test(ownerPid.getAsLong())) {
                    continue;
                }

                log.info("Delete the workspace directory left behind by process {}: {}", ownerPid.getAsLong(), entry);
                delete(entry);
            }

        } catch (IOException e) {
            log.warn("Failed to look for workspace directories left behind in {}", rootDir, e);
        }
    }

    private static void delete(Path path) {
        try {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                FileUtils.deleteDirectory(path.toFile());
            } else {
                Files.deleteIfExists(path);
            }

        } catch (IOException e) {
            log.warn("Failed to delete {}", path, e);
        }
    }
}
