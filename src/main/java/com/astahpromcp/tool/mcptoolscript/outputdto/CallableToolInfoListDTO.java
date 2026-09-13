package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CallableToolInfoListDTO(
    @JsonPropertyDescription("The tool functions that were described, in the order they were asked for")
    List<CallableToolInfoDTO> tools,

    @JsonPropertyDescription("One line for every name that was not described, saying why. Ask again for the ones that were left out for room.")
    List<String> problems
) {
}
