package com.astahpromcp.tool;

import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.PointIntDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DtoBinderTest {

    private static String errorText(DtoBinder.BindResult<?> result) {
        return ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.error().content().get(0)).text();
    }

    @Test
    void bind_ok() {
        DtoBinder.BindResult<IdDTO> result = DtoBinder.bind(Map.of("id", "abc"), IdDTO.class);

        assertNull(result.error());
        assertNotNull(result.dto());
        assertEquals("abc", result.dto().id());
    }

    @Test
    void bind_ok_noInput() {
        // Tools taking NoInputDTO may receive no arguments at all
        DtoBinder.BindResult<NoInputDTO> result = DtoBinder.bind(null, NoInputDTO.class);

        assertNull(result.error());
        assertNotNull(result.dto());
    }

    @Test
    void bind_ng_unconvertibleValue() {
        DtoBinder.BindResult<IdDTO> result = DtoBinder.bind(Map.of("id", Map.of("nested", "value")), IdDTO.class);

        assertNull(result.dto());
        assertNotNull(result.error());
        assertTrue(result.error().isError());
    }

    @Test
    void bind_ng_unknownProperty() {
        // A superfluous argument must fail fast instead of being silently ignored.
        DtoBinder.BindResult<IdDTO> result = DtoBinder.bind(Map.of("id", "abc", "extra", "x"), IdDTO.class);

        assertNull(result.dto());
        assertNotNull(result.error());
        assertTrue(result.error().isError());

        // The error message must name the offending property so that the AI agent can self-correct
        assertTrue(errorText(result).contains("extra"),
                "error message should contain the unknown property name: " + errorText(result));
    }

    @Test
    void bind_ng_missingRequiredProperty() {
        // Every schema property is declared required; omitting one must fail instead of silently binding null
        DtoBinder.BindResult<IdDTO> result = DtoBinder.bind(Map.of(), IdDTO.class);

        assertNull(result.dto());
        assertNotNull(result.error());
        assertTrue(result.error().isError());

        // The error message must name the missing property so that the AI agent can self-correct
        assertTrue(errorText(result).contains("id"),
                "error message should name the missing property: " + errorText(result));
    }

    @Test
    void bind_ng_missingOneOfSeveralProperties() {
        // A missing primitive creator property (y) must be rejected rather than defaulted to 0
        DtoBinder.BindResult<PointIntDTO> result = DtoBinder.bind(Map.of("x", 10), PointIntDTO.class);

        assertNull(result.dto());
        assertNotNull(result.error());
        assertTrue(result.error().isError());
        assertTrue(errorText(result).contains("y"),
                "error message should name the missing property: " + errorText(result));
    }

    @Test
    void bind_ok_allPropertiesPresent() {
        DtoBinder.BindResult<PointIntDTO> result = DtoBinder.bind(Map.of("x", 10, "y", 20), PointIntDTO.class);

        assertNull(result.error());
        assertNotNull(result.dto());
        assertEquals(10, result.dto().x());
        assertEquals(20, result.dto().y());
    }

    @Test
    void bind_ok_emptyRecordWithEmptyArgs() {
        // An empty record has no creator properties, so missing-property detection must not break it
        DtoBinder.BindResult<NoInputDTO> result = DtoBinder.bind(Map.of(), NoInputDTO.class);

        assertNull(result.error());
        assertNotNull(result.dto());
    }
}
