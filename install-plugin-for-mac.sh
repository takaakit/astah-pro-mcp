#!/bin/bash
set -euo pipefail

# This script installs the built astah-pro-mcp plugin JAR file into Astah.

PLUGIN_DIR="$HOME/.astah/professional/plugins"

if [ ! -d "$PLUGIN_DIR" ]; then
  echo "Creating plugin directory: $PLUGIN_DIR"
  mkdir -p "$PLUGIN_DIR"
fi

shopt -s nullglob
jar_files=(target/astah-pro-mcp-*.jar)
shopt -u nullglob

if [ ${#jar_files[@]} -eq 0 ]; then
  echo "Error: No astah-pro-mcp-*.jar file found in target directory."
  exit 1
fi

jar_file="${jar_files[0]}"

echo "Copying plugin..."
echo "Source: $jar_file"
echo "Destination: $PLUGIN_DIR"

cp -f "$jar_file" "$PLUGIN_DIR/"

# Set cache directory path
CACHE_DIR="$HOME/.astah/professional/cache"

# Delete cache directory if it exists
if [ -d "$CACHE_DIR" ]; then
  echo "Deleting cache directory: $CACHE_DIR"
  rm -rf "$CACHE_DIR"
  echo "Cache directory deleted successfully."
else
  echo "Cache directory does not exist: $CACHE_DIR"
fi

echo "Installation completed."

