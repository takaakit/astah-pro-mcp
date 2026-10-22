package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ProjectSideDTO(
    @JsonPropertyDescription("file or open_project")
    ProjectKind kind,

    @JsonPropertyDescription("Path of the project file; empty for an open project never saved")
    String path,

    @JsonPropertyDescription("Project name")
    String projectName,

    @JsonPropertyDescription("xmi.id of the root model of the open project; empty for a file")
    String projectRootId,

    @JsonPropertyDescription("Time reading the open project started (ISO 8601); empty for a file")
    String captureStartedAt,

    @JsonPropertyDescription("Time reading the open project completed (ISO 8601); empty for a file")
    String captureCompletedAt,

    @JsonPropertyDescription("Version of the Astah that wrote the XMI; empty until known")
    String astahVersion,

    @JsonPropertyDescription("Model version the project was saved with; -1 until known")
    int projectModelVersion,

    @JsonPropertyDescription("Referenced projects")
    List<ReferenceProjectDTO> referenceProjects
) {
}
