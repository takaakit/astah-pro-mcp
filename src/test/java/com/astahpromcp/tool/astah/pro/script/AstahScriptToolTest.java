package com.astahpromcp.tool.astah.pro.script;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.SchemaSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.script.inputdto.RunScriptDTO;
import com.astahpromcp.tool.astah.pro.script.outputdto.ScriptResultDTO;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class AstahScriptToolTest {

    private ProjectAccessor projectAccessor;
    private AstahScriptTool tool;
    private Method runScript;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.create();

        // Tool
        tool = new AstahScriptTool(projectAccessor, true);

        // runScript() method
        runScript = TestSupport.getAccessibleMethod(
            AstahScriptTool.class,
            "runScript",
            RunScriptDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void runScript_ok_print() throws Exception {
        // Create input DTO
        RunScriptDTO inputDTO = new RunScriptDTO("print('hello from script');");

        // ----------------------------------------
        // Call runScript()
        // ----------------------------------------
        ScriptResultDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            runScript,
            tool,
            inputDTO,
            ScriptResultDTO.class);

        // Check the output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.ok());
        assertTrue(outputDTO.stdout().contains("hello from script"));
        assertEquals("", outputDTO.errorMessage());
    }

    @Test
    void runScript_ok_lastExpressionValue() throws Exception {
        // Create input DTO
        RunScriptDTO inputDTO = new RunScriptDTO("1 + 2");

        // ----------------------------------------
        // Call runScript()
        // ----------------------------------------
        ScriptResultDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            runScript,
            tool,
            inputDTO,
            ScriptResultDTO.class);

        // Check the output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.ok());
        assertEquals("3", outputDTO.result());
    }

    @Test
    void runScript_ok_astahBinding() throws Exception {
        // Create input DTO
        RunScriptDTO inputDTO = new RunScriptDTO("astah.getProject().getName()");

        // ----------------------------------------
        // Call runScript()
        // ----------------------------------------
        ScriptResultDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            runScript,
            tool,
            inputDTO,
            ScriptResultDTO.class);

        // Check the output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.ok());
        assertNotNull(outputDTO.result());
    }

    @Test
    void runScript_ok_editModel() throws Exception {
        // Create input DTO
        String script = """
            var TransactionManager = Java.type('com.change_vision.jude.api.inf.editor.TransactionManager');
            try {
                TransactionManager.beginTransaction();
                var clazz = astah.getModelEditorFactory().getBasicModelEditor().createClass(astah.getProject(), 'ScriptCreatedClass');
                TransactionManager.endTransaction();
            } catch (e) {
                TransactionManager.abortTransaction();
                throw e;
            }
            clazz.getName();
            """;
        RunScriptDTO inputDTO = new RunScriptDTO(script);

        // ----------------------------------------
        // Call runScript()
        // ----------------------------------------
        ScriptResultDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            runScript,
            tool,
            inputDTO,
            ScriptResultDTO.class);

        // Check the output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.ok());
        assertEquals("ScriptCreatedClass", outputDTO.result());

        // Check the model
        INamedElement createdClass = TestSupport.instance().getNamedElementByClassAndName(IClass.class, "ScriptCreatedClass");
        assertNotNull(createdClass);
    }

    @Test
    void runScript_ng_syntaxErrorReported() throws Exception {
        // Create input DTO
        RunScriptDTO inputDTO = new RunScriptDTO("var ;");

        // ----------------------------------------
        // Call runScript()
        // ----------------------------------------
        ScriptResultDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            runScript,
            tool,
            inputDTO,
            ScriptResultDTO.class);

        // Check the output DTO
        assertNotNull(outputDTO);
        assertFalse(outputDTO.ok());
        assertNotNull(outputDTO.errorMessage());
        assertEquals(1, outputDTO.errorLine());
    }

    @Test
    void runScript_ng_danglingTransactionAborted() throws Exception {
        // Create input DTO
        String script = """
            var TransactionManager = Java.type('com.change_vision.jude.api.inf.editor.TransactionManager');
            TransactionManager.beginTransaction();
            astah.getModelEditorFactory().getBasicModelEditor().createClass(astah.getProject(), 'DanglingClass');
            """;
        RunScriptDTO inputDTO = new RunScriptDTO(script);

        // ----------------------------------------
        // Call runScript()
        // ----------------------------------------
        ScriptResultDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            runScript,
            tool,
            inputDTO,
            ScriptResultDTO.class);

        // Check the output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.stderr().contains("aborted"));

        // Check that no transaction is left open
        assertFalse(TransactionManager.isInTransaction());
    }
}
