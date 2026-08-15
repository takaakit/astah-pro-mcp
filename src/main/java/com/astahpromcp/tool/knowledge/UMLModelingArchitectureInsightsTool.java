package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.AbsoluteFilePathDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.TextContentsDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.knowledge.outputdto.InsightsDTO;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class UMLModelingArchitectureInsightsTool implements ToolProvider {

    private static final String INSIGHTS_FILE_NAME = "insights-on-uml-modeling-architecture.md";

    private final Path workspaceDir;

    public UMLModelingArchitectureInsightsTool(Path workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "uml_modeling_architecture_insights",
                    "MCP client (you) MUST call this tool function before creating, editing, or reviewing a UML model to grasp the procedure for recalling insights about UML, modeling, and architecture, and then follow that procedure.",
                    this::recallInsights,
                    NoInputDTO.class,
                    GuideDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "store_uml_modeling_architecture_insights",
                    "Store insights on UML, modeling, and architecture in a file, and return the absolute path of the stored file.",
                    this::storeInsights,
                    TextContentsDTO.class,
                    AbsoluteFilePathDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "read_uml_modeling_architecture_insights",
                    "Read insights on UML, modeling, and architecture from the stored file.",
                    this::readInsights,
                    NoInputDTO.class,
                    InsightsDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create UML, modeling, and architecture insights tools", e);
            return List.of();
        }
    }

    private GuideDTO recallInsights(NoInputDTO param) throws Exception {
        log.debug("Recall UML, modeling, and architecture insights: {}", param);

        Path insightsFile = workspaceDir.resolve(INSIGHTS_FILE_NAME);
        String absolutePath = insightsFile.toAbsolutePath().toString();

        if (Files.exists(insightsFile)) {
            log.info("Insights file already exists: {}", absolutePath);
            String contents = """
Follow these steps to recall insights on UML, modeling, and architecture:
1. Call the `read_uml_modeling_architecture_insights` tool function to load the insights into your working context.
2. Give focused attention to the loaded insights so that they are prioritized throughout your subsequent reasoning.
""";

            return new GuideDTO(contents);

        } else {
            log.info("Insights file does not exist: {}", absolutePath);
            String contents = """
Follow these steps to recall insights on UML, modeling, and architecture:
1. Retrieve from your internal knowledge any insights presented in the following books.
   - Martin Fowler. UML distilled: a brief guide to the standard object modeling language. Addison-Wesley Professional, 2018.
   - Grady Booch, James Rumbaugh, and Ivar Jacobson. Unified modeling language user guide, the 2nd edition. Addison-Wesley Professional, 2005.
   - James Rumbaugh, Ivar Jacobson, and Grady Booch. The Unified Modeling Language Reference Manual, the 2nd edition. Addison-Wesley Professional, 2004.
   - Scott W. Ambler. The Elements of UML 2.0 Style. Cambridge University Press, 2005.
   - Craig Larman. Applying UML and Patterns: An Introduction to Object-Oriented Analysis and Design and Iterative Development, the 3rd edition. Pearson Education, 2004.
   - Eric Evans. Domain-driven design: tackling complexity in the heart of software. Addison-Wesley Professional, 2004.
   - Robert C. Martin. Clean architecture: a craftsman's guide to software structure and design. Prentice Hall Press, 2017.
   **Note:** Retrieve ONLY insights that are actually presented in those books. Do NOT include any critique, commentary, explanation, interpretation, review, or personal impressions about the books.
2. Verbalize those insights explicitly for each book, and call the `store_uml_modeling_architecture_insights` tool function to store them.
3. Call the `read_uml_modeling_architecture_insights` tool function to load the insights into your working context.
4. Give focused attention to the loaded insights so that they are prioritized throughout your subsequent reasoning.
""";

            return new GuideDTO(contents);
        }
    }

    private AbsoluteFilePathDTO storeInsights(TextContentsDTO param) throws Exception {
        log.debug("Store UML, modeling, and architecture insights: {}", param);

        Path insightsFile = workspaceDir.resolve(INSIGHTS_FILE_NAME);
        String absolutePath = insightsFile.toAbsolutePath().toString();

        if (Files.exists(insightsFile)) {
            throw new IllegalStateException("The file storing insights on UML, modeling, and architecture already exists: " + absolutePath);
        }

        Files.writeString(insightsFile, param.contents());
        log.info("Stored insights file: {}", absolutePath);

        return new AbsoluteFilePathDTO(absolutePath);
    }

    private InsightsDTO readInsights(NoInputDTO param) throws Exception {
        log.debug("Read UML, modeling, and architecture insights: {}", param);

        Path insightsFile = workspaceDir.resolve(INSIGHTS_FILE_NAME);
        String absolutePath = insightsFile.toAbsolutePath().toString();

        if (!Files.exists(insightsFile)) {
            throw new IllegalStateException("The file for storing insights on UML, modeling, and architecture has not been created yet: " + absolutePath);
        }

        String contents = Files.readString(insightsFile);
        log.info("Read insights file: {}", absolutePath);

        return new InsightsDTO(contents, absolutePath);
    }
}
