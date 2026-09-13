package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record McpToolScriptResultDTO(
    @JsonPropertyDescription("True if the script completed without error")
    boolean ok,

    @JsonPropertyDescription("String form of the script's last expression value; empty when none or failed")
    String result,

    @JsonPropertyDescription("Text written by print()")
    String stdout,

    @JsonPropertyDescription("Error output, including a note when a transaction left open by the script was aborted")
    String stderr,

    @JsonPropertyDescription("Error message when 'ok' is false; empty otherwise. It says what went wrong; 'errorLine' and 'errorToolCallIndex' say where.")
    String errorMessage,

    @JsonPropertyDescription("1-based line of the script error, or -1 when unknown")
    int errorLine,

    @JsonPropertyDescription("1-based column of the script error, or -1 when unknown")
    int errorColumn,

    @JsonPropertyDescription("1-based ordinal of the tool function call that failed, counting every call the run made, or -1 when the failure was not in one. Read it together with 'errorLine': the line says which call in the script failed, and this says which time it was called, which is what identifies the pass of a loop that a repeated call failed on.")
    int errorToolCallIndex
) {
}
