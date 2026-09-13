package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import io.modelcontextprotocol.spec.McpSchema;

import java.nio.file.Path;

// An image capture that skips the picture a diagram editing tool takes of its own result.
public class NoThumbnailImageCaptureSupport extends ImageCaptureSupport {

    // The editing tools pass this straight into List.of(...), which rejects null, so "no picture" has to be said with content carrying no data rather than with no content at all.
    private static final McpSchema.ImageContent NO_IMAGE =
            McpSchema.ImageContent.builder("", "image/png").build();

    public NoThumbnailImageCaptureSupport(AstahProToolSupport astahProToolSupport,
                                          SystemPropertySupport systemPropertySupport,
                                          Path imageOutputDir) {
        super(astahProToolSupport, systemPropertySupport, imageOutputDir);
    }

    @Override
    public McpSchema.ImageContent createSmallImageContent(String diagramId) {
        return NO_IMAGE;
    }
}
