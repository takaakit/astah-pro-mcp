package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.*;
import com.astahpromcp.tool.astah.pro.project.outputdto.AllLabelIdTypeInfoDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.AllNameIdTypeInfoDTO;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProjectInfoTool implements ToolProvider {

    private static final int CHUNK_SIZE = 400;

    private final ProjectAccessor projectAccessor;
    private final AstahProToolSupport astahProToolSupport;
    private final List<List<NameIdTypeDTO>> nameIdTypeDTOChunksCache;
    private final List<List<LabelIdTypeDTO>> labelIdTypeDTOChunksCache;
    private final Object nameCacheLock = new Object();
    private final Object labelCacheLock = new Object();

    public ProjectInfoTool(ProjectAccessor projectAccessor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.astahProToolSupport = astahProToolSupport;
        this.nameIdTypeDTOChunksCache = new ArrayList<>();
        this.labelIdTypeDTOChunksCache = new ArrayList<>();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_info_of_all_named_elems",
                            "Return the total number of chunks and the data of the first chunk of all named elements in the project. The chunk data is a simplified information: name, identifier, and type.",
                            this::getAllNamedElements,
                            NoInputDTO.class,
                            AllNameIdTypeInfoDTO.class),

                    ToolSupport.definition(
                            "get_chunk_of_all_named_elems",
                            "Return the chunk data of all named elements in the project. The chunk data is a simplified information: name, identifier, and type.",
                            this::getNamedElementsChunk,
                            ChunkDTO.class,
                            NameIdTypeListDTO.class),

                    ToolSupport.definition(
                            "get_info_of_all_prsts",
                            "Return the total number of chunks and the data of the first chunk of all presentations in the project. The chunk data is a simplified information: label, identifier, and type.",
                            this::getAllPresentations,
                            NoInputDTO.class,
                            AllLabelIdTypeInfoDTO.class),
                            
                    ToolSupport.definition(
                            "get_chunk_of_all_prsts",
                            "Return the chunk data of all presentations in the project. The chunk data is a simplified information: label, identifier, and type.",
                            this::getPresentationsChunk,
                            ChunkDTO.class,
                            LabelIdTypeListDTO.class),

                    ToolSupport.definition(
                            "get_info_of_dgm_prsts",
                            "Return all presentation data on the specified diagram (specified by ID). The presentation data is a simplified information: label, identifier, and type.",
                            this::getAllPresentationsOnDiagram,
                            IdDTO.class,
                            LabelIdTypeListDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create project info tools", e);
            return List.of();
        }
    }


    private AllNameIdTypeInfoDTO getAllNamedElements(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get information of all named elements: {}", param);

        INamedElement[] astahNamedElements = projectAccessor.findElements(INamedElement.class);
        List<NameIdTypeDTO> namedIdTypeDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            try {
                namedIdTypeDTOs.add(NameIdTypeDTOAssembler.toDTO(astahNamedElement));
            } catch (Exception e) {
                log.info("Due to an issue on the Astah side, failed to convert INamedElement to NameIdTypeDTO: " + e.getMessage());
            }
        }

        List<List<NameIdTypeDTO>> newCache = new ArrayList<>();
        if (namedIdTypeDTOs.isEmpty()) {
            newCache.add(new ArrayList<>());
        } else {
            for (int i = 0; i < namedIdTypeDTOs.size(); i += CHUNK_SIZE) {
                newCache.add(new ArrayList<>(namedIdTypeDTOs.subList(i, Math.min(namedIdTypeDTOs.size(), i + CHUNK_SIZE))));
            }
        }

        List<NameIdTypeDTO> firstChunk;
        int totalChunks;
        synchronized (nameCacheLock) {
            nameIdTypeDTOChunksCache.clear();
            nameIdTypeDTOChunksCache.addAll(newCache);
            totalChunks = nameIdTypeDTOChunksCache.size();
            firstChunk = nameIdTypeDTOChunksCache.get(0);
        }

        return new AllNameIdTypeInfoDTO(totalChunks, firstChunk);
    }

    private NameIdTypeListDTO getNamedElementsChunk(McpSyncServerExchange exchange, ChunkDTO param) throws Exception {
        log.debug("Get named elements chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        List<NameIdTypeDTO> chunk;
        synchronized (nameCacheLock) {
            if (chunkIndex < 0 || chunkIndex >= nameIdTypeDTOChunksCache.size()) {
                throw new RuntimeException("Invalid chunk index: " + chunkIndex);
            }
            chunk = nameIdTypeDTOChunksCache.get(chunkIndex);
        }

        return new NameIdTypeListDTO(chunk);
    }

    private AllLabelIdTypeInfoDTO getAllPresentations(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get information of all presentations: {}", param);

        INamedElement[] astahNamedElements = projectAccessor.findElements(IDiagram.class);
        List<LabelIdTypeDTO> labelIdTypeDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            IDiagram astahDiagram = (IDiagram) astahNamedElement;
            for (IPresentation astahPresentation : astahDiagram.getPresentations()) {
                if (astahPresentation.getLabel() == null
                        || astahPresentation.getID() == null
                        || astahPresentation.getID().isEmpty()) {
                    log.info("Label or ID is invalid: Label={}, ID={}", astahPresentation.getLabel(), astahPresentation.getID());
                    continue;
                }
                labelIdTypeDTOs.add(LabelIdTypeDTOAssembler.toDTO(astahPresentation));
            }
        }

        List<List<LabelIdTypeDTO>> newCache = new ArrayList<>();
        if (labelIdTypeDTOs.isEmpty()) {
            newCache.add(new ArrayList<>());
        } else {
            for (int i = 0; i < labelIdTypeDTOs.size(); i += CHUNK_SIZE) {
                newCache.add(new ArrayList<>(labelIdTypeDTOs.subList(i, Math.min(labelIdTypeDTOs.size(), i + CHUNK_SIZE))));
            }
        }

        List<LabelIdTypeDTO> firstChunk;
        int totalChunks;
        synchronized (labelCacheLock) {
            labelIdTypeDTOChunksCache.clear();
            labelIdTypeDTOChunksCache.addAll(newCache);
            totalChunks = labelIdTypeDTOChunksCache.size();
            firstChunk = labelIdTypeDTOChunksCache.get(0);
        }

        return new AllLabelIdTypeInfoDTO(totalChunks, firstChunk);
    }

    private LabelIdTypeListDTO getPresentationsChunk(McpSyncServerExchange exchange, ChunkDTO param) throws Exception {
        log.debug("Get presentations chunk: {}", param);
        
        int chunkIndex = param.chunkIndex();
        List<LabelIdTypeDTO> chunk;
        synchronized (labelCacheLock) {
            if (chunkIndex < 0 || chunkIndex >= labelIdTypeDTOChunksCache.size()) {
                throw new RuntimeException("Invalid chunk index: " + chunkIndex);
            }
            chunk = labelIdTypeDTOChunksCache.get(chunkIndex);
        }

        return new LabelIdTypeListDTO(chunk);
    }

    private LabelIdTypeListDTO getAllPresentationsOnDiagram(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get information of all presentations on diagram: {}", param);

        IDiagram astahDiagram = astahProToolSupport.getDiagram(param.id());
        List<LabelIdTypeDTO> labelIdTypeDTOs = new ArrayList<>();
        for (IPresentation astahPresentation : astahDiagram.getPresentations()) {
            if (astahPresentation.getLabel() == null
                    || astahPresentation.getID() == null
                    || astahPresentation.getID().isEmpty()) {
                log.info("Label or ID is invalid: Label={}, ID={}", astahPresentation.getLabel(), astahPresentation.getID());
                continue;
            }
            labelIdTypeDTOs.add(LabelIdTypeDTOAssembler.toDTO(astahPresentation));
        }

        return new LabelIdTypeListDTO(labelIdTypeDTOs);
    }
}

