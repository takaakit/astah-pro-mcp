package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PropertyChangeDTO(
    @JsonPropertyDescription("Position of the value in the XMI, relative to the element (e.g., @name)")
    String path,

    @JsonPropertyDescription("Change of the value: added, removed, modified, or reordered (the same references in another order)")
    PropertyChangeKind change,

    @JsonPropertyDescription("Value in the base; empty if added")
    String before,

    @JsonPropertyDescription("Value in the target; empty if removed")
    String after,

    @JsonPropertyDescription("Name of the element 'before' refers to; empty otherwise")
    String beforeRefName,

    @JsonPropertyDescription("Name of the element 'after' refers to; empty otherwise")
    String afterRefName,

    @JsonPropertyDescription("decoded, raw, or unknown (may still be URL-encoded)")
    ValueEncoding encoding,

    @JsonPropertyDescription("True if a value of this property is cut short in this answer; the whole value is in the file at 'fullDiffPath'")
    boolean cut
) {
}
