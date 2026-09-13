package com.astahpromcp.tool.mcptoolscript;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// Checks, from a separate JVM, that a script cannot shut Astah down.
//
// Nashorn's exit() and quit() end the JVM outright, and a ClassFilter does not touch them because they are not
// classes. Left reachable, one line in a script would close Astah and take the user's unsaved work with it.
// The check has to run in its own process: if it failed in the test JVM the run would not report a failure, it
// would simply disappear.
public class McpToolScriptExecutorIsolationTest {

    @Test
    void execute_ok_cannotTerminateTheJvm() throws Exception {
        assertSurvives("exit(" + ExitCanaryRunner.CANARY_EXIT_CODE + ");");
    }

    @Test
    void execute_ok_cannotTerminateTheJvmThroughQuit() throws Exception {
        assertSurvives("quit(" + ExitCanaryRunner.CANARY_EXIT_CODE + ");");
    }

    // The route a name-by-name denial list misses: a second engine built through the injected 'engine' global has no ClassFilter, so anything reachable from it is reachable from the script.
    @Test
    void execute_ok_cannotTerminateTheJvmThroughASecondEngine() throws Exception {
        assertSurvives("engine.getFactory().getScriptEngine()"
                + ".eval(\"Java.type('java.lang.Runtime').getRuntime().halt("
                + ExitCanaryRunner.CANARY_EXIT_CODE + ")\");");
    }

    private static void assertSurvives(String script) throws Exception {
        Path outputFile = Files.createTempFile("astah-mcp-exit-canary", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(ExitCanaryRunner.class.getName());
            command.add(script);

            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(outputFile.toFile())
                    .start();

            assertTrue(process.waitFor(120, TimeUnit.SECONDS), "The canary process did not finish in time");

            int exitCode = process.exitValue();
            String output = Files.readString(outputFile, StandardCharsets.UTF_8);

            assertNotEquals(ExitCanaryRunner.CANARY_EXIT_CODE, exitCode,
                    "The script shut the JVM down, so a script on this port could close Astah. Output: " + output);
            assertEquals(0, exitCode, "The canary process ended unexpectedly. Output: " + output);
            assertTrue(output.contains(ExitCanaryRunner.SURVIVED_PREFIX),
                    "The canary process did not report back. Output: " + output);
            assertTrue(output.contains(ExitCanaryRunner.SURVIVED_PREFIX + "false"),
                    "The script should have failed rather than succeeded. Output: " + output);

        } finally {
            Files.deleteIfExists(outputFile);
        }
    }
}
