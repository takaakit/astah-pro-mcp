package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import com.astahpromcp.tool.projectdiff.export.ExportInformation;
import com.astahpromcp.tool.projectdiff.export.SubprocessXmiExporter;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class XmiExportMainTest {

    private static final String MODEL = "src/test/resources/modelfile/model/ClassToolTest.asta";

    @TempDir
    Path dir;

    // The separate process gets this one class file and nothing else of the plugin, so the class must not have nested
    // classes: a nested, anonymous or local class, or an enum switch, would be a class file of its own that it lacks.
    @Test
    void xmiExportMain_ok_isOneClassFile() {
        String prefix = XmiExportMain.class.getSimpleName() + "$";
        for (int i = 1; i <= 5; i++) {
            assertNull(XmiExportMain.class.getResource(prefix + i + ".class"), prefix + i);
        }
        assertEquals(0, XmiExportMain.class.getDeclaredClasses().length);
    }

    @Test
    void information_ok_reportsTheProjectAndTheRuntime() throws Exception {
        ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
        projectAccessor.open(Path.of(MODEL).toAbsolutePath().toString(), false, false, true);
        try {
            ExportInformation information = JsonSupport.OBJ_MAPPER.readValue(XmiExportMain.information(projectAccessor), ExportInformation.class);

            assertTrue(information.projectModelVersion() > 0);
            assertNotNull(information.runtime());
            assertEquals(System.getProperty("java.home"), information.runtime().javaHome());
            assertEquals(System.getProperty("java.version"), information.runtime().javaVersion());
            assertEquals(Charset.defaultCharset().name(), information.runtime().defaultCharset());
            assertFalse(information.runtime().locale().isEmpty());
        } finally {
            projectAccessor.close();
        }
    }

    @Test
    void quote_ok_writesValidJsonStrings() {
        assertEquals("\"a\\\"b\\\\c\\u000a日本\"", XmiExportMain.quote("a\"b\\c\n日本"));
        assertEquals("\"\"", XmiExportMain.quote(null));
    }

    // When Astah goes away without stopping the process, the pipe to the process's standard input closes, as it does here.
    // The work folder has characters that the system code page may not hold.
    @Test
    void main_ok_stopsItselfWhenItsParentGoesAway() throws Exception {
        Path work = dir.resolve("作業 é 한").resolve("orphan");
        Files.createDirectories(work);
        Files.copy(Path.of(MODEL), work.resolve(SubprocessXmiExporter.PROJECT_FILE_NAME));
        ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
        SubprocessXmiExporter.Plan plan = SubprocessXmiExporter.plan(System.getProperty("java.home"), projectAccessor.getAstahInstallPath(), XmiExportMain.class);
        ProjectDiffJobs projectDiffJobs = new ProjectDiffJobs(dir.resolve("project-diff"));

        SubprocessXmiExporter.prepare(work, plan);
        SubprocessXmiExporter.Running running = SubprocessXmiExporter.start(work, plan, projectDiffJobs);
        running.process().getOutputStream().close();

        assertTrue(running.process().waitFor(30, TimeUnit.SECONDS));
        assertEquals(XmiExportMain.EXIT_PARENT_GONE, running.process().exitValue());
        projectDiffJobs.finished(running.process());
        assertTrue(projectDiffJobs.shutdown());
    }
}
