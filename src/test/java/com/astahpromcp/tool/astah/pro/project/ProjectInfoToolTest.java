package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.SearchDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.*;
import com.astahpromcp.tool.astah.pro.project.outputdto.AllLabelIdTypeInfoDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.AllNameIdTypeInfoDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.NameIdTypeDefinitionListDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.NameIdTypeNamespaceListDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.SourceTargetNameIdTypeListDTO;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.visualization.outputdto.PlantumlDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectInfoToolTest {

    private ProjectAccessor projectAccessor;
    private ProjectInfoTool tool;
    private Method getAllNamedElements;
    private Method getNamedElementsChunk;
    private Method getAllPresentations;
    private Method getPresentationsChunk;
    private Method retrieveClassifiersThatReferenceOrBeReferencedBy;
    private Method searchWithinNamedElements;
    private Method searchWithinPresentations;
    private Method retrieveClassifiersWithinPackage;
    private Method retrievePackageStructureAsPlantuml;
    private Method retrieveClassifiersRelationshipsAsPlantuml;
    private Field nameIdTypeDTOChunksCacheField;
    private Field labelIdTypeDTOChunksCacheField;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.open("src/test/resources/modelfile/project/ProjectInfoToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ProjectInfoTool(
            projectAccessor,
            astahProToolSupport,
            true);

        // getAllNamedElements() method
        getAllNamedElements = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getAllNamedElements",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // getNamedElementsChunk() method
        getNamedElementsChunk = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getNamedElementsChunk",
            McpSyncServerExchange.class,
            ChunkDTO.class);

        // getAllPresentations() method
        getAllPresentations = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getAllPresentations",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // getPresentationsChunk() method
        getPresentationsChunk = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getPresentationsChunk",
            McpSyncServerExchange.class,
            ChunkDTO.class);
        
        // retrieveClassifiersThatReferenceOrBeReferencedBy() method
        retrieveClassifiersThatReferenceOrBeReferencedBy = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrieveClassifiersThatReferenceOrBeReferencedBy",
            McpSyncServerExchange.class,
            IdDTO.class);

        // searchWithinNamedElements() method
        searchWithinNamedElements = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "searchWithinNamedElements",
            McpSyncServerExchange.class,
            SearchDTO.class);

        // searchWithinPresentations() method
        searchWithinPresentations = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "searchWithinPresentations",
            McpSyncServerExchange.class,
            SearchDTO.class);

        // retrieveClassifiersWithinPackage() method
        retrieveClassifiersWithinPackage = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrieveClassifiersWithinPackage",
            McpSyncServerExchange.class,
            IdDTO.class);

        // retrievePackageStructureAsPlantuml() method
        retrievePackageStructureAsPlantuml = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrievePackageStructureAsPlantuml",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // retrieveClassifiersRelationshipsAsPlantuml() method
        retrieveClassifiersRelationshipsAsPlantuml = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrieveClassifiersRelationshipsAsPlantuml",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // nameIdTypeDTOChunksCache field
        nameIdTypeDTOChunksCacheField = TestSupport.getAccessibleField(
            ProjectInfoTool.class,
            "nameIdTypeDTOChunksCache");

        // labelIdTypeDTOChunksCache field
        labelIdTypeDTOChunksCacheField = TestSupport.getAccessibleField(
            ProjectInfoTool.class,
            "labelIdTypeDTOChunksCache");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getAllNamedElements_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getAllNamedElements()
        // ----------------------------------------
        AllNameIdTypeInfoDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getAllNamedElements,
            tool,
            inputDTO,
            AllNameIdTypeInfoDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getNamedElementsChunk_ok() throws Exception {
        // Set test data to chunk cache using reflection
        List<List<NameIdTypeDTO>> nameIdTypeDTOChunksCache = (List<List<NameIdTypeDTO>>) nameIdTypeDTOChunksCacheField.get(tool);
        nameIdTypeDTOChunksCache.clear();
        nameIdTypeDTOChunksCache.add(List.of(
            new NameIdTypeDTO("test1", "test1", "Class"),
            new NameIdTypeDTO("test2", "test2", "Interface"),
            new NameIdTypeDTO("test3", "test3", "Package")));

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getNamedElementsChunk()
        // ----------------------------------------
        NameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getNamedElementsChunk,
            tool,
            inputDTO,
            NameIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getAllPresentations_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getAllPresentations()
        // ----------------------------------------
        AllLabelIdTypeInfoDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getAllPresentations,
            tool,
            inputDTO,
            AllLabelIdTypeInfoDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getPresentationsChunk_ok() throws Exception {
        // Set test data to chunk cache using reflection
        List<List<LabelIdTypeDTO>> labelIdTypeDTOChunksCache = (List<List<LabelIdTypeDTO>>) labelIdTypeDTOChunksCacheField.get(tool);
        labelIdTypeDTOChunksCache.clear();
        labelIdTypeDTOChunksCache.add(List.of(
            new LabelIdTypeDTO("test1", "test1", "NodePresentation"),
            new LabelIdTypeDTO("test2", "test2", "LinkPresentation"),
            new LabelIdTypeDTO("test3", "test3", "NotePresentation")));

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getPresentationsChunk()
        // ----------------------------------------
        LabelIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getPresentationsChunk,
            tool,
            inputDTO,
            LabelIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void retrieveClassifiersThatReferenceOrBeReferencedBy_ok() throws Exception {
        // Get classifier
        IClass classifier = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Baz");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(classifier.getId());
        
        // ----------------------------------------
        // Call retrieveClassifiersThatReferenceOrBeReferencedBy()
        // ----------------------------------------
        SourceTargetNameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            retrieveClassifiersThatReferenceOrBeReferencedBy,
            tool,
            inputDTO,
            SourceTargetNameIdTypeListDTO.class);
        
        // Get classifier
        IClass quux01 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux01");
        IClass quux02 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux02");
        IClass quux03 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux03");
        IClass quux04 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux04");
        IClass quux05 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux05");
        IClass quux06 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux06");
        IClass quux07 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux07");
        IClass quux08 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux08");
        IClass quux09 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux09");
        IClass quux10 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux10");
        IClass quux11 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux11");
        IClass quux12 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux12");
        IClass quux13 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux13");
        IClass quux14 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux14");
        IClass quux15 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quux15");
        
        IClass quuy01 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy01");
        IClass quuy02 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy02");
        IClass quuy03 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy03");
        IClass quuy04 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy04");
        IClass quuy05 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy05");
        IClass quuy06 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy06");
        IClass quuy07 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy07");
        IClass quuy08 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy08");
        IClass quuy09 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy09");
        IClass quuy10 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy10");
        IClass quuy11 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy11");
        IClass quuy12 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy12");
        IClass quuy13 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy13");
        IClass quuy14 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy14");
        IClass quuy15 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy15");
        IClass quuy16 = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Quuy16");
        
        // Check output DTO
        assertNotNull(outputDTO);

        NameIdTypeDTO quux01DTO = NameIdTypeDTOAssembler.toDTO(quux01);
        NameIdTypeDTO quux02DTO = NameIdTypeDTOAssembler.toDTO(quux02);
        NameIdTypeDTO quux03DTO = NameIdTypeDTOAssembler.toDTO(quux03);
        NameIdTypeDTO quux04DTO = NameIdTypeDTOAssembler.toDTO(quux04);
        NameIdTypeDTO quux05DTO = NameIdTypeDTOAssembler.toDTO(quux05);
        NameIdTypeDTO quux06DTO = NameIdTypeDTOAssembler.toDTO(quux06);
        NameIdTypeDTO quux07DTO = NameIdTypeDTOAssembler.toDTO(quux07);
        NameIdTypeDTO quux08DTO = NameIdTypeDTOAssembler.toDTO(quux08);
        NameIdTypeDTO quux09DTO = NameIdTypeDTOAssembler.toDTO(quux09);
        NameIdTypeDTO quux10DTO = NameIdTypeDTOAssembler.toDTO(quux10);
        NameIdTypeDTO quux11DTO = NameIdTypeDTOAssembler.toDTO(quux11);
        NameIdTypeDTO quux12DTO = NameIdTypeDTOAssembler.toDTO(quux12);
        NameIdTypeDTO quux13DTO = NameIdTypeDTOAssembler.toDTO(quux13);
        NameIdTypeDTO quux14DTO = NameIdTypeDTOAssembler.toDTO(quux14);
        NameIdTypeDTO quux15DTO = NameIdTypeDTOAssembler.toDTO(quux15);

        List<NameIdTypeDTO> associationSourceClassifier = outputDTO.associationSourceClassifier();
        assertTrue(associationSourceClassifier.contains(quux01DTO));
        assertTrue(associationSourceClassifier.contains(quux02DTO));
        assertTrue(associationSourceClassifier.contains(quux03DTO));
        assertTrue(associationSourceClassifier.contains(quux04DTO));
        assertTrue(associationSourceClassifier.contains(quux05DTO));
        assertTrue(associationSourceClassifier.contains(quux06DTO));
        assertTrue(associationSourceClassifier.contains(quux07DTO));
        assertTrue(associationSourceClassifier.contains(quux08DTO));

        List<NameIdTypeDTO> inheritanceSourceClassifier = outputDTO.inheritanceSourceClassifier();
        assertTrue(inheritanceSourceClassifier.contains(quux09DTO));

        List<NameIdTypeDTO> dependencySourceClassifier = outputDTO.dependencySourceClassifier();
        assertTrue(dependencySourceClassifier.contains(quux10DTO));
        assertTrue(dependencySourceClassifier.contains(quux11DTO));

        List<NameIdTypeDTO> typeUsageSourceClassifier = outputDTO.typeUsageSourceClassifier();
        assertTrue(typeUsageSourceClassifier.contains(quux12DTO));
        assertTrue(typeUsageSourceClassifier.contains(quux13DTO));
        assertTrue(typeUsageSourceClassifier.contains(quux14DTO));
        assertTrue(typeUsageSourceClassifier.contains(quux15DTO));

        NameIdTypeDTO quuy01DTO = NameIdTypeDTOAssembler.toDTO(quuy01);
        NameIdTypeDTO quuy02DTO = NameIdTypeDTOAssembler.toDTO(quuy02);
        NameIdTypeDTO quuy03DTO = NameIdTypeDTOAssembler.toDTO(quuy03);
        NameIdTypeDTO quuy04DTO = NameIdTypeDTOAssembler.toDTO(quuy04);
        NameIdTypeDTO quuy05DTO = NameIdTypeDTOAssembler.toDTO(quuy05);
        NameIdTypeDTO quuy06DTO = NameIdTypeDTOAssembler.toDTO(quuy06);
        NameIdTypeDTO quuy07DTO = NameIdTypeDTOAssembler.toDTO(quuy07);
        NameIdTypeDTO quuy08DTO = NameIdTypeDTOAssembler.toDTO(quuy08);
        NameIdTypeDTO quuy09DTO = NameIdTypeDTOAssembler.toDTO(quuy09);
        NameIdTypeDTO quuy10DTO = NameIdTypeDTOAssembler.toDTO(quuy10);
        NameIdTypeDTO quuy11DTO = NameIdTypeDTOAssembler.toDTO(quuy11);
        NameIdTypeDTO quuy12DTO = NameIdTypeDTOAssembler.toDTO(quuy12);
        NameIdTypeDTO quuy13DTO = NameIdTypeDTOAssembler.toDTO(quuy13);
        NameIdTypeDTO quuy14DTO = NameIdTypeDTOAssembler.toDTO(quuy14);
        NameIdTypeDTO quuy15DTO = NameIdTypeDTOAssembler.toDTO(quuy15);
        NameIdTypeDTO quuy16DTO = NameIdTypeDTOAssembler.toDTO(quuy16);
        
        List<NameIdTypeDTO> associationTargetClassifier = outputDTO.associationTargetClassifier();
        assertTrue(associationTargetClassifier.contains(quuy01DTO));
        assertTrue(associationTargetClassifier.contains(quuy02DTO));
        assertTrue(associationTargetClassifier.contains(quuy03DTO));
        assertTrue(associationTargetClassifier.contains(quuy04DTO));
        assertTrue(associationTargetClassifier.contains(quuy05DTO));
        assertTrue(associationTargetClassifier.contains(quuy06DTO));
        assertTrue(associationTargetClassifier.contains(quuy07DTO));
        assertTrue(associationTargetClassifier.contains(quuy08DTO));

        List<NameIdTypeDTO> inheritanceTargetClassifier = outputDTO.inheritanceTargetClassifier();
        assertTrue(inheritanceTargetClassifier.contains(quuy09DTO));

        List<NameIdTypeDTO> dependencyTargetClassifier = outputDTO.dependencyTargetClassifier();
        assertTrue(dependencyTargetClassifier.contains(quuy10DTO));
        assertTrue(dependencyTargetClassifier.contains(quuy11DTO));

        List<NameIdTypeDTO> typeUsageTargetClassifier = outputDTO.typeUsageTargetClassifier();
        assertTrue(typeUsageTargetClassifier.contains(quuy12DTO));
        assertTrue(typeUsageTargetClassifier.contains(quuy13DTO));
        assertTrue(typeUsageTargetClassifier.contains(quuy14DTO));
        assertTrue(typeUsageTargetClassifier.contains(quuy15DTO));

        List<NameIdTypeDTO> realizationTargetClassifier = outputDTO.realizationTargetClassifier();
        assertTrue(realizationTargetClassifier.contains(quuy16DTO));
    }

    @Test
    void searchWithinNamedElements_ok_1() throws Exception {
        // Create input DTO
        SearchDTO inputDTO = new SearchDTO("test");

        // ----------------------------------------
        // Call searchWithinNamedElements()
        // ----------------------------------------
        NameIdTypeDefinitionListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            searchWithinNamedElements,
            tool,
            inputDTO,
            NameIdTypeDefinitionListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertEquals(6, outputDTO.value().size());
    }

    @Test
    void searchWithinNamedElements_ok_2() throws Exception {
        // Create input DTO
        SearchDTO inputDTO = new SearchDTO("テスト");

        // ----------------------------------------
        // Call searchWithinNamedElements()
        // ----------------------------------------
        NameIdTypeDefinitionListDTO outputDTO = TestSupport.instance().invokeToolMethod(
                searchWithinNamedElements,
                tool,
                inputDTO,
                NameIdTypeDefinitionListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertEquals(5, outputDTO.value().size());
    }

    @Test
    void searchWithinNamedElements_ng() throws Exception {
        // Create input DTO with empty string
        SearchDTO inputDTO = new SearchDTO("");

        // ----------------------------------------
        // Call searchWithinNamedElements() and expect exception
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethod(
                searchWithinNamedElements,
                tool,
                inputDTO,
                NameIdTypeDefinitionListDTO.class);
        });
    }

    @Test
    void searchWithinPresentations_ok_1() throws Exception {
        // Create input DTO
        SearchDTO inputDTO = new SearchDTO("note");

        // ----------------------------------------
        // Call searchWithinPresentations()
        // ----------------------------------------
        LabelIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            searchWithinPresentations,
            tool,
            inputDTO,
            LabelIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertEquals(3, outputDTO.value().size());
    }

    @Test
    void searchWithinPresentations_ok_2() throws Exception {
        // Create input DTO
        SearchDTO inputDTO = new SearchDTO("ノート");

        // ----------------------------------------
        // Call searchWithinPresentations()
        // ----------------------------------------
        LabelIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethod(
                searchWithinPresentations,
                tool,
                inputDTO,
                LabelIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertEquals(2, outputDTO.value().size());
    }

    @Test
    void searchWithinPresentations_ng() throws Exception {
        // Create input DTO with empty string
        SearchDTO inputDTO = new SearchDTO("");

        // ----------------------------------------
        // Call searchWithinPresentations() and expect exception
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethod(
                searchWithinPresentations,
                tool,
                inputDTO,
                LabelIdTypeListDTO.class);
        });
    }

    @Test
    void retrieveClassifiersWithinPackage_ok() throws Exception {
        // Get package
        IPackage package_ = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "package0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(package_.getId());
        
        // ----------------------------------------
        // Call retrieveClassifiersWithinPackage()
        // ----------------------------------------
        NameIdTypeNamespaceListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            retrieveClassifiersWithinPackage,
            tool,
            inputDTO,
            NameIdTypeNamespaceListDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertEquals(6, outputDTO.value().size());
    }

    @Test
    void retrievePackageStructureAsPlantuml_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call retrievePackageStructureAsPlantuml()
        // ----------------------------------------
        PlantumlDTO outputDTO = TestSupport.instance().invokeToolMethod(
            retrievePackageStructureAsPlantuml,
            tool,
            inputDTO,
            PlantumlDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.plantumlCode());
    }

    @Test
    void retrieveClassifiersRelationshipsAsPlantuml_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call retrieveClassifiersRelationshipsAsPlantuml()
        // ----------------------------------------
        PlantumlDTO outputDTO = TestSupport.instance().invokeToolMethod(
            retrieveClassifiersRelationshipsAsPlantuml,
            tool,
            inputDTO,
            PlantumlDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.plantumlCode());
        assertTrue(outputDTO.plantumlCode().contains("\"Quuy01\" --- \"Baz\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" --> \"Quuy02\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" --> \"Quuy03\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" --> \"Quuy04\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Quuy05\" --- \"Baz\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" --> \"Quuy06\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Quuy07\" --- \"Baz\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" --> \"Quuy08\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" --|> \"Quuy09\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" ..> \"Quuy10\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" ..> \"Quuy11\""));
        assertTrue(outputDTO.plantumlCode().contains("\"Baz\" ..|> \"Quuy16\""));
    }
}
