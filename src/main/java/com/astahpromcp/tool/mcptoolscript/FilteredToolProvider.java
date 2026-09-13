package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;

import java.util.List;
import java.util.Set;

// Exposes only the named tools of a delegate provider.
//
// The programmatic profile registers a small subset of the tools the providers offer, but it has to take that subset from
// the very same provider instances the registry was built from. Several providers cache what they load on first use,
// so building them a second time for the exposed subset would give the two paths separate caches.
public final class FilteredToolProvider implements ToolProvider {

    private final ToolProvider delegate;
    private final Set<String> exposedNames;

    public FilteredToolProvider(ToolProvider delegate, Set<String> exposedNames) {
        this.delegate = delegate;
        this.exposedNames = exposedNames;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return delegate.createToolDefinitions().stream()
                .filter(definition -> exposedNames.contains(definition.toolSchema().name()))
                .toList();
    }

    // Whether this provider contributes anything at all, so that empty ones can be left out of the profile
    public boolean hasAnyExposedTool() {
        return !createToolDefinitions().isEmpty();
    }
}
