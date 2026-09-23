# Astah Pro MCP: Enabling AI-Powered UML Modeling

A local MCP server plugin for Astah Professional, a UML modeling tool, that enables AI agents to:

- Design systems and represent them as UML models.
- Explain UML models.
- Generate source code from UML models, and vice versa.
- Create UML diagrams from hand-drawn sketches.

This plugin experimentally implements a programmatic tool calling mode, inspired by the **"Programmatic Tool Calling"** idea described in [this Anthropic article](https://www.anthropic.com/engineering/advanced-tool-use).

## Demo

Prompts:  
**I.** *Design a simple library management system and draw a class diagram in Astah to illustrate its structure.*  
**II.** *Based on the contents of the Astah model, draw a sequence diagram to illustrate the behavior of borrowing a book.*  
**III.** *Based on the contents of the Astah model, draw an activity diagram to illustrate the behavior of returning a book.*  
**IV.** *Based on the contents of the Astah model, draw a state machine diagram to illustrate the state transitions of a book.*

### Claude Code

Model: Opus 5.5 / Effort: Medium / Total time: approx. 15 min

<table width="100%">
  <tr>
    <td width="25%" align="center"><b>I</b></td>
    <td width="25%" align="center"><b>II</b></td>
    <td width="25%" align="center"><b>III</b></td>
    <td width="25%" align="center"><b>IV</b></td>
  </tr>
  <tr>
    <td width="25%"><a href="demo/claude-code/Library%20Management%20System.png"><img src="demo/claude-code/Library%20Management%20System.png" width="100%"></a></td>
    <td width="25%"><a href="demo/claude-code/Borrow%20a%20Book.png"><img src="demo/claude-code/Borrow%20a%20Book.png" width="100%"></a></td>
    <td width="25%"><a href="demo/claude-code/Return%20a%20Book.png"><img src="demo/claude-code/Return%20a%20Book.png" width="100%"></a></td>
    <td width="25%"><a href="demo/claude-code/BookCopy%20States.png"><img src="demo/claude-code/BookCopy%20States.png" width="100%"></a></td>
  </tr>
</table>

### Codex CLI

Model: GPT-6 Astra / Effort: Medium / Total time: approx. 19 min

<table width="100%">
  <tr>
    <td width="25%" align="center"><b>I</b></td>
    <td width="25%" align="center"><b>II</b></td>
    <td width="25%" align="center"><b>III</b></td>
    <td width="25%" align="center"><b>IV</b></td>
  </tr>
  <tr>
    <td width="25%"><a href="demo/codex-cli/Library%20Management.png"><img src="demo/codex-cli/Library%20Management.png" width="100%"></a></td>
    <td width="25%"><a href="demo/codex-cli/Borrow%20a%20Book.png"><img src="demo/codex-cli/Borrow%20a%20Book.png" width="100%"></a></td>
    <td width="25%"><a href="demo/codex-cli/Return%20a%20Book.png"><img src="demo/codex-cli/Return%20a%20Book.png" width="100%"></a></td>
    <td width="25%"><a href="demo/codex-cli/BookCopy%20Lifecycle.png"><img src="demo/codex-cli/BookCopy%20Lifecycle.png" width="100%"></a></td>
  </tr>
</table>

### Grok Build

Model: Grok 4.7 / Effort: Medium / Total time: approx. 41 min

<table width="100%">
  <tr>
    <td width="25%" align="center"><b>I</b></td>
    <td width="25%" align="center"><b>II</b></td>
    <td width="25%" align="center"><b>III</b></td>
    <td width="25%" align="center"><b>IV</b></td>
  </tr>
  <tr>
    <td width="25%"><a href="demo/grok-build/Library%20structure.png"><img src="demo/grok-build/Library%20structure.png" width="100%"></a></td>
    <td width="25%"><a href="demo/grok-build/Borrow%20a%20copy.png"><img src="demo/grok-build/Borrow%20a%20copy.png" width="100%"></a></td>
    <td width="25%"><a href="demo/grok-build/Return%20a%20copy.png"><img src="demo/grok-build/Return%20a%20copy.png" width="100%"></a></td>
    <td width="25%"><a href="demo/grok-build/Book%20copy.png"><img src="demo/grok-build/Book%20copy.png" width="100%"></a></td>
  </tr>
</table>

## Requirements

- **AI agent**

  The AI agents below have been tested, but no guarantee is implied. From personal experience, **Claude Code** works well for UML modeling, followed by **Codex CLI**.

  - Claude Code
  - Codex CLI
  - Grok Build
  - Antigravity CLI
  - Cursor IDE
  - Kiro IDE

- **Astah Pro v12.0 or later**

  > *Note:* This MCP server only connects to AI agents running on the same machine as Astah Professional.
  According to Astah's terms of use, using Astah via an AI agent is permitted only if you hold a valid license and access it exclusively for your own use with your licensed Astah. Allowing a non-licensed third party to operate Astah via such an agent is strictly prohibited.  
  For details, please refer to the FAQ ([English](https://astah.net/support/cv-members-guide/#ai-external-access) / [Japanese](https://astah.change-vision.com/ja/faq/faq-license/ai-external-access.html)) or [contact Change Vision (the developer of Astah)](https://astah.net/about/contact/) directly.

## Supported Diagram Types

- **Class Diagram**  
- **Sequence Diagram**  
- **Activity Diagram**
- **State Machine Diagram**
- **Use Case Diagram**
- **Mind Map**
- **Requirement Diagram**
- **Composite Structure Diagram** (with some limitations)
- **Communication Diagram** (query-only)
- **ER Diagram** (query-only)

## Installation

### Install Astah Pro

Download from [here](https://astah.net/downloads/) and install.

### Install the Astah Pro MCP plugin

Download [the plugin JAR file (astah-pro-mcp-x.x.x.jar)](https://github.com/takaakit/astah-pro-mcp/releases), drop it into Astah, and restart Astah (see [here](https://astahblog.com/2014/12/15/astah_plugins/)). If the `mcp` tab appears in the Extra View, the plugin is installed.

![mcp tab](img/mcp-tab.png)

### AI agent settings

This plugin experimentally implements a programmatic tool calling mode (port `8888`), inspired by the **"Programmatic Tool Calling"** idea described in [this Anthropic article](https://www.anthropic.com/engineering/advanced-tool-use). The direct tool calling mode also remains available (port `18888`). Unless you have a specific reason to choose otherwise, specify port `8888`.

| Mode | Port | Exposed tools |
| --- | --- | --- |
| Programmatic tool calling | `8888` | 84 |
| Direct tool calling | `18888` | 399 |

<details>
<summary><b>Claude Code</b></summary>

Run this command for project scope in your project directory:
```bash
claude mcp add --transport http --scope project astah-pro-mcp http://127.0.0.1:8888/mcp
```

Or run this command for user scope:
```bash
claude mcp add --transport http --scope user astah-pro-mcp http://127.0.0.1:8888/mcp
```

</details>


<details>
<summary><b>Codex CLI</b></summary>

Create `.codex/config.toml` under your project directory or your user directory with:

```toml
[mcp_servers.astah-pro-mcp]
transport = "http"
url = "http://127.0.0.1:8888/mcp"
```

</details>


<details>
<summary><b>Grok Build</b></summary>

Run this command for project scope in your project directory:
```bash
grok mcp add --scope project --transport http astah-pro-mcp http://127.0.0.1:8888/mcp
```

Or run this command for user scope:
```bash
grok mcp add --scope user --transport http astah-pro-mcp http://127.0.0.1:8888/mcp
```

</details>


<details>
<summary><b>Antigravity CLI</b></summary>

Create `.agents/mcp_config.json` under your project directory (workspace scope) or edit `~/.gemini/config/mcp_config.json` (global scope) with:

```json
{
  "mcpServers": {
    "astah-pro-mcp": {
      "serverUrl": "http://127.0.0.1:8888/mcp"
    }
  }
}
```

</details>


<details>
<summary><b>Cursor IDE</b></summary>

```json
{
  "mcpServers": {
    "astah-pro-mcp": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://127.0.0.1:8888/mcp",
        "--allow-http"
      ]
    }
  }
}
```

> Use [mcp-remote](https://github.com/geelen/mcp-remote) to bridge the HTTP connection. [Node.js](https://nodejs.org/) must be installed.

</details>


<details>
<summary><b>Kiro IDE</b></summary>

```json
{
  "mcpServers": {
    "astah-pro-mcp": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://127.0.0.1:8888/mcp",
        "--allow-http"
      ]
    }
  }
}
```

> Use [mcp-remote](https://github.com/geelen/mcp-remote) to bridge the HTTP connection. [Node.js](https://nodejs.org/) must be installed.

</details>


## Changing the Port Numbers

By default, the plugin listens on `8888` (programmatic tool calling mode) and `18888` (direct tool calling mode). Each port can be overridden with an environment variable:

| Mode | Environment variable |
| --- | --- |
| Programmatic tool calling | `ASTAH_PRO_MCP_PORT_FOR_PROGRAMMATIC` |
| Direct tool calling | `ASTAH_PRO_MCP_PORT_FOR_DIRECT` |

Remember to update your AI agent settings to the same port numbers.

## How to Use

1. Start Astah Pro

   Some AI agents try to connect to the MCP server on startup, so start Astah Pro **first**.

2. Start AI agents

   Each time an AI agent establishes a new session with the Astah Pro MCP server, you will be asked to confirm. Review the details and click **'Connect'**.  
  ![Connection Request](img/mcp-connection-request.png)

3. Send prompts to the AI agents

If you want to disable the Astah Pro MCP plugin in Astah, click [Plugin] > [Installed Plugins], select the *Astah Pro MCP* entry in the plugin list dialog, click Disable, and then restart Astah.

## Build & Test

If you want to build and test locally:

1. Set up your Astah plugin development environment (see [here](https://astah.net/support/plugin-dev-tutorial/plugin-development-setup-for-astah-professional/)).

2. Build:
   ```bash
   astah-build
   ```

3. Run tests (change `astahPath` to your Astah Pro installation path):

   Run all tests on Windows 11:
   ```bash
   astah-mvn test -DastahPath="C:\Program Files\astah-professional"
   ```

   Run specific tests on Windows 11:
   ```bash
   astah-mvn test -DastahPath="C:\Program Files\astah-professional" -Dtest="**/editor/*Test"
   ```

## Considerations

### Experimental Status
This project is currently experimental. The design and implementation may undergo breaking changes.

### Confidentiality Risk
Astah project data and logs will be shared with the AI agent. For Astah projects that contain confidential information, either refrain from using this MCP server or use it only with appropriate safeguards (e.g., enabling opt-out settings for AI agents).

### Backups Recommended
Because this MCP server edits model elements and diagrams, we recommend committing your Astah project to a Git repository or making copies before and during use so you can revert if necessary.

### Feature Limitations
Some model or diagram information (e.g., certain properties) cannot be viewed or edited via the provided tool functions. Use the Astah GUI directly for those.

### Costs
This MCP server prioritizes providing the information AI agents need and does **not** implement token-saving measures. We recommend using AI agents on a **flat-rate** plan rather than pay-as-you-go.

### AI Limitations
Just as when working with source code, AI agents can make mistakes or misinterpret model elements and diagrams.

## License

Some tool functions provided by this MCP server return excerpts from the [OMG UML 2.5.1](https://www.omg.org/spec/UML/2.5.1/PDF) and [OMG SysML 1.7](https://www.omg.org/spec/SysML/1.7/PDF) specifications and from [FIPS PUB 184 IDEF1X](https://www.govinfo.gov/app/details/GOVPUB-C13-986bf8b12a4fed44eb78fca0bb55d668). The OMG UML/SysML specifications are licensed as stated at the beginning of each document. FIPS PUB 184 IDEF1X is a U.S. Government work (NIST) and is not subject to copyright protection in the United States (17 U.S.C. §105), but may be subject to foreign copyright. When content from these specifications/documents is returned by tool functions, it is explicitly indicated as an excerpt. "Mind Map" is a registered trademark of The Buzan Organisation Limited.

One tool function returns UML diagram consistency rules quoted from the papers below. Copyright of these rule statements remains with their authors and publishers; they are quoted with attribution and explicitly indicated as excerpts.
- Torre, Damiano, et al. "A systematic identification of consistency rules for UML diagrams." Journal of Systems and Software 144 (2018): 121-142.
- Torre, Damiano, et al. "How consistency is handled in model-driven software engineering and UML: an expert opinion survey." Software Quality Journal 31.1 (2023): 1-54.

All other works, including source code, are copyrighted by **Takaaki Teshima** and released under the **MIT-0** license.  
[![License: MIT-0](https://img.shields.io/badge/License-MIT--0-blue.svg)](https://opensource.org/licenses/MIT-0)

## Disclaimer

This project is developed independently by the authors in their personal capacities and is not affiliated with any university, institution, or employer.

## Got a feature request or found a bug?

Please open an [issue](https://github.com/takaakit/astah-pro-mcp/issues). Because this project is in an experimental phase and may introduce breaking changes, we aren't accepting pull requests until the design and implementation stabilize. Thank you for your understanding.

## Need support?

If you need private support, contact `takaaki.teshima.dev [at] gmail.com` (replace `[at]` with `@`). It could become a paid project; I may still be able to support you/it.
