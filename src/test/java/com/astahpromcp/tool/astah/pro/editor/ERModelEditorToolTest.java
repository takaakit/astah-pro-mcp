package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ERModelEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ERModelEditorToolTest {

    private ProjectAccessor projectAccessor;
    private ERModelEditorTool tool;
    private Method createERModel;
    private Method createERPackage;
    private Method createEREntity;
    private Method createERAttribute;
    private Method createERDatatype;
    private Method createERDomainInERModel;
    private Method createERDomainInERDomain;
    private Method createIdentifyingRelationship;
    private Method createNonIdentifyingRelationship;
    private Method createManyToManyRelationship;
    private Method createSubtypeRelationship;
    private Method createERIndex;
    private Method delete;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        ERModelEditor erModelEditor = projectAccessor.getModelEditorFactory().getERModelEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/ERModelEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERModelEditorTool(
            erModelEditor,
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // createERModel() method
        createERModel = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createERModel",
            McpSyncServerExchange.class,
            NewERModelDTO.class);

        // createERPackage() method
        createERPackage = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createERPackage",
            McpSyncServerExchange.class,
            NewERPackageInERPackageDTO.class);

        // createEREntity() method
        createEREntity = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createEREntity",
            McpSyncServerExchange.class,
            NewEREntityInERPackageDTO.class);

        // createERAttribute() method
        createERAttribute = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createERAttribute",
            McpSyncServerExchange.class,
            NewERAttributeInEREntityDTO.class);

        // createERDatatype() method
        createERDatatype = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createERDatatype",
            McpSyncServerExchange.class,
            NewERDatatypeInERModelDTO.class);

        // createERDomainInERModel() method
        createERDomainInERModel = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createERDomainInERModel",
            McpSyncServerExchange.class,
            NewERDomainInERModelDTO.class);

        // createERDomainInERDomain() method
        createERDomainInERDomain = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createERDomainInERDomain",
            McpSyncServerExchange.class,
            NewERDomainInERDomainDTO.class);

        // createIdentifyingRelationship() method
        createIdentifyingRelationship = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createIdentifyingRelationship",
            McpSyncServerExchange.class,
            NewIdentifyingRelationshipDTO.class);

        // createNonIdentifyingRelationship() method
        createNonIdentifyingRelationship = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createNonIdentifyingRelationship",
            McpSyncServerExchange.class,
            NewNonIdentifyingRelationshipDTO.class);

        // createManyToManyRelationship() method
        createManyToManyRelationship = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createManyToManyRelationship",
            McpSyncServerExchange.class,
            NewManyToManyRelationshipDTO.class);

        // createSubtypeRelationship() method
        createSubtypeRelationship = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createSubtypeRelationship",
            McpSyncServerExchange.class,
            NewSubtypeRelationshipDTO.class);

        // createERIndex() method
        createERIndex = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "createERIndex",
            McpSyncServerExchange.class,
            NewERIndexDTO.class);

        // delete() method
        delete = TestSupport.getAccessibleMethod(
            ERModelEditorTool.class,
            "delete",
            McpSyncServerExchange.class,
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createERModel_ok() throws Exception {
        // Delete existing ER model
        IERModel existingERModel = (IERModel) TestSupport.instance().getNamedElement(
            IERModel.class,
            "ER Model");

        IdDTO deleteDTO = new IdDTO(existingERModel.getId());
        TestSupport.instance().invokeToolMethod(
            delete,
            tool,
            deleteDTO,
            ElementDTO.class);

        // Create input DTO
        NewERModelDTO inputDTO = new NewERModelDTO("TestERModel");

        // ----------------------------------------
        // Call createERModel()
        // ----------------------------------------
        ERModelDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERModel,
            tool,
            inputDTO,
            ERModelDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestERModel", outputDTO.pkg().namedElement().name());
    }

    @Test
    void createERPackage_ok() throws Exception {
        // Get parent ER package
        IERPackage parentERPackage = (IERPackage) TestSupport.instance().getNamedElement(
            IERPackage.class,
            "package0");

        // Create input DTO
        NewERPackageInERPackageDTO inputDTO = new NewERPackageInERPackageDTO(
            parentERPackage.getId(),
            "TestERPackage");

        // ----------------------------------------
        // Call createERPackage()
        // ----------------------------------------
        ERPackageDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERPackage,
            tool,
            inputDTO,
            ERPackageDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestERPackage", outputDTO.pkg().namedElement().name());
    }

    @Test
    void createEREntity_ok() throws Exception {
        // Get parent ER package
        IERPackage parentERPackage = (IERPackage) TestSupport.instance().getNamedElement(
            IERPackage.class,
            "package0");

        // Create input DTO
        NewEREntityInERPackageDTO inputDTO = new NewEREntityInERPackageDTO(
            parentERPackage.getId(),
            "TestEntityLogical",
            "test_entity_physical");

        // ----------------------------------------
        // Call createEREntity()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createEREntity,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestEntityLogical", outputDTO.logicalName());
    }

    @Test
    void createERAttribute_ok() throws Exception {
        // Get target ER entity
        IEREntity targetEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        NewERAttributeInEREntityDTO inputDTO = new NewERAttributeInEREntityDTO(
            targetEREntity.getId(),
            "TestAttributeLogical",
            "test_attribute_physical",
            erDatatype.getId());

        // ----------------------------------------
        // Call createERAttribute()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERAttribute,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestAttributeLogical", outputDTO.logicalName());
    }

    @Test
    void createERDatatype_ok() throws Exception {
        // Get target ER model
        IERModel erModel = (IERModel) TestSupport.instance().getNamedElement(
            IERModel.class,
            "ER Model");

        // Create input DTO
        NewERDatatypeInERModelDTO inputDTO = new NewERDatatypeInERModelDTO(
            erModel.getId(),
            "TestDatatype");

        // ----------------------------------------
        // Call createERDatatype()
        // ----------------------------------------
        ERDatatypeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERDatatype,
            tool,
            inputDTO,
            ERDatatypeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createERDomainInERModel_ok() throws Exception {
        // Get target ER model
        IERModel erModel = (IERModel) TestSupport.instance().getNamedElement(
            IERModel.class,
            "ER Model");

        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        NewERDomainInERModelDTO inputDTO = new NewERDomainInERModelDTO(
            erModel.getId(),
            "TestDomainLogical",
            "test_domain_physical",
            erDatatype.getId());

        // ----------------------------------------
        // Call createERDomainInERModel()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERDomainInERModel,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestDomainLogical", outputDTO.logicalName());
        assertEquals("test_domain_physical", outputDTO.physicalName());
    }

    @Test
    void createERDomainInERDomain_ok() throws Exception {
        // Get parent ER domain
        IERDomain parentERDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        NewERDomainInERDomainDTO inputDTO = new NewERDomainInERDomainDTO(
            parentERDomain.getId(),
            "TestSubDomainLogical",
            "test_sub_domain_physical",
            erDatatype.getId());

        // ----------------------------------------
        // Call createERDomainInERDomain()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERDomainInERDomain,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestSubDomainLogical", outputDTO.logicalName());
    }

    @Test
    void createIdentifyingRelationship_ok() throws Exception {
        // Get ER entities
        IEREntity parentEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        IEREntity childEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity1");

        // Create input DTO
        NewIdentifyingRelationshipDTO inputDTO = new NewIdentifyingRelationshipDTO(
            parentEREntity.getId(),
            childEREntity.getId(),
            "TestIdentifyingRelationship",
            "test_identifying_relationship",
            null);

        // ----------------------------------------
        // Call createIdentifyingRelationship()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createIdentifyingRelationship,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestIdentifyingRelationship", outputDTO.logicalName());
        assertTrue(outputDTO.isIdentifying());
    }

    @Test
    void createNonIdentifyingRelationship_ok() throws Exception {
        // Get ER entities
        IEREntity parentEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        IEREntity childEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity1");

        // Create input DTO
        NewNonIdentifyingRelationshipDTO inputDTO = new NewNonIdentifyingRelationshipDTO(
            parentEREntity.getId(),
            childEREntity.getId(),
            "TestNonIdentifyingRelationship",
            "test_non_identifying_relationship",
            null);

        // ----------------------------------------
        // Call createNonIdentifyingRelationship()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createNonIdentifyingRelationship,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestNonIdentifyingRelationship", outputDTO.logicalName());
        assertTrue(outputDTO.isNonIdentifying());
    }

    @Test
    void createManyToManyRelationship_ok() throws Exception {
        // Get ER entities
        IEREntity parentEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        IEREntity childEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity1");

        // Create input DTO
        NewManyToManyRelationshipDTO inputDTO = new NewManyToManyRelationshipDTO(
            parentEREntity.getId(),
            childEREntity.getId(),
            "TestManyToManyRelationship",
            "test_many_to_many_relationship");

        // ----------------------------------------
        // Call createManyToManyRelationship()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createManyToManyRelationship,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestManyToManyRelationship", outputDTO.logicalName());
        assertTrue(outputDTO.isManyToMany());
    }

    @Test
    void createSubtypeRelationship_ok() throws Exception {
        // Get ER entities
        IEREntity parentEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        IEREntity childEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity1");

        // Create input DTO
        NewSubtypeRelationshipDTO inputDTO = new NewSubtypeRelationshipDTO(
            parentEREntity.getId(),
            childEREntity.getId(),
            "TestSubtypeRelationship",
            "test_subtype_relationship");

        // ----------------------------------------
        // Call createSubtypeRelationship()
        // ----------------------------------------
        ERSubtypeRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createSubtypeRelationship,
            tool,
            inputDTO,
            ERSubtypeRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestSubtypeRelationship", outputDTO.logicalName());
    }

    @Test
    void createERIndex_ok() throws Exception {
        // Get parent ER entity
        IEREntity parentEREntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute1");

        List<String> targetAttributeIds = new ArrayList<>();
        targetAttributeIds.add(erAttribute.getId());

        // Create input DTO
        NewERIndexDTO inputDTO = new NewERIndexDTO(
            "TestERIndex",
            parentEREntity.getId(),
            true,
            true,
            targetAttributeIds);

        // ----------------------------------------
        // Call createERIndex()
        // ----------------------------------------
        ERIndexDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createERIndex,
            tool,
            inputDTO,
            ERIndexDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("TestERIndex", outputDTO.namedElement().name());
        assertTrue(outputDTO.isKey());
        assertTrue(outputDTO.isUnique());
    }

    @Test
    void delete_ok() throws Exception {
        // Get parent ER package
        IERPackage parentERPackage = (IERPackage) TestSupport.instance().getNamedElement(
            IERPackage.class,
            "package0");

        // Get ER package
        IERPackage targetERPackage = (IERPackage) TestSupport.instance().getNamedElement(
            IERPackage.class,
            "package1");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(targetERPackage.getId());

        // ----------------------------------------
        // Call delete()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            delete,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNull(TestSupport.instance().getNamedElement(
            IERPackage.class,
            "package1"));
    }
}
