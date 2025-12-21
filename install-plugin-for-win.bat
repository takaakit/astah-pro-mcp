@echo off
setlocal enabledelayedexpansion

REM This script installs the built astah-pro-mcp plugin JAR file into Astah.

REM Set plugin directory path
set "PLUGIN_DIR=%USERPROFILE%\.astah\professional\plugins"

REM Create plugin directory if it doesn't exist
if not exist "%PLUGIN_DIR%" (
    echo Creating plugin directory: %PLUGIN_DIR%
    mkdir "%PLUGIN_DIR%"
)

REM Search for astah-pro-mcp-*.jar file in target folder
set "JAR_FILE="
for %%f in (target\astah-pro-mcp-*.jar) do (
    set "JAR_FILE=%%f"
    goto :found
)

:found
if "%JAR_FILE%"=="" (
    echo Error: astah-pro-mcp-*.jar file not found in target folder.
    pause
    exit /b 1
)

REM Copy jar file
echo Copying plugin...
echo Source: %JAR_FILE%
echo Destination: %PLUGIN_DIR%

copy /Y "%JAR_FILE%" "%PLUGIN_DIR%\" >nul
if %ERRORLEVEL% EQU 0 (
    echo Copy completed successfully.
) else (
    echo Error: Copy failed.
    pause
    exit /b 1
)

REM Set cache directory path
set "CACHE_DIR=%USERPROFILE%\.astah\professional\cache"

REM Delete cache directory if it exists
if exist "%CACHE_DIR%" (
    echo Deleting cache directory: %CACHE_DIR%
    rmdir /s /q "%CACHE_DIR%"
    if %ERRORLEVEL% EQU 0 (
        echo Cache directory deleted successfully.
    ) else (
        echo Warning: Failed to delete cache directory.
    )
) else (
    echo Cache directory does not exist: %CACHE_DIR%
)

echo.
echo Installation completed.
pause

