package com.astahpromcp.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class WorkspaceDirectoriesTest {

    private static final long OWN_PID = 100;

    @TempDir
    Path rootDir;

    private static LongPredicate aliveAre(Long... pids) {
        Set<Long> alive = Set.of(pids);
        return alive::contains;
    }

    private Path workspaceDirWithFile(String name) throws IOException {
        Path dir = Files.createDirectory(rootDir.resolve(name));
        Files.writeString(dir.resolve("uml-modeling-architecture-insights.md"), "left behind");
        return dir;
    }

    @Test
    void prepare_ok_createsTheWorkspaceDirectoryAndItsParent() throws Exception {
        Path workspaceDir = rootDir.resolve("not-yet-created").resolve("workspace-" + OWN_PID);

        WorkspaceDirectories.prepare(workspaceDir, aliveAre(OWN_PID));

        assertTrue(Files.isDirectory(workspaceDir));
    }

    @Test
    void prepare_ok_emptiesTheWorkspaceDirectoryLeftBehindUnderTheSameProcessId() throws Exception {
        Path workspaceDir = workspaceDirWithFile("workspace-" + OWN_PID);

        WorkspaceDirectories.prepare(workspaceDir, aliveAre(OWN_PID));

        assertTrue(Files.isDirectory(workspaceDir));
        try (var entries = Files.list(workspaceDir)) {
            assertEquals(0, entries.count(), "Files of an earlier session must not be carried over");
        }
    }

    @Test
    void prepare_ok_deletesTheWorkspaceDirectoriesOfProcessesThatNoLongerExist() throws Exception {
        Path stale = workspaceDirWithFile("workspace-200");

        WorkspaceDirectories.prepare(rootDir.resolve("workspace-" + OWN_PID), aliveAre(OWN_PID));

        assertFalse(Files.exists(stale));
    }

    @Test
    void prepare_ok_keepsTheWorkspaceDirectoriesOfProcessesThatAreRunning() throws Exception {
        Path running = workspaceDirWithFile("workspace-300");

        WorkspaceDirectories.prepare(rootDir.resolve("workspace-" + OWN_PID), aliveAre(OWN_PID, 300L));

        assertTrue(Files.exists(running.resolve("uml-modeling-architecture-insights.md")));
    }

    @Test
    void prepare_ok_leavesAloneWhatIsNotAWorkspaceDirectory() throws Exception {
        // The directory of earlier versions, which may still be in use by one of them
        Path legacy = workspaceDirWithFile("workspace");
        Path otherName = workspaceDirWithFile("workspace-old");
        Path tooLongId = workspaceDirWithFile("workspace-1234567890123456789");
        Path file = Files.writeString(rootDir.resolve("workspace-400"), "not a directory");
        Path log = Files.writeString(rootDir.resolve("astah-pro-mcp.log"), "log");

        WorkspaceDirectories.prepare(rootDir.resolve("workspace-" + OWN_PID), aliveAre(OWN_PID));

        assertTrue(Files.exists(legacy.resolve("uml-modeling-architecture-insights.md")));
        assertTrue(Files.exists(otherName.resolve("uml-modeling-architecture-insights.md")));
        assertTrue(Files.exists(tooLongId.resolve("uml-modeling-architecture-insights.md")));
        assertTrue(Files.exists(file));
        assertTrue(Files.exists(log));
    }

    @Test
    void prepare_ok_neverFollowsASymbolicLinkNamedLikeAWorkspaceDirectory(@TempDir Path elsewhere) throws Exception {
        Path target = Files.writeString(elsewhere.resolve("important.txt"), "must survive");
        Path link = rootDir.resolve("workspace-500");
        try {
            Files.createSymbolicLink(link, elsewhere);
        } catch (IOException | UnsupportedOperationException e) {
            // Creating a symbolic link needs a privilege on Windows
            assumeTrue(false, "Symbolic links cannot be created here: " + e);
        }

        WorkspaceDirectories.prepare(rootDir.resolve("workspace-" + OWN_PID), aliveAre(OWN_PID));

        assertTrue(Files.exists(target));
    }

    @Test
    void ownerPidOf_ok_readsTheProcessIdFromTheName() throws Exception {
        Path dir = Files.createDirectory(rootDir.resolve("workspace-8804"));

        assertEquals(OptionalLong.of(8804), WorkspaceDirectories.ownerPidOf(dir));
    }

    @Test
    void isProcessAlive_ok_isTrueForThisProcess() {
        assertTrue(WorkspaceDirectories.isProcessAlive(ProcessHandle.current().pid()));
    }

    @Test
    void isProcessAlive_ok_isFalseForAProcessThatHasEnded() throws Exception {
        String java = ProcessHandle.current().info().command().orElse(null);
        assumeTrue(java != null, "The command of this process is not known");

        Process process = new ProcessBuilder(java, "-version").redirectErrorStream(true).start();
        process.getInputStream().transferTo(OutputStream.nullOutputStream());
        assertTrue(process.waitFor(30, TimeUnit.SECONDS));

        assertFalse(WorkspaceDirectories.isProcessAlive(process.pid()));
    }
}
