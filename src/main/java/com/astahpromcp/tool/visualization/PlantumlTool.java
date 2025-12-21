package com.astahpromcp.tool.visualization;

import com.astahpromcp.tool.*;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.visualization.inputdto.PlantumlDTO;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

import java.io.ByteArrayOutputStream;
import java.util.*;

@Slf4j
public class PlantumlTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    
    public PlantumlTool(ProjectAccessor projectAccessor) {
        this.projectAccessor = projectAccessor;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            generateDiagramImageFromPlantumlTool(),
            getRelationshipsAsPlantumlCodeTool()
        );
    }

    private ToolDefinition generateDiagramImageFromPlantumlTool() {
        McpSchema.Tool schema = McpSchema.Tool.builder()
                .name("generate_dgm_img_from_puml")
                .description("Generate a diagram image based on the provided PlantUML code. Make use of this tool when the MCP client (you) wants to visually understand the structure and behavior of architectures and algorithms.")
                .inputSchema(JsonSupport.MCP_JSON_MAPPER, SchemaSupport.generateSchema(PlantumlDTO.class))
                .build();

        return new ToolDefinition(schema, this::generateDiagramImageFromPlantuml);
    }

    private McpSchema.CallToolResult generateDiagramImageFromPlantuml(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        ValidationSupport.ValidationResult<PlantumlDTO> parsed = ValidationSupport.parse(request.arguments(), PlantumlDTO.class);
        if (parsed.error() != null) return parsed.error();

        try {
            PlantumlDTO param = parsed.dto();
            McpSchema.ImageContent content = createImageContent(param.plantumlCode());
            List<McpSchema.Content> contents = new ArrayList<>();
            contents.add(content);

            return McpSchema.CallToolResult.builder()
                    .content(contents)
                    .isError(false)
                    .build();

        } catch (Exception e) {
            String message = "Failed to generate PlantUML diagram image: " + e.getMessage();
            log.error(message, e);
            return ResponseSupport.error(message);
        }
    }

    private McpSchema.ImageContent createImageContent(String plantumlCode) throws Exception {
        log.debug("Generate PlantUML diagram image from code");

        if (plantumlCode == null || plantumlCode.trim().isEmpty()) {
            throw new Exception("PlantUML code is null or empty");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantumlCode);
            FileFormatOption formatOption = new FileFormatOption(FileFormat.PNG);
            DiagramDescription description = reader.outputImage(outputStream, formatOption);
            
            byte[] pngBytes = outputStream.toByteArray();

            if (pngBytes.length == 0) {
                throw new RuntimeException("PlantUML PNG generation produced empty result");
            }

            log.info("PlantUML PNG generation succeeded ({} bytes, description: {})", 
                    pngBytes.length, description);

            String encoded = Base64.getEncoder().encodeToString(pngBytes);
            return new McpSchema.ImageContent(null, encoded, "image/png");

        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Insufficient memory to process PlantUML diagram", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert PlantUML to PNG", e);
        }
    }

    private ToolDefinition getRelationshipsAsPlantumlCodeTool() {
        return ToolSupport.definition(
            "get_relationships_as_puml_code",
            "Return the PlantUML code for a class diagram that shows only the relationships among NamedElements in the Astah project. However, do not include the role names, multiplicities, or composition types of the relationships. If you need that information, retrieve the detail information for each individual relationship. This tool is recommended when you want to understand the overall element relationships in the Astah project.",
            this::getRelationshipsAsPlantumlCode,
            NoInputDTO.class,
            PlantumlDTO.class);
    }

    private PlantumlDTO getRelationshipsAsPlantumlCode(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get relationships as PlantUML code: {}", param);

        StringBuilder plantumlCode = new StringBuilder("@startuml\n");

        List<INamedElement> namedElements = Arrays.asList(projectAccessor.findElements(INamedElement.class));
        Collections.sort(namedElements, (a, b) -> a.getFullName(".").compareTo(b.getFullName(".")));

        for (INamedElement namedElement : namedElements) {
            if (namedElement.getName().isEmpty()) {
                continue;
            }

            if (namedElement instanceof IClass) {
                // Add nested classes
                for (IClass nestedClass : ((IClass) namedElement).getNestedClasses()) {
                    plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                            .append(" +-- ")
                            .append("\"").append(nestedClass.getFullName(".")).append("\"\n");
                }

                // Add inheritances
                for (IGeneralization generalization : ((IClass) namedElement).getGeneralizations()) {
                    INamedElement superType = generalization.getSuperType();
                    if (superType == null) {
                        continue;
                    }

                    plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                            .append(" --|> ")
                            .append("\"").append(superType.getFullName(".")).append("\"\n");
                }
            }
            
            // Add realizations
            for (IRealization realization : namedElement.getClientRealizations()) {
                INamedElement supplier = realization.getSupplier();
                if (supplier == null) {
                    continue;
                }

                plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                        .append(" ..|> ")
                        .append("\"").append(supplier.getFullName(".")).append("\"\n");
            }

            // Add dependencies
            for (IDependency dependency : namedElement.getClientDependencies()) {
                INamedElement supplier = dependency.getSupplier();
                if (supplier == null) {
                    continue;
                }

                plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                        .append(" ..> ")
                        .append("\"").append(supplier.getFullName(".")).append("\"\n");
            }
            
            // Add usages
            for (IUsage usage : namedElement.getClientUsages()) {
                INamedElement supplier = usage.getSupplier();
                if (supplier == null) {
                    continue;
                }
                
                plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                        .append(" ..> ")
                        .append("\"").append(supplier.getFullName(".")).append("\"\n");
            }
        }
        plantumlCode.append("@enduml\n");

        return new PlantumlDTO(plantumlCode.toString());
    }
}
