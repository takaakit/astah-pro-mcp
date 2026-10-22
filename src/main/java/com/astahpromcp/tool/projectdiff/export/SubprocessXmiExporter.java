package com.astahpromcp.tool.projectdiff.export;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

// Writes the XMI of a project file in a separate process on Astah's own Java runtime, rather than by opening the file in
// Astah, which would replace the project the user has open.
@Slf4j
public final class SubprocessXmiExporter {

    public static final String PROJECT_FILE_NAME = "project.asta";
    public static final String XMI_FILE_NAME = "out.xmi";
    public static final String INFORMATION_FILE_NAME = "info.json";
    public static final String LOG_FILE_NAME = "child.log";

    // The process has a home directory and a temporary directory of its own, so that it neither changes the user's
    // settings nor leaves files behind
    private static final String HOME_DIRECTORY = "home";
    private static final String TEMPORARY_DIRECTORY = "tmp";
    private static final String CLASSES_DIRECTORY = "classes";

    // Environment variables that a Java runtime reads options from. Set for Astah, they would apply to the process too,
    // and could switch on headless mode, for instance.
    private static final List<String> JAVA_OPTION_VARIABLES = List.of("JAVA_TOOL_OPTIONS", "_JAVA_OPTIONS", "JDK_JAVA_OPTIONS");

    // The exit code of a process that wrote the XMI
    private static final int EXIT_OK = 0;

    private static final long POLL_MILLIS = 200;

    // How much of the end of a process's log an error message quotes
    private static final int LOG_TAIL_BYTES = 1_500;

    // How to start the process: the Java runtime, the Astah jars, and the main class that writes the XMI
    public record Plan(Path javaExecutable, Path astahProJar, Path astahApiJar, Path libraryDirectory, Class<?> mainClass) {
    }

    // A process that is writing an XMI
    public record Running(Process process, Path workDirectory, long deadlineNanos) {
    }

    private SubprocessXmiExporter() {
    }

    // astahInstallPath is what ProjectAccessor.getAstahInstallPath() reports
    public static Plan plan(String javaHome, String astahInstallPath, Class<?> mainClass) {
        Path javaExecutable = Path.of(javaHome, "bin", File.separatorChar == '\\' ? "java.exe" : "java");
        if (!Files.isRegularFile(javaExecutable)) {
            throw new IllegalStateException("The Java runtime Astah runs on has no java executable at " + javaExecutable + ".");
        }

        Path install = installDirectory(astahInstallPath);
        Path astahProJar = install.resolve("astah-pro.jar");
        Path astahApiJar = install.resolve("astah-api.jar");
        if (!Files.isRegularFile(astahProJar) || !Files.isRegularFile(astahApiJar)) {
            throw new IllegalStateException("Astah's installation at " + install + " does not have astah-pro.jar and astah-api.jar, which writing the XMI of a project file needs.");
        }

        Path library = install.resolve("lib").resolve("rlm");
        return new Plan(javaExecutable, astahProJar, astahApiJar, Files.isDirectory(library) ? library : null, mainClass);
    }

    // ProjectAccessor.getAstahInstallPath() reports a path such as "/C:/Program Files/astah-professional/" on Windows.
    // Package-private so that a test can check it on a Windows path on any platform.
    static Path installDirectory(String reported) {
        String path = reported;
        if (File.separatorChar == '\\' && path.length() >= 3 && path.charAt(0) == '/' && path.charAt(2) == ':') {
            path = path.substring(1);
        }
        return Path.of(path);
    }

