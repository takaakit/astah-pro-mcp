package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AstahScriptGuideTool implements ToolProvider {

    public AstahScriptGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "astah_script_guide",
                    "MCP client (you) MUST call this tool function before using the 'run_astah_script' tool function to learn how to write an Astah script and how to use the Astah API callable from an Astah script.",
                    this::getGuide,
                    NoInputDTO.class,
                    GuideDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create astah script guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(NoInputDTO param) throws Exception {
        log.debug("Get astah script guide: {}", param);

        String contents = """
To learn how to write an Astah script, refer to the following web page:
[Sample Astah Scripts](https://astahblog.com/sample-scripts/)

To learn how to use the Astah API callable from an Astah script, refer to the following JavaDoc:
[Astah API JavaDoc](https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/index-all.html)
        """;

        return new GuideDTO(contents);
    }
}
