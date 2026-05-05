---
name: astah-api-doc
description: Look up Astah Java API documentation (Javadoc) when you need information about classes, interfaces, methods, fields, or packages under com.change_vision.jude.api.inf.
allowed-tools: WebFetch
---

# Astah API Documentation Lookup

Use this skill to retrieve Astah API (latest version) Javadoc information.

## Base URL

```
https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc
```

## Lookup Steps

1. **To find a specific class or interface by name**, fetch the all-classes index:
   ```
   {base}/allclasses-index.html
   ```
   Search the result for the type name to confirm its fully qualified path.

2. **To browse all API entries alphabetically** (methods, fields, classes), fetch:
   ```
   {base}/index-all.html
   ```

3. **To read the detail page for a specific type**, construct the URL as:
   ```
   {base}/com/change_vision/jude/api/inf/{subpackage}/{TypeName}.html
   ```
   For inner classes, use a dot separator: `{TypeName}.{InnerName}.html`.

   Known sub-packages: `editor`, `exception`, `model`, `net`, `presentation`, `project`, `system`, `ui`, `view`.
   Types directly under `com.change_vision.jude.api.inf` (e.g. `AstahAPI`) have no sub-package segment.

4. **To view a package summary**, fetch:
   ```
   {base}/com/change_vision/jude/api/inf/{subpackage}/package-summary.html
   ```

## Guidelines

- Always fetch the actual Javadoc page via `WebFetch` rather than guessing API signatures.
- If a page returns an error, fall back to the all-classes index or alphabetical index to locate the correct path.
- When following links found within a fetched page, resolve them relative to the base URL above.
- Return precise information: class hierarchy, method signatures, parameter descriptions, and return types.
