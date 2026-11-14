package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.projectdiff.export.OpenProjectSnapshot;

import java.nio.file.Path;

// What preparing a comparison needs from Astah, which this package cannot reach itself. The Astah tools implement it, as they do McpToolScriptHost.
public interface ProjectDiffHost {

    // The folder Astah is installed in, whose jars the separate process that writes the XMI of a project file runs on
    String astahInstallPath();

    // The main class of that separate process. It uses the Astah API, so it is given here rather than named by this package.
    Class<?> xmiExportMainClass();

    // Write the project open in Astah to xmiFile, and return what the comparison needs to know about it
    OpenProjectSnapshot captureOpenProject(Path xmiFile) throws Exception;
}
