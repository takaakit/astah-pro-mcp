package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PartitionWithRepresentsDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PartitionDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IPartition;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class PartitionToolTest {

    private ProjectAccessor projectAccessor;
    private PartitionTool tool;
    private Method getInfo;
    private Method setRepresents;
    private Method removeRepresents;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/PartitionTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new PartitionTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            PartitionTool.class,
            "getInfo",
            IdDTO.class);

        // setRepresents() method
        setRepresents = TestSupport.getAccessibleMethod(
            PartitionTool.class,
            "setRepresents",
            PartitionWithRepresentsDTO.class);

        // removeRepresents() method
        removeRepresents = TestSupport.getAccessibleMethod(
            PartitionTool.class,
            "removeRepresents",
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get partition
        IPartition partition = (IPartition) TestSupport.instance().getNamedElementByClassAndName(
            IPartition.class,
            "Partition0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(partition.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        PartitionDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            PartitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setRepresents_ok() throws Exception {
        // Get partition
        IPartition partition = (IPartition) TestSupport.instance().getNamedElementByClassAndName(
            IPartition.class,
            "Partition0");

        // Get the class that the partition represents
        IClass represents = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        PartitionWithRepresentsDTO inputDTO = new PartitionWithRepresentsDTO(
            partition.getId(),
            represents.getId());

        // Check represents before setting
        assertNotEquals(represents, partition.getRepresents());

        // ----------------------------------------
        // Call setRepresents()
        // ----------------------------------------
        PartitionDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setRepresents,
            tool,
            inputDTO,
            PartitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(represents.getId(), outputDTO.represents().id());
        assertEquals("Foo", outputDTO.represents().name());

        // Check represents after setting
        assertEquals(represents, partition.getRepresents());
    }

    @Test
    void removeRepresents_ok() throws Exception {
        // Get partition
        IPartition partition = (IPartition) TestSupport.instance().getNamedElementByClassAndName(
            IPartition.class,
            "Partition0");

        // Set represents beforehand
        IClass represents = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        TestSupport.instance().invokeToolMethodReturningDto(
            setRepresents,
            tool,
            new PartitionWithRepresentsDTO(partition.getId(), represents.getId()),
            PartitionDTO.class);
        assertEquals(represents, partition.getRepresents());

        // Create input DTO
        IdDTO inputDTO = new IdDTO(partition.getId());

        // ----------------------------------------
        // Call removeRepresents()
        // ----------------------------------------
        PartitionDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeRepresents,
            tool,
            inputDTO,
            PartitionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("", outputDTO.represents().id());

        // Check represents after removing
        assertNull(partition.getRepresents());
    }
}
