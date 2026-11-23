package com.astahpromcp.tool;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ToolSupportTest {
    
    private record TestOutputDTO(String value) {
    }
    
    private record TestInputDTO(String id, int count) {
    }
    
    private static final Map<String, Object> TEST_ARGUMENTS = Map.of("id", "class-1", "count", 3);
    
    private ListAppender<ILoggingEvent> captured;
    private Level originalLevel;
    
    @BeforeEach
    void attachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(ToolSupport.class);
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        captured = new ListAppender<>();
        captured.start();
        logger.addAppender(captured);
    }
    
    @AfterEach
    void detachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(ToolSupport.class);
        logger.detachAppender(captured);
        logger.setLevel(originalLevel);
        captured.stop();
    }
    
    // The input line is the only record of a call's input, so it must name the tool and carry every field on one line
    private void assertInputLoggedOnce(String toolName) {
        List<ILoggingEvent> inputEvents = captured.list.stream()
                .filter(event -> event.getFormattedMessage().startsWith("Tool input of "))
                .toList();
        
        assertEquals(1, inputEvents.size(), "One call must log its input exactly once");
        assertEquals(Level.DEBUG, inputEvents.get(0).getLevel());
        // The values the line is made of: the tool name, and the input with every field by name
        Object[] arguments = inputEvents.get(0).getArgumentArray();
        assertEquals(toolName, arguments[0]);
        String input = String.valueOf(arguments[1]);
        assertTrue(input.contains("count=3"), input);
        assertTrue(input.contains("id=class-1"), input);
    }
    
    private List<ILoggingEvent> eventsAt(Level level) {
        return captured.list.stream()
                .filter(event -> event.getLevel() == level)
                .toList();
    }
    
    // The text that a failed call returns to the client
    private static String errorText(McpSchema.CallToolResult result) {
        return ((McpSchema.TextContent) result.content().get(0)).text();
    }
    
    // An invalid input is the caller's mistake, so it stays out of ERROR, and its WARN line carries no stack trace
    private void assertRejectedInputLogged(String message) {
        assertTrue(eventsAt(Level.ERROR).isEmpty(), "An invalid input must not be logged as ERROR");
        
        List<ILoggingEvent> warnings = eventsAt(Level.WARN);
        assertEquals(1, warnings.size());
        assertEquals(message, warnings.get(0).getFormattedMessage());
        assertNull(warnings.get(0).getThrowableProxy());
        
        // The stack trace is still kept at DEBUG
        assertTrue(eventsAt(Level.DEBUG).stream().anyMatch(event -> event.getThrowableProxy() != null
                && IllegalArgumentException.class.getName().equals(event.getThrowableProxy().getClassName())));
    }
    
    @Test
    void toolDefinitionReturningDto_ok_toolFunctionDoesNotRequireExchange() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDto(
                "test_dto",
                "test",
                input -> new TestOutputDTO("dto"),
                NoInputDTO.class,
                TestOutputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto", Map.of(), null));
        
        assertFalse(result.isError());
        assertEquals(Map.of("value", "dto"), result.structuredContent());
    }
    
    @Test
    void toolDefinitionReturningContents_ok_toolFunctionDoesNotRequireExchange() {
        McpSchema.TextContent text = McpSchema.TextContent.builder("contents").build();
        ToolDefinition definition = ToolSupport.toolDefinitionReturningContents(
                "test_contents",
                "test",
                input -> List.of(text),
                NoInputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_contents", Map.of(), null));
        
        assertFalse(result.isError());
        assertEquals(List.of(text), result.content());
    }
    
    @Test
    void toolDefinitionReturningDtoAndContents_ok_toolFunctionDoesNotRequireExchange() {
        McpSchema.TextContent text = McpSchema.TextContent.builder("contents").build();
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDtoAndContents(
                "test_dto_and_contents",
                "test",
                input -> Pair.of(new TestOutputDTO("dto"), List.of(text)),
                NoInputDTO.class,
                TestOutputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto_and_contents", Map.of(), null));
        
        assertFalse(result.isError());
        assertEquals(Map.of("value", "dto"), result.structuredContent());
        assertEquals(List.of(text), result.content());
    }
    
    @Test
    void toolDefinitionReturningDto_ok_logsTheInputOnceOnOneLine() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDto(
                "test_dto",
                "test",
                input -> new TestOutputDTO("dto"),
                TestInputDTO.class,
                TestOutputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto", TEST_ARGUMENTS, null));
        
        assertFalse(result.isError());
        assertInputLoggedOnce("test_dto");
    }
    
    @Test
    void toolDefinitionReturningContents_ok_logsTheInputOnceOnOneLine() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningContents(
                "test_contents",
                "test",
                input -> List.of(McpSchema.TextContent.builder("contents").build()),
                TestInputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_contents", TEST_ARGUMENTS, null));
        
        assertFalse(result.isError());
        assertInputLoggedOnce("test_contents");
    }
    
    @Test
    void toolDefinitionReturningDtoAndContents_ok_logsTheInputOnceOnOneLine() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDtoAndContents(
                "test_dto_and_contents",
                "test",
                input -> Pair.of(new TestOutputDTO("dto"), List.of()),
                TestInputDTO.class,
                TestOutputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto_and_contents", TEST_ARGUMENTS, null));
        
        assertFalse(result.isError());
        assertInputLoggedOnce("test_dto_and_contents");
    }
    
    @Test
    void toolDefinitionReturningDtoAndContents_ok_declaresTheSameOutputSchemaAsForTheDtoAlone() {
        ToolDefinition withContents = ToolSupport.toolDefinitionReturningDtoAndContents(
                "test_dto_and_contents",
                "test",
                input -> Pair.of(new TestOutputDTO("dto"), List.of()),
                NoInputDTO.class,
                TestOutputDTO.class);
        ToolDefinition dtoAlone = ToolSupport.toolDefinitionReturningDto(
                "test_dto",
                "test",
                input -> new TestOutputDTO("dto"),
                NoInputDTO.class,
                TestOutputDTO.class);
        
        Map<String, Object> outputSchema = withContents.toolSchema().outputSchema();
        
        assertNotNull(outputSchema);
        assertEquals(dtoAlone.toolSchema().outputSchema(), outputSchema);
        assertTrue(((Map<?, ?>) outputSchema.get("properties")).containsKey("value"), String.valueOf(outputSchema));
    }
    
    @Test
    void toolDefinitionReturningDto_ng_illegalArgumentIsLoggedAsWarning() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDto(
                "test_dto",
                "test",
                input -> { throw new IllegalArgumentException("Failed to get class (ID: x) due to an incorrect ID."); },
                NoInputDTO.class,
                TestOutputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto", Map.of(), null));
        
        assertTrue(result.isError());
        assertTrue(errorText(result).contains("test_dto"), errorText(result));
        assertTrue(errorText(result).contains("Failed to get class (ID: x) due to an incorrect ID."), errorText(result));
        assertRejectedInputLogged(errorText(result));
    }
    
    @Test
    void toolDefinitionReturningContents_ng_illegalArgumentIsLoggedAsWarning() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningContents(
                "test_contents",
                "test",
                input -> { throw new IllegalArgumentException("bad input"); },
                NoInputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_contents", Map.of(), null));
        
        assertTrue(result.isError());
        assertTrue(errorText(result).contains("test_contents"), errorText(result));
        assertTrue(errorText(result).contains("bad input"), errorText(result));
        assertRejectedInputLogged(errorText(result));
    }
    
    @Test
    void toolDefinitionReturningDtoAndContents_ng_otherFailureIsLoggedAsErrorWithStackTrace() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDtoAndContents(
                "test_dto_and_contents",
                "test",
                input -> { throw new IllegalStateException("broken"); },
                NoInputDTO.class,
                TestOutputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto_and_contents", Map.of(), null));
        
        assertTrue(result.isError());
        assertTrue(errorText(result).contains("test_dto_and_contents"), errorText(result));
        assertTrue(errorText(result).contains("broken"), errorText(result));
        
        // Anything but an invalid input may be a fault of the server, so it stays ERROR with its stack trace
        assertTrue(eventsAt(Level.WARN).isEmpty());
        List<ILoggingEvent> errors = eventsAt(Level.ERROR);
        assertEquals(1, errors.size());
        assertEquals(errorText(result), errors.get(0).getFormattedMessage());
        assertNotNull(errors.get(0).getThrowableProxy());
    }

    // A tool function that answers with no result is a fault of this server, so the client gets a failure and the log keeps it at ERROR
    private void assertNoResultReported(McpSchema.CallToolResult result, String toolName) {
        assertTrue(result.isError());
        assertEquals("Failure @tool=" + toolName, errorText(result));

        assertTrue(eventsAt(Level.WARN).isEmpty());
        List<ILoggingEvent> errors = eventsAt(Level.ERROR);
        assertEquals(1, errors.size());
        assertEquals(errorText(result), errors.get(0).getFormattedMessage());
    }

    @Test
    void toolDefinitionReturningDto_ng_nullOutputIsReportedAsFailure() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDto(
                "test_dto",
                "test",
                input -> null,
                NoInputDTO.class,
                TestOutputDTO.class);

        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto", Map.of(), null));

        assertNoResultReported(result, "test_dto");
    }

    @Test
    void toolDefinitionReturningContents_ng_nullContentsIsReportedAsFailure() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningContents(
                "test_contents",
                "test",
                input -> null,
                NoInputDTO.class);

        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_contents", Map.of(), null));

        assertNoResultReported(result, "test_contents");
    }

    @Test
    void toolDefinitionReturningDtoAndContents_ng_nullResultIsReportedAsFailure() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDtoAndContents(
                "test_dto_and_contents",
                "test",
                input -> null,
                NoInputDTO.class,
                TestOutputDTO.class);

        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto_and_contents", Map.of(), null));

        assertNoResultReported(result, "test_dto_and_contents");
    }

    @Test
    void toolDefinitionReturningDtoAndContents_ng_nullDtoIsReportedAsFailureEvenWithContents() {
        // The DTO is what the declared output schema describes, so contents alone do not make a result
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDtoAndContents(
                "test_dto_and_contents",
                "test",
                input -> Pair.of(null, List.of(McpSchema.TextContent.builder("contents").build())),
                NoInputDTO.class,
                TestOutputDTO.class);

        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto_and_contents", Map.of(), null));

        assertNoResultReported(result, "test_dto_and_contents");
    }

    @Test
    void toolDefinitionReturningDto_ng_unbindableArgumentsAreLoggedAsWarning() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDto(
                "test_dto",
                "test",
                input -> new TestOutputDTO("dto"),
                TestInputDTO.class,
                TestOutputDTO.class);
        
        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto", Map.of("id", "class-1", "count", 3, "extra", "x"), null));
        
        assertTrue(result.isError());
        assertTrue(eventsAt(Level.ERROR).isEmpty(), "Unbindable arguments must not be logged as ERROR");
        
        List<ILoggingEvent> warnings = eventsAt(Level.WARN);
        assertEquals(1, warnings.size());
        assertNull(warnings.get(0).getThrowableProxy());
        // Without a stack trace, the line itself has to say what was wrong with the arguments
        String warning = warnings.get(0).getFormattedMessage();
        assertTrue(warning.startsWith("Failed to convert arguments into " + TestInputDTO.class.getName() + ": "), warning);
        assertTrue(warning.contains("extra"), warning);
    }
    
    // A DTO class that Jackson cannot bind, whatever the arguments are
    private interface UnbindableDTO {
    }
    
    @Test
    void bind_ng_unbindableDtoClassIsLoggedAsError() {
        DtoBinder.BindResult<UnbindableDTO> result = DtoBinder.bind(Map.of(), UnbindableDTO.class);
        
        assertNull(result.dto());
        assertTrue(result.error().isError());
        
        // The DTO class itself is broken, which is a fault of this server rather than of the caller's arguments
        assertTrue(eventsAt(Level.WARN).isEmpty());
        List<ILoggingEvent> errors = eventsAt(Level.ERROR);
        assertEquals(1, errors.size());
        assertNotNull(errors.get(0).getThrowableProxy());
    }
    
    @Test
    void summarizeContents_ok_imageContentIsReducedToItsSize() {
        String base64Data = "iVBORw0KGgo=".repeat(1000);  // Pseudo large Base64 payload
        McpSchema.ImageContent image = McpSchema.ImageContent.builder(base64Data, "image/png").build();
        
        String summary = ToolSupport.summarizeContents(List.of(image));
        
        // The raw Base64 data must not be dumped into the log
        assertFalse(summary.contains(base64Data));
        // The summary must carry the information useful for troubleshooting
        assertTrue(summary.contains("image/png"));
        assertTrue(summary.contains(base64Data.length() + " chars"));
    }
    
    @Test
    void summarizeContents_ok_textContentIsKeptAsIs() {
        McpSchema.TextContent text = McpSchema.TextContent.builder("{\"id\":\"abc\"}").build();
        
        String summary = ToolSupport.summarizeContents(List.of(text));
        
        assertTrue(summary.contains("{\"id\":\"abc\"}"));
    }
    
    @Test
    void summarizeContents_ng_null() {
        assertEquals("null", ToolSupport.summarizeContents(null));
    }
}
