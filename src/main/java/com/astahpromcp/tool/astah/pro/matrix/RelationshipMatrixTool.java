package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.astah.pro.common.MatrixDirection;
import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.astahpromcp.tool.astah.pro.common.MatrixShowElements;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.RelationshipMatrixDTO;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// The relationship matrix: the relationships of the chosen kinds between two sets of elements.
@Slf4j
public class RelationshipMatrixTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final AstahProToolSupport astahProToolSupport;
    private final MatrixMarkdownRenderer renderer;
    private final int maxResponseBytes;

    public RelationshipMatrixTool(ProjectAccessor projectAccessor, AstahProToolSupport astahProToolSupport) {
        this(
                projectAccessor,
                astahProToolSupport,
                McpServerConfig.RELATIONSHIP_MATRIX_MAX_GRID_ROWS,
                McpServerConfig.RELATIONSHIP_MATRIX_MAX_GRID_COLUMNS,
                McpServerConfig.RELATIONSHIP_MATRIX_MAX_RESPONSE_BYTES);
    }

    // With limits of its own, so that a test can reach them without a model of that size
    RelationshipMatrixTool(
            ProjectAccessor projectAccessor,
            AstahProToolSupport astahProToolSupport,
            int maxGridRows,
            int maxGridColumns,
            int maxResponseBytes) {
        this.projectAccessor = projectAccessor;
        this.astahProToolSupport = astahProToolSupport;
        this.renderer = new MatrixMarkdownRenderer(maxGridRows, maxGridColumns);
        this.maxResponseBytes = maxResponseBytes;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
                ToolSupport.toolDefinitionReturningContents(
                        "retrieve_relationship_matrix",
                        "Return a relationship matrix as Markdown text: the elements chosen by the row criteria, the elements chosen by the column criteria, and the relationships of the chosen kinds between the two. In this tool, a relationship means any connection between two elements, not only a UML or requirement relationship: containment, state transitions, activity flows, connectors, links, data flows and ER relationships are relationships here too. Use this tool to check traceability and coverage between two sets of elements, for example which requirements no block satisfies (list them with showElements set to rows_without_relationships) or which requirements no test case verifies. The result lists each row and column element with its ID and its relationships, so that you can pass the IDs to other tool functions; a grid is added only while it stays small enough to read. An association navigable on neither end, which is what an association has by default, has no direction. Set includeImpliedRelationships to count a class as satisfying the requirements its parts satisfy, and groupByPackage to see how the packages that hold the elements depend on one another. A state transition, an activity flow, a connector, a link, a data flow or an ER relationship joins elements of types that are not taken by default, so it is shown only when the elementTypes of the rows and of the columns name the types of its ends, such as State for a transition. This tool only reads the model; create or delete relationships with the other tool functions.",
                        this::retrieveRelationshipMatrix,
                        RelationshipMatrixDTO.class));
    }

    private List<McpSchema.Content> retrieveRelationshipMatrix(RelationshipMatrixDTO param) throws Exception {
        return List.of(McpSchema.TextContent.builder(createMarkdown(param)).build());
    }

    // Arguments given wrongly are thrown as IllegalArgumentException, so that they are logged as input the caller got wrong rather than as a fault of this server
    private String createMarkdown(RelationshipMatrixDTO param) throws Exception {
        List<MatrixRelationshipKind> kinds = checkedKinds(param.relationshipKinds());
        MatrixDirection direction = required(param.direction(), "direction", "row_to_column, column_to_row or both");
        MatrixShowElements showElements = required(param.showElements(), "showElements",
                "all, with_relationships, without_relationships, rows_without_relationships or columns_without_relationships");

        MatrixElementCollector collector = new MatrixElementCollector(projectAccessor, astahProToolSupport);
        MatrixElementCollector.Axis rowAxis = collector.resolve("rows", param.rows());
        MatrixElementCollector.Axis columnAxis = collector.resolve("columns", param.columns());

        List<INamedElement> candidates = collector.candidates(rowAxis, columnAxis);
        List<INamedElement> rowElements = collector.select(rowAxis, candidates);
        List<INamedElement> columnElements = collector.select(columnAxis, candidates);

        // A relationship is placed only between a row and a column, so only the
        // elements of either axis need to be read
        Map<String, INamedElement> elements = new LinkedHashMap<>();
        for (INamedElement element : rowElements) {
            elements.putIfAbsent(element.getId(), element);
        }
        for (INamedElement element : columnElements) {
            elements.putIfAbsent(element.getId(), element);
        }

        // The end of a link does not lead back to its link, so the links are taken from the whole project
        List<ILink> links = new ArrayList<>();
        if (kinds.contains(MatrixRelationshipKind.LINK)) {
            for (INamedElement link : projectAccessor.findElements(ILink.class)) {
                links.add((ILink) link);
            }
        }

        boolean impliedRelationships = param.includeImpliedRelationships() && kinds.contains(MatrixRelationshipKind.SATISFY);
        List<MatrixRelationship> relationships = MatrixRelationshipCollector.collect(elements.values(),
                Set.copyOf(kinds), impliedRelationships, links);

        RelationshipMatrix matrix = RelationshipMatrix.of(
                toMatrixElements(rowElements),
                toMatrixElements(columnElements),
                relationships,
                direction,
                showElements);

        // The packages are found after the relationships are placed between the elements, so that each keeps the cell and the direction it has there
        if (param.groupByPackage()) {
            matrix = matrix.groupedBy(packagesByElementId(elements.values()));
        }

        log.debug("Relationship matrix of {} rows and {} columns with {} relationships",
                matrix.rows().size(), matrix.columns().size(), matrix.relationshipCount());

        String markdown = renderer.render(
                new MatrixCriteriaSummary(rowAxis.describe(), columnAxis.describe(), kinds, direction, showElements,
                        impliedRelationships, param.groupByPackage()),
                matrix,
                param.listRelationships());

        // A response cut short could make the caller take a relationship it does not see for one that does not exist, so a response too long is refused instead.
        // Bytes rather than characters, as the other limits of this server count them, keep the limit close to the tokens a client counts whatever the language of the names.
        int bytes = markdown.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > maxResponseBytes) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "The relationship matrix takes %,d bytes, over the limit of %,d bytes. Narrow scopeIds or elementTypes, list fewer elements with showElements, set listRelationships to false, or split the scopes across several calls.",
                    bytes, maxResponseBytes));
        }

        return markdown;
    }

    // The kinds as given, each once, in the order given
    private static List<MatrixRelationshipKind> checkedKinds(List<MatrixRelationshipKind> relationshipKinds) {
        Set<MatrixRelationshipKind> kinds = new LinkedHashSet<>();
        if (relationshipKinds != null) {
            for (MatrixRelationshipKind kind : relationshipKinds) {
                if (kind != null) {
                    kinds.add(kind);
                }
            }
        }

        if (kinds.isEmpty()) {
            throw new IllegalArgumentException(
                    "relationshipKinds is empty. Specify at least one kind of relationship, such as satisfy.");
        }
        return List.copyOf(kinds);
    }

    private static <T> T required(T value, String name, String values) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("%s is missing. Specify %s.", name, values));
        }
        return value;
    }

    // The package that holds each element, as a row or a column shows it
    private Map<String, MatrixElement> packagesByElementId(Collection<INamedElement> elements) throws Exception {
        IModel project = projectAccessor.getProject();
        Map<String, MatrixElement> packagesById = new HashMap<>();
        Map<String, MatrixElement> packagesByElementId = new HashMap<>();
        for (INamedElement element : elements) {
            IPackage holder = MatrixElementCollector.packageOf(element, project);
            MatrixElement holderElement = packagesById.computeIfAbsent(holder.getId(),
                    id -> MatrixElementCollector.toMatrixElement(holder));
            packagesByElementId.put(element.getId(), holderElement);
        }
        return packagesByElementId;
    }

    private static List<MatrixElement> toMatrixElements(List<INamedElement> elements) {
        List<MatrixElement> matrixElements = new ArrayList<>(elements.size());
        for (INamedElement element : elements) {
            matrixElements.add(MatrixElementCollector.toMatrixElement(element));
        }
        return matrixElements;
    }
}
