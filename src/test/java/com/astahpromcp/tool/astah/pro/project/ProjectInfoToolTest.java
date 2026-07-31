package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.*;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.project.inputdto.SearchDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.AllDefinitionNameIdTypeDTO;
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
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectInfoToolTest {

    private ProjectAccessor projectAccessor;
    private ProjectInfoTool tool;
    private Method getAllNamedElements;
    private Method getNamedElementsChunk;
    private Method getAllPresentations;
    private Method getPresentationsChunk;
    private Method getAllDefinitions;
    private Method getDefinitionsChunk;
    private Method getAllConstraintsAndConditions;
    private Method retrieveClassifiersThatReferenceOrBeReferencedBy;
    private Method searchWithinNamedElements;
    private Method searchWithinPresentations;
    private Method retrieveClassifiersWithinPackage;
    private Method retrievePackageStructureAsPlantuml;
    private Method retrieveClassifiersRelationshipsAsPlantuml;
    private Method getRelationshipsAsPlantumlCode;
    private Field nameIdTypeDTOChunksCacheUpdatedAtNanosField;
    private Field labelIdTypeDTOChunksCacheUpdatedAtNanosField;
    private Field definitionNameIdTypeDTOChunksCacheUpdatedAtNanosField;

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
            NoInputDTO.class);

        // getNamedElementsChunk() method
        getNamedElementsChunk = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getNamedElementsChunk",
            ChunkDTO.class);

        // getAllPresentations() method
        getAllPresentations = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getAllPresentations",
            NoInputDTO.class);

        // getPresentationsChunk() method
        getPresentationsChunk = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getPresentationsChunk",
            ChunkDTO.class);

        // getAllDefinitions() method
        getAllDefinitions = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getAllDefinitions",
            NoInputDTO.class);

        // getDefinitionsChunk() method
        getDefinitionsChunk = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getDefinitionsChunk",
            ChunkDTO.class);
        
        // getAllConstraintsAndConditions() method
        getAllConstraintsAndConditions = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getAllConstraintsAndConditions",
            NoInputDTO.class);

        // retrieveClassifiersThatReferenceOrBeReferencedBy() method
        retrieveClassifiersThatReferenceOrBeReferencedBy = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrieveClassifiersThatReferenceOrBeReferencedBy",
            IdDTO.class);

        // searchWithinNamedElements() method
        searchWithinNamedElements = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "searchWithinNamedElements",
            SearchDTO.class);

        // searchWithinPresentations() method
        searchWithinPresentations = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "searchWithinPresentations",
            SearchDTO.class);

        // retrieveClassifiersWithinPackage() method
        retrieveClassifiersWithinPackage = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrieveClassifiersWithinPackage",
            IdDTO.class);

        // retrievePackageStructureAsPlantuml() method
        retrievePackageStructureAsPlantuml = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrievePackageStructureAsPlantuml",
            NoInputDTO.class);

        // retrieveClassifiersRelationshipsAsPlantuml() method
        retrieveClassifiersRelationshipsAsPlantuml = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "retrieveClassifiersRelationshipsAsPlantuml",
            NoInputDTO.class);

        // getRelationshipsAsPlantumlCode() method
        getRelationshipsAsPlantumlCode = TestSupport.getAccessibleMethod(
            ProjectInfoTool.class,
            "getRelationshipsAsPlantumlCode",
            NoInputDTO.class);

        // nameIdTypeDTOChunksCacheUpdatedAtNanos field
        nameIdTypeDTOChunksCacheUpdatedAtNanosField = TestSupport.getAccessibleField(
            ProjectInfoTool.class,
            "nameIdTypeDTOChunksCacheUpdatedAtNanos");

        // labelIdTypeDTOChunksCacheUpdatedAtNanos field
        labelIdTypeDTOChunksCacheUpdatedAtNanosField = TestSupport.getAccessibleField(
            ProjectInfoTool.class,
            "labelIdTypeDTOChunksCacheUpdatedAtNanos");

        // definitionNameIdTypeDTOChunksCacheUpdatedAtNanos field
        definitionNameIdTypeDTOChunksCacheUpdatedAtNanosField = TestSupport.getAccessibleField(
            ProjectInfoTool.class,
            "definitionNameIdTypeDTOChunksCacheUpdatedAtNanos");
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
        AllNameIdTypeInfoDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getAllNamedElements,
            tool,
            inputDTO,
            AllNameIdTypeInfoDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getNamedElementsChunk_ok() throws Exception {
        // Initialize chunk cache
        TestSupport.instance().invokeToolMethodReturningDto(
            getAllNamedElements,
            tool,
            new NoInputDTO(),
            AllNameIdTypeInfoDTO.class);

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getNamedElementsChunk()
        // ----------------------------------------
        NameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getNamedElementsChunk,
            tool,
            inputDTO,
            NameIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getNamedElementsChunk_ng_cacheExpired() throws Exception {
        // Initialize chunk cache
        TestSupport.instance().invokeToolMethodReturningDto(
            getAllNamedElements,
            tool,
            new NoInputDTO(),
            AllNameIdTypeInfoDTO.class);
        nameIdTypeDTOChunksCacheUpdatedAtNanosField.setLong(tool, System.nanoTime() - TimeUnit.SECONDS.toNanos(181));

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getNamedElementsChunk()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                getNamedElementsChunk,
                tool,
                inputDTO,
                NameIdTypeListDTO.class);
        });
        
        // Check output DTO
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("get_info_of_all_named_elements again"));
        assertTrue(exception.getCause().getMessage().contains("get_chunk_of_all_named_elements"));
    }

    @Test
    void getAllPresentations_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getAllPresentations()
        // ----------------------------------------
        AllLabelIdTypeInfoDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getAllPresentations,
            tool,
            inputDTO,
            AllLabelIdTypeInfoDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getPresentationsChunk_ok() throws Exception {
        // Initialize chunk cache
        TestSupport.instance().invokeToolMethodReturningDto(
            getAllPresentations,
            tool,
            new NoInputDTO(),
            AllLabelIdTypeInfoDTO.class);

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getPresentationsChunk()
        // ----------------------------------------
        LabelIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getPresentationsChunk,
            tool,
            inputDTO,
            LabelIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getPresentationsChunk_ng_cacheExpired() throws Exception {
        // Initialize chunk cache
        TestSupport.instance().invokeToolMethodReturningDto(
            getAllPresentations,
            tool,
            new NoInputDTO(),
            AllLabelIdTypeInfoDTO.class);
        labelIdTypeDTOChunksCacheUpdatedAtNanosField.setLong(tool, System.nanoTime() - TimeUnit.SECONDS.toNanos(181));

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getPresentationsChunk()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                getPresentationsChunk,
                tool,
                inputDTO,
                LabelIdTypeListDTO.class);
        });
        
        // Check output DTO
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("get_info_of_all_prsts again"));
        assertTrue(exception.getCause().getMessage().contains("get_chunk_of_all_prsts"));
    }

    @Test
    void getAllDefinitions_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getAllDefinitions()
        // ----------------------------------------
        AllDefinitionNameIdTypeDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getAllDefinitions,
            tool,
            inputDTO,
            AllDefinitionNameIdTypeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getDefinitionsChunk_ok() throws Exception {
        // Initialize chunk cache
        TestSupport.instance().invokeToolMethodReturningDto(
            getAllDefinitions,
            tool,
            new NoInputDTO(),
            AllDefinitionNameIdTypeDTO.class);

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getDefinitionsChunk()
        // ----------------------------------------
        DefinitionNameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getDefinitionsChunk,
            tool,
            inputDTO,
            DefinitionNameIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getDefinitionsChunk_ng_cacheExpired() throws Exception {
        // Initialize chunk cache
        TestSupport.instance().invokeToolMethodReturningDto(
            getAllDefinitions,
            tool,
            new NoInputDTO(),
            AllDefinitionNameIdTypeDTO.class);
        definitionNameIdTypeDTOChunksCacheUpdatedAtNanosField.setLong(tool, System.nanoTime() - TimeUnit.SECONDS.toNanos(181));

        // Create input DTO
        ChunkDTO inputDTO = new ChunkDTO(0);

        // ----------------------------------------
        // Call getDefinitionsChunk()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                getDefinitionsChunk,
                tool,
                inputDTO,
                DefinitionNameIdTypeListDTO.class);
        });
        
        // Check output DTO
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("get_info_of_all_definitions again"));
        assertTrue(exception.getCause().getMessage().contains("get_chunk_of_all_definitions"));
    }

    @Test
    void getAllConstraintsAndConditions_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getAllConstraintsAndConditions()
        // ----------------------------------------
        ReportDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getAllConstraintsAndConditions,
            tool,
            inputDTO,
            ReportDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.contents());
    }

    @Test
    void retrieveClassifiersThatReferenceOrBeReferencedBy_ok() throws Exception {
        // Get classifier
        IClass classifier = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Baz");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(classifier.getId());
        
        // ----------------------------------------
        // Call retrieveClassifiersThatReferenceOrBeReferencedBy()
        // ----------------------------------------
        SourceTargetNameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            retrieveClassifiersThatReferenceOrBeReferencedBy,
            tool,
            inputDTO,
            SourceTargetNameIdTypeListDTO.class);
        
        // Get classifier
        IClass quux01 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux01");
        IClass quux02 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux02");
        IClass quux03 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux03");
        IClass quux04 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux04");
        IClass quux05 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux05");
        IClass quux06 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux06");
        IClass quux07 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux07");
        IClass quux08 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux08");
        IClass quux09 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux09");
        IClass quux10 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux10");
        IClass quux11 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux11");
        IClass quux12 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux12");
        IClass quux13 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux13");
        IClass quux14 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux14");
        IClass quux15 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quux15");
        
        IClass quuy01 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy01");
        IClass quuy02 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy02");
        IClass quuy03 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy03");
        IClass quuy04 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy04");
        IClass quuy05 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy05");
        IClass quuy06 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy06");
        IClass quuy07 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy07");
        IClass quuy08 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy08");
        IClass quuy09 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy09");
        IClass quuy10 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy10");
        IClass quuy11 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy11");
        IClass quuy12 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy12");
        IClass quuy13 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy13");
        IClass quuy14 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy14");
        IClass quuy15 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Quuy15");
        IClass quuy16 = (IClass) TestSupport.instance().getNamedElementByClassAndName(
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
        NameIdTypeDefinitionListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        NameIdTypeDefinitionListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
    void searchWithinNamedElements_ok_emptyStringTargetsAll() throws Exception {
        // Create input DTO with empty string
        SearchDTO inputDTO = new SearchDTO("");

        // ----------------------------------------
        // Call searchWithinNamedElements()
        // ----------------------------------------
        NameIdTypeDefinitionListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            searchWithinNamedElements,
            tool,
            inputDTO,
            NameIdTypeDefinitionListDTO.class);

        // Check output DTO: an empty search string targets all named elements
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertEquals(projectAccessor.findElements(INamedElement.class).length, outputDTO.value().size());
    }

    @Test
    void searchWithinPresentations_ok_1() throws Exception {
        // Create input DTO
        SearchDTO inputDTO = new SearchDTO("note");

        // ----------------------------------------
        // Call searchWithinPresentations()
        // ----------------------------------------
        LabelIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        LabelIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
    void searchWithinPresentations_ok_emptyStringTargetsAll() throws Exception {
        // Create input DTO with empty string
        SearchDTO inputDTO = new SearchDTO("");

        // ----------------------------------------
        // Call searchWithinPresentations()
        // ----------------------------------------
        LabelIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            searchWithinPresentations,
            tool,
            inputDTO,
            LabelIdTypeListDTO.class);

        // Check output DTO: an empty search string targets all presentations
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertFalse(outputDTO.value().isEmpty());
    }

    @Test
    void retrieveClassifiersWithinPackage_ok() throws Exception {
        // Get package
        IPackage package_ = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "package0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(package_.getId());
        
        // ----------------------------------------
        // Call retrieveClassifiersWithinPackage()
        // ----------------------------------------
        NameIdTypeNamespaceListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        PlantumlDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        PlantumlDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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

    @Test
    void getRelationshipsAsPlantumlCode_ok() throws Exception {
        // Get classifiers used as endpoints of Baz's relationships
        IClass baz = (IClass) TestSupport.instance().getNamedElementByClassAndName(IClass.class, "Baz");
        IClass quuy09 = (IClass) TestSupport.instance().getNamedElementByClassAndName(IClass.class, "Quuy09");
        IClass quuy10 = (IClass) TestSupport.instance().getNamedElementByClassAndName(IClass.class, "Quuy10");
        IClass quuy11 = (IClass) TestSupport.instance().getNamedElementByClassAndName(IClass.class, "Quuy11");
        IClass quuy16 = (IClass) TestSupport.instance().getNamedElementByClassAndName(IClass.class, "Quuy16");

        // Node names are fully-qualified names
        String bazFqn = baz.getFullName(".");
        String quuy09Fqn = quuy09.getFullName(".");
        String quuy10Fqn = quuy10.getFullName(".");
        String quuy11Fqn = quuy11.getFullName(".");
        String quuy16Fqn = quuy16.getFullName(".");

        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getRelationshipsAsPlantumlCode()
        // ----------------------------------------
        PlantumlDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getRelationshipsAsPlantumlCode,
            tool,
            inputDTO,
            PlantumlDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.plantumlCode());

        String plantumlCode = outputDTO.plantumlCode();
        assertTrue(plantumlCode.startsWith("@startuml"));
        assertTrue(plantumlCode.contains("@enduml"));

        // Generalization, realization, dependency and usage are output using fully-qualified names
        assertTrue(plantumlCode.contains("\"" + bazFqn + "\" --|> \"" + quuy09Fqn + "\""));
        assertTrue(plantumlCode.contains("\"" + bazFqn + "\" ..|> \"" + quuy16Fqn + "\""));
        assertTrue(plantumlCode.contains("\"" + bazFqn + "\" ..> \"" + quuy10Fqn + "\""));
        assertTrue(plantumlCode.contains("\"" + bazFqn + "\" ..> \"" + quuy11Fqn + "\""));
    }
}