    // The command that starts the process. Every path of the job is relative to its working directory, because the Java
    // launcher reads the command line in the system code page, which cannot hold every character a path may have.
    // Package-private so that a test can check the command without starting it.
    static List<String> command(Plan plan, String maxHeap, long timeoutSeconds) {
        List<String> command = new ArrayList<>();
        command.add(plan.javaExecutable().toString());
        command.add("-Xmx" + maxHeap);
        command.add("-Duser.home=" + HOME_DIRECTORY);
        command.add("-Djava.io.tmpdir=" + TEMPORARY_DIRECTORY);
        if (plan.libraryDirectory() != null) {
            command.add("-Djava.library.path=" + plan.libraryDirectory());
        }
        command.add("-cp");
        command.add(plan.astahProJar() + File.pathSeparator + plan.astahApiJar() + File.pathSeparator + CLASSES_DIRECTORY);
        command.add(plan.mainClass().getName());
        command.add(PROJECT_FILE_NAME);
        command.add(XMI_FILE_NAME);
        command.add(INFORMATION_FILE_NAME);
        command.add(Long.toString(timeoutSeconds));
        return command;
    }

    // The values of the command that the system code page cannot hold. Those would reach the process as '?'.
    // Package-private so that a test can check it with a code page of its choice.
    static List<String> unencodable(List<String> command, Charset charset) {
        CharsetEncoder encoder = charset.newEncoder();
        List<String> found = new ArrayList<>();
        for (String value : command) {
            if (!encoder.canEncode(value)) {
                found.add(value);
            }
        }
        return found;
    }

    private static Charset commandLineCharset() {
        String name = System.getProperty("sun.jnu.encoding");
        try {
            return name == null ? Charset.defaultCharset() : Charset.forName(name);
        } catch (Exception e) {
            return Charset.defaultCharset();
        }
    }

    // Check, before anything is copied, that a process can be started at all from this installation
    public static void checkCommandLine(Plan plan) {
        List<String> found = unencodable(command(plan, McpServerConfig.PROJECT_DIFF_CHILD_MAX_HEAP, McpServerConfig.PROJECT_DIFF_EXPORT_TIMEOUT_SECONDS), commandLineCharset());
        if (!found.isEmpty()) {
            throw new IllegalStateException("The XMI of a project file is written by a separate Java process, but the path of Astah's installation or of its Java runtime has characters the system code page ("
                    + commandLineCharset().name() + ") cannot hold, so the process could not be started: " + found);
        }
    }

    // Prepare the work directory, which already holds the project file as project.asta, for the process
    public static void prepare(Path workDirectory, Plan plan) {
        try {
            Files.createDirectories(workDirectory.resolve(HOME_DIRECTORY));
            Files.createDirectories(workDirectory.resolve(TEMPORARY_DIRECTORY));
            writeMainClass(workDirectory.resolve(CLASSES_DIRECTORY), plan.mainClass());

        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare the work directory " + workDirectory + ": " + e.getMessage(), e);
        }
    }

