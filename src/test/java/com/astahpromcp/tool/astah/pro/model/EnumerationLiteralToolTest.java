package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.EnumerationLiteralWithValueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EnumerationLiteralDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IEnumerationLiteral;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class EnumerationLiteralToolTest {

    private ProjectAccessor projectAccessor;
    private EnumerationLiteralTool tool;
    private Method setValue;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/EnumerationLiteralToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new EnumerationLiteralTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // setValue() method
        setValue = TestSupport.getAccessibleMethod(
            EnumerationLiteralTool.class,
            "setValue",
            McpSyncServerExchange.class,
            EnumerationLiteralWithValueDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void setValue_ok() throws Exception {
        // Get enumeration literal
        IEnumerationLiteral enumerationLiteral = (IEnumerationLiteral) TestSupport.instance().getNamedElement(
            IEnumerationLiteral.class,
            "Enumeration Literal0");
        
        // Create input DTO
        EnumerationLiteralWithValueDTO inputDTO = new EnumerationLiteralWithValueDTO(
            enumerationLiteral.getId(),
            "testValue");

        // Check value before setting
        assertNotEquals("testValue", enumerationLiteral.getValue());

        // ----------------------------------------
        // Call setValue()
        // ----------------------------------------
        EnumerationLiteralDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setValue,
            tool,
            inputDTO,
            EnumerationLiteralDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check value after setting
        assertEquals("testValue", enumerationLiteral.getValue());
    }
}
