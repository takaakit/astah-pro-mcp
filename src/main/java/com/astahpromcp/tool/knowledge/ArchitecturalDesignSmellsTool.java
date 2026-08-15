package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.AbsoluteFilePathDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.TextContentsDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.knowledge.outputdto.SmellsDTO;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class ArchitecturalDesignSmellsTool implements ToolProvider {

    private static final String SMELLS_FILE_NAME = "smells-on-architecture-and-design.md";

    private final Path workspaceDir;

    public ArchitecturalDesignSmellsTool(Path workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "architectural_design_smells",
                    "MCP client (you) MUST call this tool function before creating, editing, or reviewing a UML model to grasp the procedure for recalling architectural smells and design smells that should be avoided, and then follow that procedure.",
                    this::recallSmells,
                    NoInputDTO.class,
                    GuideDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "store_architectural_design_smells",
                    "Store architectural smells and design smells that should be avoided in a file, and return the absolute path of the stored file.",
                    this::storeSmells,
                    TextContentsDTO.class,
                    AbsoluteFilePathDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "read_architectural_design_smells",
                    "Read architectural smells and design smells that should be avoided from the stored file.",
                    this::readSmells,
                    NoInputDTO.class,
                    SmellsDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create architectural and design smells tools", e);
            return List.of();
        }
    }

    private GuideDTO recallSmells(NoInputDTO param) throws Exception {
        log.debug("Recall architectural and design smells: {}", param);

        Path smellsFile = workspaceDir.resolve(SMELLS_FILE_NAME);
        String absolutePath = smellsFile.toAbsolutePath().toString();

        if (Files.exists(smellsFile)) {
            log.info("Smells file already exists: {}", absolutePath);
            String contents = """
Follow these steps to recall architectural smells and design smells that should be avoided:
1. Call the `read_architectural_design_smells` tool function to load the smells to avoid into your working context.
2. Give focused attention to the loaded smells so that avoiding them remains a priority throughout your subsequent reasoning.
""";

            return new GuideDTO(contents);

        } else {
            log.info("Smells file does not exist: {}", absolutePath);
            String contents = """
Follow these steps to recall architectural smells and design smells that should be avoided:
1. Retrieve from your internal knowledge the architectural and design smells to avoid that are cataloged in the following studies.
   - Mumtaz, Haris, Paramvir Singh, and Kelly Blincoe. "A systematic mapping study on architectural smells detection." Journal of Systems and Software 173 (2021): 110885.
   - Alkharabsheh, Khalid, et al. "Software design smell detection: a systematic mapping study." Software Quality Journal 27.3 (2019): 1069-1148.
   **Note:** Retrieve ONLY the architectural smells and design smells that are actually presented in these studies.
2. Verbalize those smells to avoid identified in each study, and call the `store_architectural_design_smells` tool function to store them.
3. Call the `read_architectural_design_smells` tool function to load the smells into your working context.
4. Give focused attention to the loaded smells so that avoiding them remains a priority throughout your subsequent reasoning.
""";

            return new GuideDTO(contents);
        }
    }

    private AbsoluteFilePathDTO storeSmells(TextContentsDTO param) throws Exception {
        log.debug("Store architectural and design smells: {}", param);

        Path smellsFile = workspaceDir.resolve(SMELLS_FILE_NAME);
        String absolutePath = smellsFile.toAbsolutePath().toString();

        if (Files.exists(smellsFile)) {
            throw new IllegalStateException("The file storing architectural and design smells already exists: " + absolutePath);
        }

        Files.writeString(smellsFile, param.contents());
        log.info("Stored smells file: {}", absolutePath);

        return new AbsoluteFilePathDTO(absolutePath);
    }

    private SmellsDTO readSmells(NoInputDTO param) throws Exception {
        log.debug("Read architectural and design smells: {}", param);

        Path smellsFile = workspaceDir.resolve(SMELLS_FILE_NAME);
        String absolutePath = smellsFile.toAbsolutePath().toString();

        if (!Files.exists(smellsFile)) {
            throw new IllegalStateException("The file for storing architectural and design smells has not been created yet: " + absolutePath);
        }

        String contents = Files.readString(smellsFile);
        log.info("Read smells file: {}", absolutePath);

        return new SmellsDTO(contents, absolutePath);
    }
}
