package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record McpToolScriptResultDTO(
    @JsonPropertyDescription("True if the script completed and no tool function call failed. A tool function failure the script caught still makes it false, even in a run that only reads; read 'modelChanges' to tell whether anything was lost.")
    boolean ok,

    @JsonPropertyDescription("String form of the script's last expression value; empty when none or failed")
    String result,

    @JsonPropertyDescription("Text written by print(). Output past the size limit is dropped, and 'stderr' then says so.")
    String stdout,

    @JsonPropertyDescription("Error output, including a note when 'stdout' was truncated or when a transaction left open by the script was aborted")
    String stderr,

    @JsonPropertyDescription("Error message when 'ok' is false; empty otherwise. It says what went wrong; 'errorLine' and 'errorToolCallIndex' say where.")
    String errorMessage,

    @JsonPropertyDescription("1-based line of the script error, or -1 when unknown")
    int errorLine,

    @JsonPropertyDescription("1-based column of the script error, or -1 when unknown")
    int errorColumn,

    @JsonPropertyDescription("1-based ordinal of the tool function call that failed, counting every call the run made, or -1 when the failure was not in one. Read it together with 'errorLine': the line says which call in the script failed, and this says which time it was called, which is what identifies the pass of a loop that a repeated call failed on.")
    int errorToolCallIndex,

    @JsonPropertyDescription("What became of the changes the run made to the model: 'none' when it made none (a run that only reads, or one that failed before it changed anything), so a failed run lost nothing and what it printed still holds; 'committed' when they were kept; 'rolled_back' when they were all discarded because the run failed; 'unknown' when the run timed out or its caller stopped waiting before it ended, in which case whatever it changed is rolled back once it stops.")
    ModelChangesKind modelChanges
) {
}