    public static Running start(Path workDirectory, Plan plan, ProcessLauncher launcher) {
        checkCommandLine(plan);

        ProcessBuilder builder = new ProcessBuilder(command(plan, McpServerConfig.PROJECT_DIFF_CHILD_MAX_HEAP, McpServerConfig.PROJECT_DIFF_EXPORT_TIMEOUT_SECONDS))
                .directory(workDirectory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(workDirectory.resolve(LOG_FILE_NAME).toFile());
        for (String variable : JAVA_OPTION_VARIABLES) {
            builder.environment().remove(variable);
        }

        try {
            Process process = launcher.start(builder);
            log.debug("Started the process {} that writes the XMI in {}", process.pid(), workDirectory);
            return new Running(process, workDirectory, System.nanoTime() + TimeUnit.SECONDS.toNanos(McpServerConfig.PROJECT_DIFF_EXPORT_TIMEOUT_SECONDS));

        } catch (IOException e) {
            throw new IllegalStateException("Failed to start the process that writes the XMI: " + e.getMessage(), e);
        }
    }

    // Wait for the process to write the XMI, and return what it reports. The process is stopped when the time runs out,
    // when its log or its XMI grows beyond the limit, or when the comparison is stopped.
    public static ExportInformation await(Running running, ProcessLauncher launcher, BooleanSupplier stopRequested) throws InterruptedException {
        Process process = running.process();
        Path work = running.workDirectory();
        try {
            while (!process.waitFor(POLL_MILLIS, TimeUnit.MILLISECONDS)) {
                if (stopRequested.getAsBoolean()) {
                    throw new IllegalStateException("The comparison was stopped.");
                }
                if (System.nanoTime() > running.deadlineNanos()) {
                    throw new IllegalStateException("Writing the XMI took longer than " + McpServerConfig.PROJECT_DIFF_EXPORT_TIMEOUT_SECONDS + " seconds.");
                }
                if (sizeOf(work.resolve(LOG_FILE_NAME)) > McpServerConfig.PROJECT_DIFF_CHILD_LOG_MAX_BYTES) {
                    throw new IllegalStateException("The process that writes the XMI wrote more log than allowed.");
                }
                if (sizeOf(work.resolve(XMI_FILE_NAME + ".part")) > McpServerConfig.PROJECT_DIFF_MAX_XMI_BYTES) {
                    throw new IllegalStateException(String.format("The XMI grew beyond %,d bytes, which is more than this server compares.", McpServerConfig.PROJECT_DIFF_MAX_XMI_BYTES));
                }
            }

            int exit = process.exitValue();
            Path xmi = work.resolve(XMI_FILE_NAME);
            if (exit != EXIT_OK || !Files.isRegularFile(xmi)) {
                throw new IllegalStateException("The process that writes the XMI ended with code " + exit + " without writing it. Its log ends with: " + tail(work.resolve(LOG_FILE_NAME)));
            }

            try {
                return JsonSupport.OBJ_MAPPER.readValue(Files.readString(work.resolve(INFORMATION_FILE_NAME), StandardCharsets.UTF_8), ExportInformation.class);

            } catch (IOException e) {
                throw new IllegalStateException("Failed to read what the process that writes the XMI reported: " + e.getMessage(), e);
            }

        } finally {
            stop(process);
            launcher.finished(process);
        }
    }

    // Give up on a process that nobody will wait for any more, because the comparison failed before it got to it
    public static void abandon(Running running, ProcessLauncher launcher) {
        stop(running.process());
        launcher.finished(running.process());
    }

    // Stop the process if it is still running, and wait for it to end
    private static void stop(Process process) {
        if (!process.isAlive()) {
            return;
        }
        process.destroyForcibly();
        try {
            if (!process.waitFor(McpServerConfig.PROJECT_DIFF_STOP_WAIT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("The process {} that writes an XMI did not end after it was stopped", process.pid());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Copy the class file of the main class to where the process finds it. The plugin is an OSGi bundle, whose classes
    // are not files on disk that a class path could name, so the class is read through the class loader instead.
    // Package-private so that a test can check the copy without starting a process.
    static void writeMainClass(Path classesDirectory, Class<?> mainClass) throws IOException {
        String fileName = mainClass.getSimpleName() + ".class";
        Path target = classesDirectory.resolve(mainClass.getPackageName().replace('.', '/')).resolve(fileName);
        Files.createDirectories(target.getParent());
        try (InputStream in = mainClass.getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IOException("The class file " + fileName + " is not readable through the plugin's class loader");
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static long sizeOf(Path file) {
        try {
            return Files.exists(file) ? Files.size(file) : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    // The end of a log, for an error message
    private static String tail(Path logFile) {
        try (RandomAccessFile file = new RandomAccessFile(logFile.toFile(), "r")) {
            long length = file.length();
            int size = (int) Math.min(length, LOG_TAIL_BYTES);
            byte[] bytes = new byte[size];
            file.seek(length - size);
            file.readFully(bytes);
            return new String(bytes, commandLineCharset()).strip();
        } catch (IOException e) {
            return "(no log)";
        }
    }
}
