package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithTypeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EREntityDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ERModelEditor;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.model.IERPackage;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.model.IERSubtypeRelationship;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EREntityToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private EREntityTool tool;
    private Method getInfo;
    private Method setLogicalName;
    private Method setPhysicalName;
    private Method setType;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/EREntityToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new EREntityTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "getInfo",
            IdDTO.class);

        // setLogicalName() method
        setLogicalName = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "setLogicalName",
            EREntityWithLogicalNameDTO.class);

        // setPhysicalName() method
        setPhysicalName = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "setPhysicalName",
            EREntityWithPhysicalNameDTO.class);

        // setType() method
        setType = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "setType",
            EREntityWithTypeDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElementByClassAndName(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erEntity.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erEntity.getId(), outputDTO.namedElement().element().id());
        assertEquals(erEntity.getName(), outputDTO.namedElement().name());
    }

    @Test
    void getInfo_ok_parentRelationshipsHoldThoseWhoseChildIsThisEntity() throws Exception {
        ERModelEditor erModelEditor = projectAccessor.getModelEditorFactory().getERModelEditor();

        // A parent entity and a child entity in the package that holds Entity0
        IEREntity entity0 = (IEREntity) TestSupport.instance().getNamedElementByClassAndName(
            IEREntity.class,
            "Entity0");
        IERPackage erPackage = (IERPackage) entity0.getOwner();
        IEREntity parentEntity = transactionSupport.call(() -> erModelEditor.createEREntity(erPackage, "ParentEntity", "parent_entity"));
        IEREntity childEntity = transactionSupport.call(() -> erModelEditor.createEREntity(erPackage, "ChildEntity", "child_entity"));
        IERRelationship relationship = transactionSupport.call(() ->
            erModelEditor.createNonIdentifyingRelationship(parentEntity, childEntity, "Relationship", "relationship"));
        IERSubtypeRelationship subtypeRelationship = transactionSupport.call(() ->
            erModelEditor.createSubtypeRelationship(parentEntity, childEntity, "SubtypeRelationship", "subtype_relationship"));

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        EREntityDTO parentDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(parentEntity.getId()),
            EREntityDTO.class);
        EREntityDTO childDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(childEntity.getId()),
            EREntityDTO.class);

        // Check output DTO: "parent" and "child" name the entity at the other end
        assertEquals(List.of(relationship.getId()), idsOf(childDTO.parentERRelationships()));
        assertEquals(List.of(subtypeRelationship.getId()), idsOf(childDTO.parentERSubtypeRelationships()));
        assertTrue(childDTO.childERRelationships().isEmpty());
        assertTrue(childDTO.childERSubtypeRelationships().isEmpty());

        assertEquals(List.of(relationship.getId()), idsOf(parentDTO.childERRelationships()));
        assertEquals(List.of(subtypeRelationship.getId()), idsOf(parentDTO.childERSubtypeRelationships()));
        assertTrue(parentDTO.parentERRelationships().isEmpty());
        assertTrue(parentDTO.parentERSubtypeRelationships().isEmpty());
    }

    private static List<String> idsOf(List<NameIdTypeDTO> elements) {
        return elements.stream().map(NameIdTypeDTO::id).toList();
    }

    @Test
    void setLogicalName_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElementByClassAndName(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        EREntityWithLogicalNameDTO inputDTO = new EREntityWithLogicalNameDTO(
            erEntity.getId(),
            "LogicalName_777");

        // ----------------------------------------
        // Call setLogicalName()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setLogicalName,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("LogicalName_777", erEntity.getLogicalName());
    }

    @Test
    void setPhysicalName_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElementByClassAndName(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        EREntityWithPhysicalNameDTO inputDTO = new EREntityWithPhysicalNameDTO(
            erEntity.getId(),
            "physical_name_777");

        // ----------------------------------------
        // Call setPhysicalName()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setPhysicalName,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("physical_name_777", erEntity.getPhysicalName());
    }

    @Test
    void setType_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElementByClassAndName(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        EREntityWithTypeDTO inputDTO = new EREntityWithTypeDTO(
            erEntity.getId(),
            "Event");

        // ----------------------------------------
        // Call setType()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setType,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Event", erEntity.getType());
    }
}
