package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithDefaultLengthPrecisionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithDescriptionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithLengthConstraintDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDatatypeWithPrecisionConstraintDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDatatypeDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERDatatype;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ERDatatypeToolTest {

    private ProjectAccessor projectAccessor;
    private ERDatatypeTool tool;
    private Method getInfo;
    private Method setLengthConstraint;
    private Method setPrecisionConstraint;
    private Method setDefaultLengthPrecision;
    private Method setDescription;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ERDatatypeToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERDatatypeTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ERDatatypeTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setLengthConstraint() method
        setLengthConstraint = TestSupport.getAccessibleMethod(
            ERDatatypeTool.class,
            "setLengthConstraint",
            McpSyncServerExchange.class,
            ERDatatypeWithLengthConstraintDTO.class);

        // setPrecisionConstraint() method
        setPrecisionConstraint = TestSupport.getAccessibleMethod(
            ERDatatypeTool.class,
            "setPrecisionConstraint",
            McpSyncServerExchange.class,
            ERDatatypeWithPrecisionConstraintDTO.class);

        // setDefaultLengthPrecision() method
        setDefaultLengthPrecision = TestSupport.getAccessibleMethod(
            ERDatatypeTool.class,
            "setDefaultLengthPrecision",
            McpSyncServerExchange.class,
            ERDatatypeWithDefaultLengthPrecisionDTO.class);

        // setDescription() method
        setDescription = TestSupport.getAccessibleMethod(
            ERDatatypeTool.class,
            "setDescription",
            McpSyncServerExchange.class,
            ERDatatypeWithDescriptionDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erDatatype.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ERDatatypeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ERDatatypeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erDatatype.getId(), outputDTO.namedElement().element().id());
        assertEquals(erDatatype.getName(), outputDTO.namedElement().name());
    }

    @Test
    void setLengthConstraint_ok() throws Exception {
        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        ERDatatypeWithLengthConstraintDTO inputDTO = new ERDatatypeWithLengthConstraintDTO(
            erDatatype.getId(),
            "Required");

        // ----------------------------------------
        // Call setLengthConstraint()
        // ----------------------------------------
        ERDatatypeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLengthConstraint,
            tool,
            inputDTO,
            ERDatatypeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Required", erDatatype.getLengthConstraint());
    }

    @Test
    void setPrecisionConstraint_ok() throws Exception {
        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        ERDatatypeWithPrecisionConstraintDTO inputDTO = new ERDatatypeWithPrecisionConstraintDTO(
            erDatatype.getId(),
            "Optional");

        // ----------------------------------------
        // Call setPrecisionConstraint()
        // ----------------------------------------
        ERDatatypeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setPrecisionConstraint,
            tool,
            inputDTO,
            ERDatatypeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Optional", erDatatype.getPrecisionConstraint());
    }

    @Test
    void setDefaultLengthPrecision_ok() throws Exception {
        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        ERDatatypeWithDefaultLengthPrecisionDTO inputDTO = new ERDatatypeWithDefaultLengthPrecisionDTO(
            erDatatype.getId(),
            "10,2");

        // ----------------------------------------
        // Call setDefaultLengthPrecision()
        // ----------------------------------------
        ERDatatypeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDefaultLengthPrecision,
            tool,
            inputDTO,
            ERDatatypeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("10,2", erDatatype.getDefaultLengthPrecision());
    }

    @Test
    void setDescription_ok() throws Exception {
        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        ERDatatypeWithDescriptionDTO inputDTO = new ERDatatypeWithDescriptionDTO(
            erDatatype.getId(),
            "Description_777");

        // ----------------------------------------
        // Call setDescription()
        // ----------------------------------------
        ERDatatypeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDescription,
            tool,
            inputDTO,
            ERDatatypeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Description_777", erDatatype.getDefinition());
    }
}
