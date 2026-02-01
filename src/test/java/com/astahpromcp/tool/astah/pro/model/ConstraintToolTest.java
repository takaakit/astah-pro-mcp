package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ConstraintWithSpecificationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ConstraintDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ConstraintToolTest {

    private ProjectAccessor projectAccessor;
    private ConstraintTool tool;
    private Method getInfo;
    private Method setSpecification;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ConstraintToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ConstraintTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ConstraintTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setSpecification() method
        setSpecification = TestSupport.getAccessibleMethod(
            ConstraintTool.class,
            "setSpecification",
            McpSyncServerExchange.class,
            ConstraintWithSpecificationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Get constraint from class
        IConstraint constraint = clazz.getConstraints()[0];
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(constraint.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ConstraintDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ConstraintDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("specification contents", outputDTO.specificationContents());
    }

    @Test
    void setSpecification_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Get constraint from class
        IConstraint constraint = clazz.getConstraints()[0];
        
        // Create input DTO
        String newSpecification = "new specification contents";
        ConstraintWithSpecificationDTO inputDTO = new ConstraintWithSpecificationDTO(
            constraint.getId(),
            newSpecification);

        // Get specification before setting
        String oldSpecification = constraint.getSpecification();

        // ----------------------------------------
        // Call setSpecification()
        // ----------------------------------------
        ConstraintDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setSpecification,
            tool,
            inputDTO,
            ConstraintDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(newSpecification, outputDTO.specificationContents());

        // Check specification after setting
        assertNotEquals(oldSpecification, constraint.getSpecification());
        assertEquals(newSpecification, constraint.getSpecification());
    }
}
