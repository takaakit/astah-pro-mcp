package com.astahpromcp.tool.astah.pro.script.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RunScriptDTO(
    @JsonPropertyDescription("JavaScript (Nashorn, ECMAScript 5.1) source code to run inside Astah. The global variable 'astah' (alias 'projectAccessor') is the ProjectAccessor of the running Astah. Use print() for output (console.log is unavailable). Use Java.type() or new JavaImporter() to access Java classes (importPackage is unavailable). Wrap model edits in TransactionManager beginTransaction/endTransaction.")
    String script
) {
}
