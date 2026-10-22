package com.astahpromcp.tool.projectdiff.export;

import com.astahpromcp.tool.astah.pro.project.XmiExportMain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubprocessXmiExporterTest {

    @TempDir
    Path dir;

    @Test
    void writeMainClass_ok_copiesTheClassFileWhereTheClassPathFindsIt() throws Exception {
        SubprocessXmiExporter.writeMainClass(dir, XmiExportMain.class);

        Path written = dir.resolve(XmiExportMain.class.getName().replace('.', '/') + ".class");
        byte[] bytes = Files.readAllBytes(written);
        assertEquals((byte) 0xCA, bytes[0]);
        assertEquals((byte) 0xFE, bytes[1]);
    }

    @Test
    void installDirectory_ok_dropsTheSlashBeforeAWindowsDrive() {
        Path path = SubprocessXmiExporter.installDirectory("/C:/Program Files/astah-professional/");
        if (File.separatorChar == '\\') {
            assertEquals(Path.of("C:/Program Files/astah-professional"), path);
        } else {
            assertEquals(Path.of("/C:/Program Files/astah-professional/"), path);
        }
    }

    @Test
    void command_ok_namesEveryPathOfTheJobRelativeToItsWorkingDirectory() {
        SubprocessXmiExporter.Plan plan = new SubprocessXmiExporter.Plan(Path.of("java"), Path.of("astah-pro.jar"), Path.of("astah-api.jar"), null, XmiExportMain.class);

        List<String> command = SubprocessXmiExporter.command(plan, "512m", 60);

        assertTrue(command.contains("-Duser.home=home"));
        assertTrue(command.contains("-Djava.io.tmpdir=tmp"));
        assertTrue(command.contains("-Xmx512m"));
        assertFalse(command.stream().anyMatch(argument -> argument.contains("headless")));
        assertTrue(command.get(command.indexOf("-cp") + 1).endsWith(File.pathSeparator + "classes"));
        assertEquals(List.of(XmiExportMain.class.getName(), "project.asta", "out.xmi", "info.json", "60"),
                command.subList(command.size() - 5, command.size()));
    }

    @Test
    void unencodable_ok_findsWhatTheSystemCodePageCannotHold() {
        Charset sjis = Charset.forName("windows-31j");

        assertEquals(List.of(), SubprocessXmiExporter.unencodable(List.of("C:/日本語/a.jar"), sjis));
        assertEquals(List.of("C:/한국어/a.jar"), SubprocessXmiExporter.unencodable(List.of("C:/한국어/a.jar", "ascii"), sjis));
        assertEquals(List.of(), SubprocessXmiExporter.unencodable(List.of("C:/한국어/a.jar"), StandardCharsets.UTF_8));
    }
}
