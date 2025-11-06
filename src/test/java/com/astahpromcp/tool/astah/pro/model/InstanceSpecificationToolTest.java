package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InstanceSpecificationWithClassifierDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InstanceSpecificationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstanceSpecificationToolTest {

    private ProjectAccessor projectAccessor;
    private InstanceSpecificationTool tool;
    private Method getInfo;
    private Method setClassifier;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/InstanceSpecificationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new InstanceSpecificationTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            InstanceSpecificationTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setClassifier() method
        setClassifier = TestSupport.getAccessibleMethod(
            InstanceSpecificationTool.class,
            "setClassifier",
            McpSyncServerExchange.class,
            InstanceSpecificationWithClassifierDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get instance specification
        IInstanceSpecification instanceSpecification = (IInstanceSpecification) TestSupport.instance().getNamedElement(
            IInstanceSpecification.class,
            "foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(instanceSpecification.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        InstanceSpecificationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            InstanceSpecificationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setClassifier_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");
        
        // Get instance specification
        IInstanceSpecification instanceSpecification = (IInstanceSpecification) TestSupport.instance().getNamedElement(
            IInstanceSpecification.class,
            "foo");

        // Create input DTO
        InstanceSpecificationWithClassifierDTO inputDTO = new InstanceSpecificationWithClassifierDTO(
            clazz.getId(),
            instanceSpecification.getId());

        // ----------------------------------------
        // Call setClassifier()
        // ----------------------------------------
        InstanceSpecificationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setClassifier,
            tool,
            inputDTO,
            InstanceSpecificationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
