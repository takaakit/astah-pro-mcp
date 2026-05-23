---
name: mcp-core-api-doc
description: Look up Java MCP SDK API documentation (Javadoc) when you need information about classes, interfaces, methods, fields, or packages under io.modelcontextprotocol.
allowed-tools: WebFetch
---

# Java MCP SDK API Documentation Lookup

Use this skill to retrieve Java MCP SDK API information.

## Base URL

```
https://javadoc.io/doc/io.modelcontextprotocol.sdk/mcp-core/latest
```

## Lookup Steps

1. **To find a specific class or interface by name**, fetch the all-classes index:
   ```
   {base}/allclasses-index.html
   ```

2. **To browse all API entries alphabetically** (methods, fields, classes), fetch:
   ```
   {base}/index-all.html
   ```

3. **To read the detail page for a specific type**, construct the URL as:
   ```
   {base}/io/modelcontextprotocol/{subpackage}/{TypeName}.html
   ```

4. **To view a package summary**, fetch:
   ```
   {base}/io/modelcontextprotocol/{subpackage}/package-summary.html
   ```

## Guidelines

- Always fetch the actual Javadoc page via `WebFetch` rather than guessing API signatures.
- If a page returns an error, fall back to the all-classes index or alphabetical index to locate the correct path.
- When following links found within a fetched page, resolve them relative to the base URL above.
- Return precise information: class hierarchy, method signatures, parameter descriptions, and return types.
