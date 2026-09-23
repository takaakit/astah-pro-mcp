package com.astahpromcp.tool.astah.pro.astahapiscript.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AstahApiScriptExampleDTO(
    @JsonPropertyDescription("Example name of the astah api script to return, spelled exactly as the catalogue spells it, '.js' included.")
    String exampleName
) {
}
