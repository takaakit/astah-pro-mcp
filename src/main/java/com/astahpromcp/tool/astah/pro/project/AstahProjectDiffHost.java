package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.projectdiff.ProjectDiffHost;
import com.astahpromcp.tool.projectdiff.export.OpenProjectSnapshot;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import java.nio.file.Path;

// What preparing a comparison needs from Astah (ProjectDiffHost), taken from the running Astah.
// It is used only while ProjectDiffStartTool prepares a comparison, on the thread that holds the Astah API lock.
public final class AstahProjectDiffHost implements ProjectDiffHost {

    private final ProjectAccessor projectAccessor;
    private final CurrentProjectXmiExporter currentProjectXmiExporter;

    public AstahProjectDiffHost(ProjectAccessor projectAccessor) {
        this.projectAccessor = projectAccessor;
        this.currentProjectXmiExporter = new CurrentProjectXmiExporter(projectAccessor);
    }

    @Override
    public String astahInstallPath() {
        return projectAccessor.getAstahInstallPath();
    }

    @Override
    public Class<?> xmiExportMainClass() {
        return XmiExportMain.class;
    }

    @Override
    public OpenProjectSnapshot captureOpenProject(Path xmiFile) {
        return currentProjectXmiExporter.capture(xmiFile);
    }
}
