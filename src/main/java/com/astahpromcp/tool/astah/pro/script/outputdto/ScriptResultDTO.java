package com.astahpromcp.tool.astah.pro.script.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ScriptResultDTO(
    @JsonPropertyDescription("True if the script completed without error")
    boolean ok,

    @JsonPropertyDescription("String form of the script's last expression value; empty when none or failed")
    String result,

    @JsonPropertyDescription("Text written by print()")
    String stdout,

    @JsonPropertyDescription("Error output, including a note when a transaction left open by the script was aborted")
    String stderr,

    @JsonPropertyDescription("Error message when 'ok' is false; empty otherwise")
    String errorMessage,

    @JsonPropertyDescription("1-based line of the script error, or -1 when unknown")
    int errorLine,

    @JsonPropertyDescription("1-based column of the script error, or -1 when unknown")
    int errorColumn
) {
}
