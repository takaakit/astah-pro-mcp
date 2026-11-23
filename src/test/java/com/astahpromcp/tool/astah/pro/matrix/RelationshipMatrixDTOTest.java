package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.DtoBinder;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.SchemaSupport;
import com.astahpromcp.tool.astah.pro.common.MatrixDirection;
import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.astahpromcp.tool.astah.pro.common.MatrixShowElements;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.MatrixColumnCriteriaDTO;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.MatrixRowCriteriaDTO;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.RelationshipMatrixDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipMatrixDTOTest {

    @SuppressWarnings("unchecked")
    private static Map<String, Object> schema() throws Exception {
        return JsonSupport.OBJ_MAPPER.readValue(SchemaSupport.generateSchema(RelationshipMatrixDTO.class), Map.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Map<String, Object> node, String... path) {
        Map<String, Object> current = node;
        for (String name : path) {
            current = (Map<String, Object>) current.get(name);
            assertNotNull(current, "no " + name + " in " + node);
        }
        return current;
    }

    // The arguments an agent sends, which each test changes as it needs
    private static Map<String, Object> arguments() {
        Map<String, Object> rows = new HashMap<>(Map.of(
            "scopeIds", List.of("requirements"),
            "includeNestedElements", true,
            "elementTypes", List.of("Requirement"),
            "stereotypes", List.of()));
        Map<String, Object> columns = new HashMap<>(Map.of(
            "scopeIds", List.of(),
            "includeNestedElements", true,
            "elementTypes", List.of("Class"),
            "stereotypes", List.of("block")));
        return new HashMap<>(Map.of(
            "rows", rows,
            "columns", columns,
            "relationshipKinds", List.of("satisfy", "derive_reqt"),
            "direction", "column_to_row",
            "showElements", "rows_without_relationships",
            "includeImpliedRelationships", true,
            "groupByPackage", false,
            "listRelationships", false));
    }

    @Test
    void generateSchema_ok_writesTheRowAndColumnCriteriaWhereTheyAreUsed() throws Exception {
        String schemaText = SchemaSupport.generateSchema(RelationshipMatrixDTO.class);
        Map<String, Object> schema = schema();

        // A client that cannot follow $ref would see no criteria at all
        assertNull(schema.get("$defs"), "The criteria must not become named definitions: " + schemaText);
        assertFalse(schemaText.contains("$ref"), schemaText);

        for (String axis : List.of("rows", "columns")) {
            Map<String, Object> criteria = object(schema, "properties", axis);
            assertEquals("object", criteria.get("type"), axis);
            assertEquals(Set.of("scopeIds", "includeNestedElements", "elementTypes", "stereotypes"), object(criteria, "properties").keySet(), axis);
            assertEquals(Set.of("scopeIds", "includeNestedElements", "elementTypes", "stereotypes"), Set.copyOf((List<?>) criteria.get("required")), axis);
        }
    }

    @Test
    void generateSchema_ok_hasSixteenPropertiesEveryOneRequired() throws Exception {
        Map<String, Object> schema = schema();

        Set<String> topLevel = object(schema, "properties").keySet();
        assertEquals(Set.of("rows", "columns", "relationshipKinds", "direction", "showElements", "includeImpliedRelationships", "groupByPackage", "listRelationships"), topLevel);
        assertEquals(topLevel, Set.copyOf((List<?>) schema.get("required")));

        int nested = object(schema, "properties", "rows", "properties").size() + object(schema, "properties", "columns", "properties").size();
        assertEquals(16, topLevel.size() + nested);
    }

    @Test
    void generateSchema_ok_namesTheValuesOfEachEnumInLowercase() throws Exception {
        Map<String, Object> schema = schema();

        assertEquals(
            Arrays.stream(MatrixRelationshipKind.values()).map(MatrixRelationshipKind::label).toList(),
            object(schema, "properties", "relationshipKinds", "items").get("enum"));
        assertEquals(
            List.of("row_to_column", "column_to_row", "both"),
            object(schema, "properties", "direction").get("enum"));
        assertEquals(
            List.of("all", "with_relationships", "without_relationships", "rows_without_relationships", "columns_without_relationships"),
            object(schema, "properties", "showElements").get("enum"));
    }

    // What the matrix shows of a kind is the very value the tool takes, so that an agent can pass it back
    @Test
    void label_ok_isTheValueTheToolTakes() throws Exception {
        for (MatrixRelationshipKind kind : MatrixRelationshipKind.values()) {
            assertEquals("\"" + kind.label() + "\"", JsonSupport.OBJ_MAPPER.writeValueAsString(kind));
        }
        for (MatrixDirection direction : MatrixDirection.values()) {
            assertEquals("\"" + direction.label() + "\"", JsonSupport.OBJ_MAPPER.writeValueAsString(direction));
        }
        for (MatrixShowElements showElements : MatrixShowElements.values()) {
            assertEquals("\"" + showElements.label() + "\"", JsonSupport.OBJ_MAPPER.writeValueAsString(showElements));
        }
    }

    @Test
    void bind_ok_readsTheArgumentsAnAgentSends() {
        DtoBinder.BindResult<RelationshipMatrixDTO> result = DtoBinder.bind(arguments(), RelationshipMatrixDTO.class);

        assertNull(result.error(), () -> String.valueOf(result.error()));
        RelationshipMatrixDTO dto = result.dto();
        assertEquals(new MatrixRowCriteriaDTO(List.of("requirements"), true, List.of("Requirement"), List.of()), dto.rows());
        assertEquals(new MatrixColumnCriteriaDTO(List.of(), true, List.of("Class"), List.of("block")), dto.columns());
        assertEquals(List.of(MatrixRelationshipKind.SATISFY, MatrixRelationshipKind.DERIVE_REQT), dto.relationshipKinds());
        assertEquals(MatrixDirection.COLUMN_TO_ROW, dto.direction());
        assertEquals(MatrixShowElements.ROWS_WITHOUT_RELATIONSHIPS, dto.showElements());
        assertTrue(dto.includeImpliedRelationships());
        assertFalse(dto.groupByPackage());
        assertFalse(dto.listRelationships());
    }

    @Test
    @SuppressWarnings("unchecked")
    void bind_ng_rejectsAMissingProperty() {
        Map<String, Object> arguments = arguments();
        ((Map<String, Object>) arguments.get("rows")).remove("stereotypes");

        DtoBinder.BindResult<RelationshipMatrixDTO> result = DtoBinder.bind(arguments, RelationshipMatrixDTO.class);

        assertNotNull(result.error());
        assertTrue(result.error().isError());
    }

    @Test
    void bind_ng_rejectsAnUnknownKind() {
        Map<String, Object> arguments = arguments();
        arguments.put("relationshipKinds", List.of("satisfies"));

        DtoBinder.BindResult<RelationshipMatrixDTO> result = DtoBinder.bind(arguments, RelationshipMatrixDTO.class);

        assertNotNull(result.error());
        assertTrue(result.error().isError());
    }
}
