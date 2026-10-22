package com.astahpromcp.tool.projectdiff.export;

import com.astahpromcp.tool.projectdiff.outputdto.ReferenceProjectDTO;

import java.util.List;

// What the side that wrote an XMI reports about the project and the runtime it ran on.
public record ExportInformation(
    int projectModelVersion,
    int astahModelVersion,
    String astahVersion,
    List<ReferenceProjectDTO> referenceProjects,
    RuntimeInformation runtime
) {

    // Whether the project file was saved by an older Astah, so that Astah converted it on loading
    public boolean isOlderFormat() {
        return projectModelVersion >= 0 && projectModelVersion < astahModelVersion;
    }
}
