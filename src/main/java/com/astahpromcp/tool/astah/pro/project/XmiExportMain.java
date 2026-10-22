package com.astahpromcp.tool.astah.pro.project;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

// The main class of the separate process that writes the XMI of a project file (see SubprocessXmiExporter).
// The process has only the Astah jars and this one class file on its class path, so this class has no nested or anonymous class.
public final class XmiExportMain {

    // The XMI was written
    private static final int EXIT_OK = 0;
    // The XMI was not written
    private static final int EXIT_FAILED = 2;
    // The parent process went away. Package-private so that a test can tell that the process stopped for this reason.
    static final int EXIT_PARENT_GONE = 3;
    // The time this process may run ran out
    private static final int EXIT_TIMED_OUT = 4;

    private static volatile long deadlineNanos;

    private XmiExportMain() {
    }

    // Arguments, every path relative to the working directory, so that the command line holds ASCII only:
    //   <project file> <XMI to write> <information file to write> <seconds this process may run>
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: XmiExportMain <project file> <XMI to write> <information file to write> <seconds>");
            Runtime.getRuntime().halt(EXIT_FAILED);
        }

        deadlineNanos = System.nanoTime() + Long.parseLong(args[3]) * 1_000_000_000L;
        startDaemon(XmiExportMain::watchParent, "parent-watch");
        startDaemon(XmiExportMain::watchDeadline, "deadline-watch");

        int code = EXIT_FAILED;
        ProjectAccessor projectAccessor = null;
        try {
            // Astah resolves a relative path against its own idea of the current directory, and exportXMI() cannot write
            // to a path that has no parent directory, so every path is made absolute here, where it is still Unicode.
            Path project = Path.of(args[0]).toAbsolutePath();
            Path xmi = Path.of(args[1]).toAbsolutePath();
            Path info = Path.of(args[2]).toAbsolutePath();
            Path part = xmi.resolveSibling(xmi.getFileName() + ".part");

            projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
            projectAccessor.open(project.toString(), false, false, true);

            // Read before writing, so that it describes the project as loaded. Printed as well, because standard output
            // is the log that is kept, while the information file is deleted afterwards and is not written if export fails.
            String information = information(projectAccessor);
            System.out.println("Export information: " + information);

            projectAccessor.exportXMI(part.toString());
            Files.move(part, xmi, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(info, information, StandardCharsets.UTF_8);
            code = EXIT_OK;

        } catch (Throwable t) {
            t.printStackTrace();

        } finally {
            if (projectAccessor != null) {
                try {
                    projectAccessor.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        System.out.flush();
        System.err.flush();
        // Astah leaves threads running (AWT among them), so the process ends here rather than when they do
        Runtime.getRuntime().halt(code);
    }

    // The project and the Java runtime it is written on, as the JSON of ExportInformation.
    static String information(ProjectAccessor projectAccessor) throws Exception {
        StringBuilder json = new StringBuilder();
        json.append('{');
        json.append("\"projectModelVersion\":").append(projectAccessor.getProjectModelVersion()).append(',');
        json.append("\"astahModelVersion\":").append(projectAccessor.getAstahModelVersion()).append(',');
        json.append("\"astahVersion\":").append(quote(projectAccessor.getAstahVersion())).append(',');
        json.append("\"referenceProjects\":[");
        String[] paths = projectAccessor.getReferenceProjectPaths();
        for (int i = 0; i < paths.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"path\":").append(quote(paths[i]))
                .append(",\"status\":").append(quote(referenceStatus(projectAccessor, paths[i]))).append('}');
        }
        json.append("],");
        json.append("\"runtime\":{");
        json.append("\"javaHome\":").append(quote(System.getProperty("java.home"))).append(',');
        json.append("\"javaVersion\":").append(quote(System.getProperty("java.version"))).append(',');
        json.append("\"javaVendor\":").append(quote(System.getProperty("java.vendor"))).append(',');
        json.append("\"osName\":").append(quote(System.getProperty("os.name") + " " + System.getProperty("os.version"))).append(',');
        json.append("\"locale\":").append(quote(Locale.getDefault().toLanguageTag())).append(',');
        json.append("\"defaultCharset\":").append(quote(Charset.defaultCharset().name())).append(',');
        json.append("\"commandLineEncoding\":").append(quote(System.getProperty("sun.jnu.encoding"))).append(',');
        json.append("\"headless\":").append(GraphicsEnvironment.isHeadless());
        json.append("}}");
        return json.toString();
    }

    // The JSON value of ReferenceProjectStatus
    private static String referenceStatus(ProjectAccessor projectAccessor, String path) {
        try {
            int status = projectAccessor.getReferenceProjectStatus(path);
            if (status == ProjectAccessor.REFERENCE_PROJECT_STATUS_LATEST) {
                return "latest";
            }
            if (status == ProjectAccessor.REFERENCE_PROJECT_STATUS_NEED_UPDATE) {
                return "need_update";
            }
            if (status == ProjectAccessor.REFERENCE_PROJECT_STATUS_NOT_FOUND) {
                return "not_found";
            }
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    // A JSON string. Package-private so that a test can check it on characters that need escaping.
    static String quote(String value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder quoted = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                quoted.append('\\').append(c);
            } else if (c < 0x20) {
                quoted.append(String.format("\\u%04x", (int) c));
            } else {
                quoted.append(c);
            }
        }
        return quoted.append('"').toString();
    }

    private static void startDaemon(Runnable task, String name) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
    }

    // The parent keeps this process's standard input open. When the input ends, the parent has gone, perhaps without
    // getting the chance to stop this process, so the process stops itself rather than run on unattended.
    private static void watchParent() {
        try {
            InputStream in = System.in;
            while (in.read() >= 0) {
                // Nothing is ever sent; reading only waits for the end
            }
        } catch (IOException e) {
            // A broken pipe means the same as the end of the input
        }
        System.err.println("The parent process went away; stopping.");
        Runtime.getRuntime().halt(EXIT_PARENT_GONE);
    }

    // The parent stops this process when its time runs out; this is for when the parent cannot
    private static void watchDeadline() {
        while (true) {
            long remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0) {
                break;
            }
            try {
                Thread.sleep(Math.max(1, remaining / 1_000_000));
            } catch (InterruptedException e) {
                // Keep waiting: only the deadline ends this thread
            }
        }
        System.err.println("The time for writing the XMI ran out; stopping.");
        Runtime.getRuntime().halt(EXIT_TIMED_OUT);
    }
}
