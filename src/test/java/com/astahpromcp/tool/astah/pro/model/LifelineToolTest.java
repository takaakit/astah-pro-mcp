package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LifelineWithBaseClassDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LifelineWithLengthDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LifelineDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class LifelineToolTest {

    private ProjectAccessor projectAccessor;
    private LifelineTool tool;
    private Method getInfo;
    private Method setBaseClass;
    private Method setLength;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/LifelineToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new LifelineTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            LifelineTool.class,
            "getInfo",
            IdDTO.class);

        // setBaseClass() method
        setBaseClass = TestSupport.getAccessibleMethod(
            LifelineTool.class,
            "setBaseClass",
            LifelineWithBaseClassDTO.class);

        // setLength() method
        setLength = TestSupport.getAccessibleMethod(
            LifelineTool.class,
            "setLength",
            LifelineWithLengthDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get lifeline - assuming there's a lifeline in the test model
        ILifeline lifeline = (ILifeline) TestSupport.instance().getNamedElementByClassAndName(
            ILifeline.class,
            "foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(lifeline.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LifelineDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            LifelineDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setBaseClass_ok() throws Exception {
        // Get lifeline
        ILifeline lifeline = (ILifeline) TestSupport.instance().getNamedElementByClassAndName(
            ILifeline.class,
            "foo");
        
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");

        // Create input DTO
        LifelineWithBaseClassDTO inputDTO = new LifelineWithBaseClassDTO(
            lifeline.getId(),
            clazz.getId());

        // Check base class before setting
        assertNotEquals(clazz, lifeline.getBase());

        // ----------------------------------------
        // Call setBaseClass()
        // ----------------------------------------
        LifelineDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setBaseClass,
            tool,
            inputDTO,
            LifelineDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check base class after setting
        assertEquals(clazz, lifeline.getBase());
    }

    @Test
    void setLength_ok() throws Exception {
        // Get lifeline
        ILifeline lifeline = (ILifeline) TestSupport.instance().getNamedElementByClassAndName(
            ILifeline.class,
            "foo");

        // Create input DTO
        LifelineWithLengthDTO inputDTO = new LifelineWithLengthDTO(
            lifeline.getId(),
            100);

        // ----------------------------------------
        // Call setLength()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setLength,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check length after setting
        String lengthProperty = lifeline.getPresentations()[0].getProperty(Key.LIFELINE_LENGTH);
        assertNotNull(lengthProperty);
        assertEquals(100, (int) Double.parseDouble(lengthProperty));
    }
}
