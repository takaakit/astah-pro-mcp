package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.AssociationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class AssociationToolTest {

    private ProjectAccessor projectAccessor;
    private AssociationTool tool;
    private Method getAssociationInfo;
    private Method getAssociationEndAInfo;
    private Method getAssociationEndBInfo;
    private Method setInitialValueOfAssociationEndA;
    private Method setInitialValueOfAssociationEndB;
    private Method setStaticOfAssociationEndA;
    private Method setStaticOfAssociationEndB;
    private Method setMultiplicityByIntOfAssociationEndA;
    private Method setMultiplicityByIntOfAssociationEndB;
    private Method setAggregationKindOfAssociationEndA;
    private Method setAggregationKindOfAssociationEndB;
    private Method setNavigabilityOfAssociationEndA;
    private Method setNavigabilityOfAssociationEndB;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/AssociationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new AssociationTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getAssociationInfo() method
        getAssociationInfo = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "getAssociationInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // getAssociationEndAInfo() method
        getAssociationEndAInfo = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "getAssociationEndAInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // getAssociationEndBInfo() method
        getAssociationEndBInfo = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "getAssociationEndBInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setInitialValueOfAssociationEndA() method
        setInitialValueOfAssociationEndA = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setInitialValueOfAssociationEndA",
            McpSyncServerExchange.class,
            AttributeWithInitialValueDTO.class);

        // setInitialValueOfAssociationEndB() method
        setInitialValueOfAssociationEndB = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setInitialValueOfAssociationEndB",
            McpSyncServerExchange.class,
            AttributeWithInitialValueDTO.class);

        // setStaticOfAssociationEndA() method
        setStaticOfAssociationEndA = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setStaticOfAssociationEndA",
            McpSyncServerExchange.class,
            AttributeWithStaticDTO.class);

        // setStaticOfAssociationEndB() method
        setStaticOfAssociationEndB = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setStaticOfAssociationEndB",
            McpSyncServerExchange.class,
            AttributeWithStaticDTO.class);

        // setMultiplicityByIntOfAssociationEndA() method
        setMultiplicityByIntOfAssociationEndA = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setMultiplicityByIntOfAssociationEndA",
            McpSyncServerExchange.class,
            AttributeWithIntMultiplicityDTO.class);

        // setMultiplicityByIntOfAssociationEndB() method
        setMultiplicityByIntOfAssociationEndB = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setMultiplicityByIntOfAssociationEndB",
            McpSyncServerExchange.class,
            AttributeWithIntMultiplicityDTO.class);

        // setAggregationKindOfAssociationEndA() method
        setAggregationKindOfAssociationEndA = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setAggregationKindOfAssociationEndA",
            McpSyncServerExchange.class,
            AssociationEndWithAggregationKindDTO.class);

        // setAggregationKindOfAssociationEndB() method
        setAggregationKindOfAssociationEndB = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setAggregationKindOfAssociationEndB",
            McpSyncServerExchange.class,
            AssociationEndWithAggregationKindDTO.class);

        // setNavigabilityOfAssociationEndA() method
        setNavigabilityOfAssociationEndA = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setNavigabilityOfAssociationEndA",
            McpSyncServerExchange.class,
            AssociationEndWithNavigabilityDTO.class);

        // setNavigabilityOfAssociationEndB() method
        setNavigabilityOfAssociationEndB = TestSupport.getAccessibleMethod(
            AssociationTool.class,
            "setNavigabilityOfAssociationEndB",
            McpSyncServerExchange.class,
            AssociationEndWithNavigabilityDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getAssociationInfo_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(association.getId());

        // ----------------------------------------
        // Call getAssociationInfo()
        // ----------------------------------------
        AssociationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getAssociationInfo,
            tool,
            inputDTO,
            AssociationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getAssociationEndAInfo_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end A (first end)
        String associationEndAId = association.getMemberEnds()[0].getId();

        // Create input DTO
        IdDTO inputDTO = new IdDTO(associationEndAId);

        // ----------------------------------------
        // Call getAssociationEndAInfo()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getAssociationEndAInfo,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getAssociationEndBInfo_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end B (second end)
        String associationEndBId = association.getMemberEnds()[1].getId();

        // Create input DTO
        IdDTO inputDTO = new IdDTO(associationEndBId);

        // ----------------------------------------
        // Call getAssociationEndBInfo()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getAssociationEndBInfo,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setInitialValueOfAssociationEndA_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end A
        String associationEndAId = association.getMemberEnds()[0].getId();

        // Create input DTO
        AttributeWithInitialValueDTO inputDTO = new AttributeWithInitialValueDTO(
            associationEndAId,
            "888");
        
        // Check initial value before setting
        assertNotEquals("888", association.getMemberEnds()[0].getInitialValue());

        // ----------------------------------------
        // Call setInitialValueOfAssociationEndA()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setInitialValueOfAssociationEndA,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check initial value after setting
        assertEquals("888", association.getMemberEnds()[0].getInitialValue());
    }

    @Test
    void setInitialValueOfAssociationEndB_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end B
        String associationEndBId = association.getMemberEnds()[1].getId();

        // Create input DTO
        AttributeWithInitialValueDTO inputDTO = new AttributeWithInitialValueDTO(
            associationEndBId,
            "888");
        
        // Check initial value before setting
        assertNotEquals("888", association.getMemberEnds()[1].getInitialValue());

        // ----------------------------------------
        // Call setInitialValueOfAssociationEndB()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setInitialValueOfAssociationEndB,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check initial value after setting
        assertEquals("888", association.getMemberEnds()[1].getInitialValue());
    }

    @Test
    void setStaticOfAssociationEndA_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end A
        String associationEndAId = association.getMemberEnds()[0].getId();

        // Create input DTO
        AttributeWithStaticDTO inputDTO = new AttributeWithStaticDTO(
            associationEndAId,
            true);
        
        // Check static before setting
        assertFalse(association.getMemberEnds()[0].isStatic());

        // ----------------------------------------
        // Call setStaticOfAssociationEndA()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setStaticOfAssociationEndA,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check static after setting
        assertTrue(association.getMemberEnds()[0].isStatic());
    }

    @Test
    void setStaticOfAssociationEndB_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end B
        String associationEndBId = association.getMemberEnds()[1].getId();

        // Create input DTO
        AttributeWithStaticDTO inputDTO = new AttributeWithStaticDTO(
            associationEndBId,
            true);

        // Check static before setting
        assertFalse(association.getMemberEnds()[1].isStatic());

        // ----------------------------------------
        // Call setStaticOfAssociationEndB()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setStaticOfAssociationEndB,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check static after setting
        assertTrue(association.getMemberEnds()[1].isStatic());
    }

    @Test
    void setMultiplicityByIntOfAssociationEndA_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end A
        String associationEndAId = association.getMemberEnds()[0].getId();

        // Create input DTO
        AttributeWithIntMultiplicityDTO inputDTO = new AttributeWithIntMultiplicityDTO(
            associationEndAId,
            1,
            10);

        // Check multiplicity before setting
        assertNotEquals(1, association.getMemberEnds()[0].getMultiplicity()[0].getLower());
        assertNotEquals(10, association.getMemberEnds()[0].getMultiplicity()[0].getUpper());

        // ----------------------------------------
        // Call setMultiplicityByIntOfAssociationEndA()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setMultiplicityByIntOfAssociationEndA,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check multiplicity after setting
        assertEquals(1, association.getMemberEnds()[0].getMultiplicity()[0].getLower());
        assertEquals(10, association.getMemberEnds()[0].getMultiplicity()[0].getUpper());
    }

    @Test
    void setMultiplicityByIntOfAssociationEndB_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end B
        String associationEndBId = association.getMemberEnds()[1].getId();

        // Create input DTO
        AttributeWithIntMultiplicityDTO inputDTO = new AttributeWithIntMultiplicityDTO(
            associationEndBId,
            1,
            10);

        // Check multiplicity before setting
        assertNotEquals(1, association.getMemberEnds()[1].getMultiplicity()[0].getLower());
        assertNotEquals(10, association.getMemberEnds()[1].getMultiplicity()[0].getUpper());

        // ----------------------------------------
        // Call setMultiplicityByIntOfAssociationEndB()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setMultiplicityByIntOfAssociationEndB,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check multiplicity after setting
        assertEquals(1, association.getMemberEnds()[1].getMultiplicity()[0].getLower());
        assertEquals(10, association.getMemberEnds()[1].getMultiplicity()[0].getUpper());
    }

    @Test
    void setAggregationKindOfAssociationEndA_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end A
        String associationEndAId = association.getMemberEnds()[0].getId();

        // Create input DTO
        AssociationEndWithAggregationKindDTO inputDTO = new AssociationEndWithAggregationKindDTO(
            associationEndAId,
            com.astahpromcp.tool.astah.pro.common.AggregationKind.aggregate);

        // Check aggregation kind before setting
        assertFalse(association.getMemberEnds()[0].isAggregate());

        // ----------------------------------------
        // Call setAggregationKindOfAssociationEndA()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAggregationKindOfAssociationEndA,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check aggregation kind after setting
        assertTrue(association.getMemberEnds()[0].isAggregate());
    }

    @Test
    void setAggregationKindOfAssociationEndB_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end B
        String associationEndBId = association.getMemberEnds()[1].getId();

        // Create input DTO
        AssociationEndWithAggregationKindDTO inputDTO = new AssociationEndWithAggregationKindDTO(
            associationEndBId,
            com.astahpromcp.tool.astah.pro.common.AggregationKind.composite);

        // Check aggregation kind before setting
        assertFalse(association.getMemberEnds()[1].isComposite());

        // ----------------------------------------
        // Call setAggregationKindOfAssociationEndB()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAggregationKindOfAssociationEndB,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check aggregation kind after setting
        assertTrue(association.getMemberEnds()[1].isComposite());
    }

    @Test
    void setNavigabilityOfAssociationEndA_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end A
        String associationEndAId = association.getMemberEnds()[0].getId();

        // Create input DTO
        AssociationEndWithNavigabilityDTO inputDTO = new AssociationEndWithNavigabilityDTO(
            associationEndAId,
            com.astahpromcp.tool.astah.pro.common.NavigabilityKind.navigable);

        // Check navigability before setting
        assertNotEquals("Navigable", association.getMemberEnds()[0].getNavigability());

        // ----------------------------------------
        // Call setNavigabilityOfAssociationEndA()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNavigabilityOfAssociationEndA,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check navigability after setting
        assertEquals("Navigable", association.getMemberEnds()[0].getNavigability());
    }

    @Test
    void setNavigabilityOfAssociationEndB_ok() throws Exception {
        // Get association
        IAssociation association = (IAssociation) TestSupport.instance().getNamedElement(
            IAssociation.class,
            "");

        // Get association end B
        String associationEndBId = association.getMemberEnds()[1].getId();

        // Create input DTO
        AssociationEndWithNavigabilityDTO inputDTO = new AssociationEndWithNavigabilityDTO(
            associationEndBId,
            com.astahpromcp.tool.astah.pro.common.NavigabilityKind.non_navigable);

        // Check navigability before setting
        assertNotEquals("Non_Navigable", association.getMemberEnds()[1].getNavigability());

        // ----------------------------------------
        // Call setNavigabilityOfAssociationEndB()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNavigabilityOfAssociationEndB,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check navigability after setting
        assertEquals("Non_Navigable", association.getMemberEnds()[1].getNavigability());
    }
}
