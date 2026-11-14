package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.projectdiff.export.ExportInformation;
import com.astahpromcp.tool.projectdiff.export.OpenProjectSnapshot;
import com.astahpromcp.tool.projectdiff.export.SubprocessXmiExporter;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Preparing a comparison without Astah: a host stands in for it, and only an installation with the two jars the plan looks for is made.
public class ProjectDiffPreparationTest {

    @TempDir
    Path root;

    private ProjectDiffJobs jobs;
    private RecordingHost host;
    private Path base;

    // Stands in for Astah, and records which of its operations were used
    private static final class RecordingHost implements ProjectDiffHost {

        private final String installPath;
        private final List<String> calls = new ArrayList<>();
        private RuntimeException captureFailure;

        RecordingHost(String installPath) {
            this.installPath = installPath;
        }

        @Override
        public String astahInstallPath() {
            calls.add("astahInstallPath");
            return installPath;
        }

        @Override
        public Class<?> xmiExportMainClass() {
            calls.add("xmiExportMainClass");
            return ProjectDiffPreparationTest.class;
        }

        @Override
        public OpenProjectSnapshot captureOpenProject(Path xmiFile) throws Exception {
            calls.add("captureOpenProject " + xmiFile);
            if (captureFailure != null) {
                throw captureFailure;
            }
            Files.writeString(xmiFile, "<xmi/>");
            Instant now = Instant.now();
            return new OpenProjectSnapshot("root-id", "Open", "C:\\work\\Open.asta", now, now, new ExportInformation(1, 1, "10.0", List.of(), null));
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Path install = Files.createDirectories(root.resolve("astah"));
        Files.createFile(install.resolve("astah-pro.jar"));
        Files.createFile(install.resolve("astah-api.jar"));

        jobs = new ProjectDiffJobs(root.resolve("project-diff"));
        host = new RecordingHost(install.toString());
        base = Files.writeString(root.resolve("Base.asta"), "base");
    }

    @Test
    void prepare_ok_copiesBothProjectFilesWithoutCapturingTheOpenProject() throws Exception {
        Path target = Files.writeString(root.resolve("Target.asta"), "target");
        ProjectDiffJob job = jobs.admit("", "a\nb").job();

        Runnable rest = new ProjectDiffPreparation(jobs, host, base, target).prepare(job);

        assertInstanceOf(ProjectDiffRunner.class, rest);
        assertEquals("base", Files.readString(ProjectDiffStore.baseOf(job.directory()).resolve(SubprocessXmiExporter.PROJECT_FILE_NAME)));
        assertEquals("target", Files.readString(ProjectDiffStore.targetOf(job.directory()).resolve(SubprocessXmiExporter.PROJECT_FILE_NAME)));
        assertEquals(ProjectKind.FILE, job.base().kind());
        assertEquals(ProjectKind.FILE, job.target().kind());
        assertEquals(List.of("astahInstallPath", "xmiExportMainClass"), host.calls);

        jobs.discard(job);
        assertTrue(jobs.shutdown());
    }

    @Test
    void prepare_ok_capturesTheOpenProjectThroughTheHostIntoTheTargetFolder() throws Exception {
        ProjectDiffJob job = jobs.admit("", "a\n(the open project)").job();

        new ProjectDiffPreparation(jobs, host, base, null).prepare(job);

        Path xmi = ProjectDiffStore.targetOf(job.directory()).resolve(SubprocessXmiExporter.XMI_FILE_NAME);
        assertEquals(List.of("astahInstallPath", "xmiExportMainClass", "captureOpenProject " + xmi), host.calls);
        assertEquals("<xmi/>", Files.readString(xmi));
        assertEquals("base", Files.readString(ProjectDiffStore.baseOf(job.directory()).resolve(SubprocessXmiExporter.PROJECT_FILE_NAME)));
        assertEquals(ProjectKind.FILE, job.base().kind());
        assertEquals(ProjectKind.OPEN_PROJECT, job.target().kind());
        assertEquals("C:\\work\\Open.asta", job.target().path());

        jobs.discard(job);
        assertTrue(jobs.shutdown());
    }

    // A failure while preparing leaves no comparison behind, and an equal request may start one again
    @Test
    void startComparison_ng_keepsNothingWhenTheHostFailsToCapture() throws Exception {
        host.captureFailure = new IllegalStateException("capture failed");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> jobs.startComparison("req-1", base, null, host));

        assertEquals("capture failed", e.getMessage());
        assertNull(jobs.earlier("req-1", base, null));
        try (Stream<Path> left = Files.list(root.resolve("project-diff"))) {
            assertEquals(List.of(), left.toList());
        }
        assertTrue(jobs.shutdown());
    }
}
