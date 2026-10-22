package com.astahpromcp.tool.projectdiff.export;

import java.time.Instant;

// What the capture of the open project recorded
public record OpenProjectSnapshot(String rootId, String projectName, String path, Instant startedAt, Instant completedAt,
                                  ExportInformation information) {
}
