package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AstahToolProvider implements ToolProvider {

    protected abstract List<ToolDefinition> createTools();

    @Override
    public final List<ToolDefinition> createToolDefinitions() {
        try {
            return List.copyOf(createTools());

        } catch (Exception e) {
            log.error("Failed to create tools of {}", name(), e);
            return List.of();
        }
    }
}
