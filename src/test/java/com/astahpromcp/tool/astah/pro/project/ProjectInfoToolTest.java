package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.AllLabelIdTypeInfoDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.AllNameIdTypeInfoDTO;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProjectInfoToolTest {

    private ProjectAccessor projectAccessor;
    private ProjectInfoTool tool;
    private Method getAllNamedElements;
    private Method getNamedElementsChunk;
    private Method getAllPresentations;
    private Method getPresentationsChunk;
    private Method retrieveDiagramContent;
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
            astahProToolSupport);

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
}
