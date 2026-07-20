package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.*;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.project.inputdto.*;
import com.astahpromcp.tool.astah.pro.project.outputdto.*;
import com.astahpromcp.tool.astah.pro.project.outputdto.assembler.*;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.visualization.outputdto.PlantumlDTO;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.Strings;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProjectInfoTool implements ToolProvider {

    private static final int CHUNK_SIZE = 400;
    private static final long CHUNK_CACHE_TTL_NANOS = TimeUnit.SECONDS.toNanos(180);

    private final ProjectAccessor projectAccessor;
    private final AstahProToolSupport astahProToolSupport;
    private final List<List<NameIdTypeDTO>> nameIdTypeDTOChunksCache;
    private final List<List<LabelIdTypeDTO>> labelIdTypeDTOChunksCache;
    private final List<List<DefinitionNameIdTypeDTO>> definitionNameIdTypeDTOChunksCache;
    private long nameIdTypeDTOChunksCacheUpdatedAtNanos = -1L;
    private long labelIdTypeDTOChunksCacheUpdatedAtNanos = -1L;
    private long definitionNameIdTypeDTOChunksCacheUpdatedAtNanos = -1L;
    private final Object nameCacheLock = new Object();
    private final Object labelCacheLock = new Object();
    private final Object definitionCacheLock = new Object();
    private final boolean includeEditTools;

    public ProjectInfoTool(ProjectAccessor projectAccessor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.astahProToolSupport = astahProToolSupport;
        this.nameIdTypeDTOChunksCache = new ArrayList<>();
        this.labelIdTypeDTOChunksCache = new ArrayList<>();
        this.definitionNameIdTypeDTOChunksCache = new ArrayList<>();
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create project info tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_info_of_all_named_elements",
                "Return the total number of chunks and the data of the first chunk of all named elements in the project. The chunk data is a simplified information: name, identifier, and type.",
                this::getAllNamedElements,
                NoInputDTO.class,
                AllNameIdTypeInfoDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_chunk_of_all_named_elements",
                "Return the chunk data of all named elements in the project. The chunk data is a simplified information: name, identifier, and type.",
                this::getNamedElementsChunk,
                ChunkDTO.class,
                NameIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_info_of_all_prsts",
                "Return the total number of chunks and the data of the first chunk of all presentations in the project. The chunk data is a simplified information: label, identifier, and type.",
                this::getAllPresentations,
                NoInputDTO.class,
                AllLabelIdTypeInfoDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_chunk_of_all_prsts",
                "Return the chunk data of all presentations in the project. The chunk data is a simplified information: label, identifier, and type.",
                this::getPresentationsChunk,
                ChunkDTO.class,
                LabelIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_info_of_all_definitions",
                "Return the total number of chunks and the data of the first chunk of all definitions in the project. The chunk data is a simplified information: definition, name, identifier, and type.",
                this::getAllDefinitions,
                NoInputDTO.class,
                AllDefinitionNameIdTypeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_chunk_of_all_definitions",
                "Return the chunk data of all definitions in the project. The chunk data is a simplified information: definition, name, identifier, and type.",
                this::getDefinitionsChunk,
                ChunkDTO.class,
                DefinitionNameIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_info_of_dgm_prsts",
                "Return all presentation data on the specified diagram (specified by ID). The presentation data is a simplified information: label, identifier, and type.",
                this::getAllPresentationsOnDiagram,
                IdDTO.class,
                LabelIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_all_constraints_and_conditions",
                "Return all constraints and conditions (preconditions, postconditions, and body conditions) in the project. For example, use this tool to obtain the constraints when you want to verify whether constraints written in OCL are satisfied.",
                this::getAllConstraintsAndConditions,
                NoInputDTO.class,
                ReportDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "retrieve_classifiers_that_ref_or_be_refed_by",
                "Return all classifiers (classes, interfaces, and enumerations) that the specified classifier (specified by ID) references or that reference it. The returned data is a simplified information: name, identifier, and type. For example, when you need to understand the scope of impact of changes to a specific element, use this tool. If you want to traverse references recursively, you'll need to use this tool repeatedly. Note that using a classifier as a type is considered a reference to that classifier.",
                this::retrieveClassifiersThatReferenceOrBeReferencedBy,
                IdDTO.class,
                SourceTargetNameIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "search_within_named_elements",
                "Search for the specified string within the name and definition (the element's description field) of named elements using partial matching. The search is case-insensitive. Note that presentations are excluded from the search scope. Specify an empty string as the search string to target all named elements.",
                this::searchWithinNamedElements,
                SearchDTO.class,
                NameIdTypeDefinitionListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "search_within_prsts",
                "Search for the specified string within the label of presentations using partial matching. The search is case-insensitive. Note that named elements are excluded from the search scope. For example, if you want to search for the specified string within note contents, use this tool. Specify an empty string as the search string to target all presentations.",
                this::searchWithinPresentations,
                SearchDTO.class,
                LabelIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "retrieve_classifiers_within_pkg",
                "Return all classifiers (classes, interfaces, and enumerations) within the specified package (specified by ID), including those in its subpackages. The returned data is a simplified information: name, identifier, type, and namespace.",
                this::retrieveClassifiersWithinPackage,
                IdDTO.class,
                NameIdTypeNamespaceListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "retrieve_pkg_structure_as_puml",
                "Return the PlantUML code that represents the package structure of the classifiers within the project. When you need to know the package structure of classifiers across the entire project, use this tool.",
                this::retrievePackageStructureAsPlantuml,
                NoInputDTO.class,
                PlantumlDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "retrieve_classifier_relationships_as_puml",
                "Return the PlantUML code that represents the relationships between the classifiers within the project. When you need to know the relationships between classifiers across the entire project, use this tool. Note that the relationships returned by this tool include only the relationship types; for example, association role names, multiplicities, composition, aggregation, and association names are not included.",
                this::retrieveClassifiersRelationshipsAsPlantuml,
                NoInputDTO.class,
                PlantumlDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_relationships_as_puml_code",
                "Return the PlantUML code for a class diagram that shows only the relationships among NamedElements in the Astah project. However, do not include the role names, multiplicities, or composition types of the relationships. If you need that information, retrieve the detail information for each individual relationship. This tool is recommended when you want to understand the overall element relationships in the Astah project.",
                this::getRelationshipsAsPlantumlCode,
                NoInputDTO.class,
                PlantumlDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of();
    }

    private AllNameIdTypeInfoDTO getAllNamedElements(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get information of all named elements: {}", param);

        INamedElement[] astahNamedElements = projectAccessor.findElements(INamedElement.class);
        List<NameIdTypeDTO> namedIdTypeDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            try {
                namedIdTypeDTOs.add(NameIdTypeDTOAssembler.toDTO(astahNamedElement));
            } catch (Exception e) {
                log.debug("Due to an issue on the Astah side, failed to convert INamedElement to NameIdTypeDTO: " + e.getMessage());
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
            nameIdTypeDTOChunksCacheUpdatedAtNanos = System.nanoTime();
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
            if (nameIdTypeDTOChunksCacheUpdatedAtNanos < 0) {
                throw new RuntimeException("Chunk cache is not initialized. Call get_info_of_all_named_elements first, then call get_chunk_of_all_named_elements.");
            }
            if (System.nanoTime() - nameIdTypeDTOChunksCacheUpdatedAtNanos >= CHUNK_CACHE_TTL_NANOS) {
                throw new RuntimeException("The cached chunk data was generated 180 or more seconds ago. The Astah model may have been updated. Call get_info_of_all_named_elements again, then call get_chunk_of_all_named_elements.");
            }
            if (chunkIndex < 0 || chunkIndex >= nameIdTypeDTOChunksCache.size()) {
                throw new RuntimeException("Invalid chunk index: " + chunkIndex);
            }
            chunk = nameIdTypeDTOChunksCache.get(chunkIndex);
        }

        return new NameIdTypeListDTO(chunk);
    }

    private AllDefinitionNameIdTypeDTO getAllDefinitions(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get information of all definitions: {}", param);

        INamedElement[] astahNamedElements = projectAccessor.findElements(INamedElement.class);
        List<DefinitionNameIdTypeDTO> definitionNameIdTypeDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            try {
                definitionNameIdTypeDTOs.add(DefinitionNameIdTypeDTOAssembler.toDTO(astahNamedElement));
            } catch (Exception e) {
                log.debug("Due to an issue on the Astah side, failed to convert INamedElement to DefinitionNameIdTypeDTO: " + e.getMessage());
            }
        }

        List<List<DefinitionNameIdTypeDTO>> newCache = new ArrayList<>();
        if (definitionNameIdTypeDTOs.isEmpty()) {
            newCache.add(new ArrayList<>());
        } else {
            for (int i = 0; i < definitionNameIdTypeDTOs.size(); i += CHUNK_SIZE) {
                newCache.add(new ArrayList<>(definitionNameIdTypeDTOs.subList(i, Math.min(definitionNameIdTypeDTOs.size(), i + CHUNK_SIZE))));
            }
        }

        List<DefinitionNameIdTypeDTO> firstChunk;
        int totalChunks;
        synchronized (definitionCacheLock) {
            definitionNameIdTypeDTOChunksCache.clear();
            definitionNameIdTypeDTOChunksCache.addAll(newCache);
            definitionNameIdTypeDTOChunksCacheUpdatedAtNanos = System.nanoTime();
            totalChunks = definitionNameIdTypeDTOChunksCache.size();
            firstChunk = definitionNameIdTypeDTOChunksCache.get(0);
        }

        return new AllDefinitionNameIdTypeDTO(totalChunks, firstChunk);
    }

    private DefinitionNameIdTypeListDTO getDefinitionsChunk(McpSyncServerExchange exchange, ChunkDTO param) throws Exception {
        log.debug("Get definitions chunk: {}", param);

        int chunkIndex = param.chunkIndex();
        List<DefinitionNameIdTypeDTO> chunk;
        synchronized (definitionCacheLock) {
            if (definitionNameIdTypeDTOChunksCacheUpdatedAtNanos < 0) {
                throw new RuntimeException("Chunk cache is not initialized. Call get_info_of_all_definitions first, then call get_chunk_of_all_definitions.");
            }
            if (System.nanoTime() - definitionNameIdTypeDTOChunksCacheUpdatedAtNanos >= CHUNK_CACHE_TTL_NANOS) {
                throw new RuntimeException("The cached chunk data was generated 180 or more seconds ago. The Astah model may have been updated. Call get_info_of_all_definitions again, then call get_chunk_of_all_definitions.");
            }
            if (chunkIndex < 0 || chunkIndex >= definitionNameIdTypeDTOChunksCache.size()) {
                throw new RuntimeException("Invalid chunk index: " + chunkIndex);
            }
            chunk = definitionNameIdTypeDTOChunksCache.get(chunkIndex);
        }

        return new DefinitionNameIdTypeListDTO(chunk);
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
                    log.debug("Label or ID is invalid: Label={}, ID={}", astahPresentation.getLabel(), astahPresentation.getID());
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
            labelIdTypeDTOChunksCacheUpdatedAtNanos = System.nanoTime();
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
            if (labelIdTypeDTOChunksCacheUpdatedAtNanos < 0) {
                throw new RuntimeException("Chunk cache is not initialized. Call get_info_of_all_prsts first, then call get_chunk_of_all_prsts.");
            }
            if (System.nanoTime() - labelIdTypeDTOChunksCacheUpdatedAtNanos >= CHUNK_CACHE_TTL_NANOS) {
                throw new RuntimeException("The cached chunk data was generated 180 or more seconds ago. The Astah model may have been updated. Call get_info_of_all_prsts again, then call get_chunk_of_all_prsts.");
            }
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
                log.debug("Label or ID is invalid: Label={}, ID={}", astahPresentation.getLabel(), astahPresentation.getID());
                continue;
            }
            labelIdTypeDTOs.add(LabelIdTypeDTOAssembler.toDTO(astahPresentation));
        }

        return new LabelIdTypeListDTO(labelIdTypeDTOs);
    }

    private ReportDTO getAllConstraintsAndConditions(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get all constraints and conditions: {}", param);

        StringBuilder contents = new StringBuilder();

        for (INamedElement astahNamedElement : projectAccessor.findElements(IConstraint.class)) {
            IConstraint astahConstraint = (IConstraint) astahNamedElement;
            String specification = astahConstraint.getSpecification();
            for (IElement astahConstrainedElement : astahConstraint.getConstrainedElement()) {
                if (astahConstrainedElement instanceof INamedElement astahConstrainedNamedElement) {
                    contents.append("# ").append(astahConstrainedNamedElement.getFullName(".")).append("\n");
                    contents.append("Type: ").append(NamedElementDTO.Type.getCorrespondingType(astahConstrainedNamedElement).typeName).append("\n");
                    contents.append("ID: ").append(astahConstrainedNamedElement.getId()).append("\n");

                    if (specification.startsWith("PRECONDITION:")) {
                        contents.append("Precondition:").append("\n");
                        contents.append("```").append("\n");
                        contents.append(Strings.CS.removeStart(specification, "PRECONDITION:")).append("\n");
                        contents.append("```").append("\n").append("\n");

                    } else if (specification.startsWith("POSTCONDITION:")) {
                        contents.append("Postcondition:").append("\n");
                        contents.append("```").append("\n");
                        contents.append(Strings.CS.removeStart(specification, "POSTCONDITION:")).append("\n");
                        contents.append("```").append("\n").append("\n");

                    } else if (specification.startsWith("BODYCONDITION:")) {
                        contents.append("Body condition:").append("\n");
                        contents.append("```").append("\n");
                        contents.append(Strings.CS.removeStart(specification, "BODYCONDITION:")).append("\n");
                        contents.append("```").append("\n").append("\n");

                    } else {
                        contents.append("Constraint:").append("\n");
                        contents.append("```").append("\n");
                        contents.append(specification).append("\n");
                        contents.append("```").append("\n").append("\n");
                    }
                }
            }
        }

        return new ReportDTO(contents.toString());
    }

    private SourceTargetNameIdTypeListDTO retrieveClassifiersThatReferenceOrBeReferencedBy(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Retrieve classifiers that reference or are referenced by: {}", param);

        IClass astahTargetClass = astahProToolSupport.getClass(param.id());

        Set<NameIdTypeDTO> inheritanceSourceClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> inheritanceTargetClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> realizationSourceClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> realizationTargetClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> associationSourceClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> associationTargetClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> dependencySourceClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> dependencyTargetClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> typeUsageSourceClassifier = new LinkedHashSet<>();
        Set<NameIdTypeDTO> typeUsageTargetClassifier = new LinkedHashSet<>();

        for (INamedElement astahNamedElement : projectAccessor.findElements(IClass.class)) {
            IClass astahClass = (IClass)astahNamedElement;

            if (astahClass.equals(astahTargetClass)) {
                // Processing for the target class itself
                for (IAttribute astahAttribute : astahClass.getAttributes()) {
                    // Association
                    if (astahAttribute.getAssociation() != null) {
                        // Both navigabilities are unspecified
                        if (astahAttribute.getAssociation().getMemberEnds()[0].getNavigability().equals("Unspecified")
                            && astahAttribute.getAssociation().getMemberEnds()[1].getNavigability().equals("Unspecified")) {

                            if (astahAttribute.getAssociation().getMemberEnds()[0].getOwner() == astahTargetClass) {
                                associationTargetClassifier.add(NameIdTypeDTOAssembler.toDTO((INamedElement) astahAttribute.getAssociation().getMemberEnds()[1].getOwner()));
                            }

                            if (astahAttribute.getAssociation().getMemberEnds()[1].getOwner() == astahTargetClass) {
                                associationTargetClassifier.add(NameIdTypeDTOAssembler.toDTO((INamedElement) astahAttribute.getAssociation().getMemberEnds()[0].getOwner()));
                            }

                        // One of the navigabilities is specified
                        } else {

                            if (astahAttribute.getAssociation().getMemberEnds()[0].getOwner() == astahTargetClass
                                && astahAttribute.getAssociation().getMemberEnds()[0].getNavigability().equals("Navigable")) {
                                associationTargetClassifier.add(NameIdTypeDTOAssembler.toDTO((INamedElement) astahAttribute.getAssociation().getMemberEnds()[1].getOwner()));
                            }

                            if (astahAttribute.getAssociation().getMemberEnds()[1].getOwner() == astahTargetClass
                                && astahAttribute.getAssociation().getMemberEnds()[1].getNavigability().equals("Navigable")) {
                                associationTargetClassifier.add(NameIdTypeDTOAssembler.toDTO((INamedElement) astahAttribute.getAssociation().getMemberEnds()[0].getOwner()));
                            }
                        }
                    // Attribute
                    } else {
                        if (astahAttribute.getType() != null) {
                            typeUsageTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahAttribute.getType()));
                        }
                    }
                }

                // Operation
                for (IOperation astahOperation : astahClass.getOperations()) {
                    if (astahOperation.getReturnType() != null) {
                        typeUsageTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahOperation.getReturnType()));
                    }

                    for (IParameter astahParameter : astahOperation.getParameters()) {
                        if (astahParameter.getType() != null) {
                            typeUsageTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahParameter.getType()));
                        }
                    }
                }

                // Realization
                for (IRealization astahRealization : astahClass.getClientRealizations()) {
                    if (astahRealization.getSupplier() != null) {
                        realizationTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahRealization.getSupplier()));
                    }
                }

                // Generalization
                for (IGeneralization astahGeneralization : astahClass.getGeneralizations()) {
                    if (astahGeneralization.getSuperType() != null) {
                        inheritanceTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahGeneralization.getSuperType()));
                    }
                }

                // Template parameter
                for (IClassifierTemplateParameter astahTemplateParameter : astahClass.getTemplateParameters()) {
                    if (astahTemplateParameter.getType() != null) {
                        typeUsageTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahTemplateParameter.getType()));
                    }
                }

                // Dependency
                for (IDependency astahDependency : astahClass.getClientDependencies()) {
                    if (astahDependency.getSupplier() != null) {
                        dependencyTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahDependency.getSupplier()));
                    }
                }

                // Usage
                for (IUsage astahUsage : astahClass.getClientUsages()) {
                    if (astahUsage.getSupplier() != null) {
                        dependencyTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahUsage.getSupplier()));
                    }
                }

                // Port
                for (IPort astahPort : astahClass.getPorts()) {
                    if (astahPort.getType() != null) {
                        typeUsageTargetClassifier.add(NameIdTypeDTOAssembler.toDTO(astahPort.getType()));
                    }
                }

            } else {
                // Processing for other classes
                for (IAttribute astahAttribute : astahClass.getAttributes()) {
                    // Association
                    if (astahAttribute.getAssociation() != null) {
                        // Both navigabilities are unspecified
                        if (astahAttribute.getAssociation().getMemberEnds()[0].getNavigability().equals("Unspecified")
                            && astahAttribute.getAssociation().getMemberEnds()[1].getNavigability().equals("Unspecified")) {

                            if (astahAttribute.getAssociation().getMemberEnds()[0].getOwner() == astahTargetClass) {
                                associationSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                            }

                            if (astahAttribute.getAssociation().getMemberEnds()[1].getOwner() == astahTargetClass) {
                                associationSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                            }

                        // One of the navigabilities is specified
                        } else {

                            if (astahAttribute.getAssociation().getMemberEnds()[0].getOwner() == astahTargetClass
                                && astahAttribute.getAssociation().getMemberEnds()[1].getNavigability().equals("Navigable")) {
                                associationSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                            }

                            if (astahAttribute.getAssociation().getMemberEnds()[1].getOwner() == astahTargetClass
                                && astahAttribute.getAssociation().getMemberEnds()[0].getNavigability().equals("Navigable")) {
                                associationSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                            }
                        }
                    // Attribute
                    } else {
                        if (astahAttribute.getType() != null) {
                            if (astahAttribute.getType().equals(astahTargetClass)) {
                                typeUsageSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                            }
                        }
                    }
                }

                // Operation
                for (IOperation astahOperation : astahClass.getOperations()) {
                    if (astahOperation.getReturnType() != null) {
                        if (astahOperation.getReturnType().equals(astahTargetClass)) {
                            typeUsageSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                        }
                    }

                    for (IParameter astahParameter : astahOperation.getParameters()) {
                        if (astahParameter.getType() != null) {
                            if (astahParameter.getType().equals(astahTargetClass)) {
                                typeUsageSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                            }
                        }
                    }
                }

                // Realization
                for (IRealization astahRealization : astahClass.getClientRealizations()) {
                    if (astahRealization.getSupplier() != null) {
                        if (astahRealization.getSupplier().equals(astahTargetClass)) {
                            realizationSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                        }
                    }
                }

                // Generalization
                for (IGeneralization astahGeneralization : astahClass.getGeneralizations()) {
                    if (astahGeneralization.getSuperType() != null) {
                        if (astahGeneralization.getSuperType().equals(astahTargetClass)) {
                            inheritanceSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                        }
                    }
                }

                // Template parameter
                for (IClassifierTemplateParameter astahTemplateParameter : astahClass.getTemplateParameters()) {
                    if (astahTemplateParameter.getType() != null) {
                        if (astahTemplateParameter.getType().equals(astahTargetClass)) {
                            typeUsageSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                        }
                    }
                }

                // Dependency
                for (IDependency astahDependency : astahClass.getClientDependencies()) {
                    if (astahDependency.getSupplier() != null) {
                        if (astahDependency.getSupplier().equals(astahTargetClass)) {
                            dependencySourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                        }
                    }
                }

                // Usage
                for (IUsage astahUsage : astahClass.getClientUsages()) {
                    if (astahUsage.getSupplier() != null) {
                        if (astahUsage.getSupplier().equals(astahTargetClass)) {
                            dependencySourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                        }
                    }
                }

                // Port
                for (IPort astahPort : astahClass.getPorts()) {
                    if (astahPort.getType() != null) {
                        if (astahPort.getType().equals(astahTargetClass)) {
                            typeUsageSourceClassifier.add(NameIdTypeDTOAssembler.toDTO(astahClass));
                        }
                    }
                }
            }
        }

        return new SourceTargetNameIdTypeListDTO(
                new ArrayList<>(inheritanceSourceClassifier),
                new ArrayList<>(inheritanceTargetClassifier),
                new ArrayList<>(realizationSourceClassifier),
                new ArrayList<>(realizationTargetClassifier),
                new ArrayList<>(associationSourceClassifier),
                new ArrayList<>(associationTargetClassifier),
                new ArrayList<>(dependencySourceClassifier),
                new ArrayList<>(dependencyTargetClassifier),
                new ArrayList<>(typeUsageSourceClassifier),
                new ArrayList<>(typeUsageTargetClassifier));
    }

    private NameIdTypeDefinitionListDTO searchWithinNamedElements(McpSyncServerExchange exchange, SearchDTO param) throws Exception {
        log.debug("Search within named elements: {}", param);

        // An empty search string targets all named elements
        String searchString = param.searchString() == null ? "" : param.searchString();
        boolean matchAll = searchString.isEmpty();

        INamedElement[] astahNamedElements = projectAccessor.findElements(INamedElement.class);
        List<NameIdTypeDefinitionDTO> nameIdTypeDefinitionDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            if (matchAll
                || Strings.CI.containsAny(astahNamedElement.getName(), searchString)
                || Strings.CI.containsAny(astahNamedElement.getDefinition(), searchString)) {
                nameIdTypeDefinitionDTOs.add(NameIdTypeDefinitionDTOAssembler.toDTO(astahNamedElement));
            }
        }

        return new NameIdTypeDefinitionListDTO(nameIdTypeDefinitionDTOs);
    }

    private LabelIdTypeListDTO searchWithinPresentations(McpSyncServerExchange exchange, SearchDTO param) throws Exception {
        log.debug("Search within presentations: {}", param);

        // An empty search string targets all presentations
        String searchString = param.searchString() == null ? "" : param.searchString();
        boolean matchAll = searchString.isEmpty();

        INamedElement[] astahNamedElements = projectAccessor.findElements(IDiagram.class);
        List<LabelIdTypeDTO> labelIdTypeDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            IDiagram astahDiagram = (IDiagram) astahNamedElement;
            for (IPresentation astahPresentation : astahDiagram.getPresentations()) {
                if (matchAll
                    || Strings.CI.containsAny(astahPresentation.getLabel(), searchString)) {
                    labelIdTypeDTOs.add(LabelIdTypeDTOAssembler.toDTO(astahPresentation));
                }
            }
        }

        return new LabelIdTypeListDTO(labelIdTypeDTOs);
    }

    private NameIdTypeNamespaceListDTO retrieveClassifiersWithinPackage(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Retrieve classifiers within package: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.id());

        List<NameIdTypeNamespaceDTO> nameIdTypeNamespaceDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : projectAccessor.findElements(IClass.class)) {
            IClass astahClass = (IClass)astahNamedElement;

            IElement owner = astahClass.getOwner();
            while (owner != null) {
                if (owner.equals(astahPackage)) {
                    nameIdTypeNamespaceDTOs.add(NameIdTypeNamespaceDTOAssembler.toDTO(astahClass));
                    break;
                }
                owner = owner.getOwner();
            }
        }

        return new NameIdTypeNamespaceListDTO(nameIdTypeNamespaceDTOs);
    }

    private PlantumlDTO retrievePackageStructureAsPlantuml(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Retrieve package structure as PlantUML: {}", param);

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.");
        }

        StringBuilder plantumlCode = new StringBuilder();
        plantumlCode.append("@startuml").append("\n");
        String indent = "";

        for (INamedElement astahNamedElement : astahProject.getOwnedElements()) {
            plantumlCode.append(getNamedElementPlantumlCode(astahNamedElement, indent));
        }

        plantumlCode.append("@enduml").append("\n");
        log.debug("Package structure as PlantUML: {}", plantumlCode.toString());

        return new PlantumlDTO(plantumlCode.toString());
    }

    private String getNamedElementPlantumlCode(INamedElement astahNamedElement, String indent) {
        StringBuilder plantumlCode = new StringBuilder();
        if (astahNamedElement instanceof IEnumeration) {
            IEnumeration astahEnumeration = (IEnumeration) astahNamedElement;
            plantumlCode.append(indent).append("enum ").append("\"" + astahEnumeration.getName() + "\"").append("\n");

        } else if (astahNamedElement instanceof IClass) {
            IClass astahClass = (IClass) astahNamedElement;
            if (Arrays.asList(astahClass.getStereotypes()).contains("interface")) {
                plantumlCode.append(indent).append("interface ").append("\"" + astahClass.getName() + "\"").append("\n");
            } else {
                plantumlCode.append(indent).append("class ").append("\"" + astahClass.getName() + "\"").append("\n");
            }

            if (astahClass.getNestedClasses().length > 0) {
                plantumlCode.append(indent).append("{").append("\n");
                for (IClass nestedClass : astahClass.getNestedClasses()) {
                    plantumlCode.append(getNamedElementPlantumlCode(nestedClass, indent + "  "));
                }
                plantumlCode.append(indent).append("}").append("\n");
            }
        }

        if (astahNamedElement instanceof IPackage) {
            IPackage astahPackage = (IPackage) astahNamedElement;
            plantumlCode.append(indent).append("package ").append("\"" + astahPackage.getName() + "\"").append("\n");

            if (astahPackage.getOwnedElements().length > 0) {
                plantumlCode.append(indent).append("{").append("\n");
                for (INamedElement nestedElement : astahPackage.getOwnedElements()) {
                    plantumlCode.append(getNamedElementPlantumlCode(nestedElement, indent + "  "));
                }
                plantumlCode.append(indent).append("}").append("\n");
            }
        }

        return plantumlCode.toString();
    }

    private PlantumlDTO retrieveClassifiersRelationshipsAsPlantuml(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Retrieve classifiers relationships as PlantUML: {}", param);

        StringBuilder plantumlCode = new StringBuilder();
        plantumlCode.append("@startuml").append("\n");

        // Sort the classifiers by their full namespace
        INamedElement[] astahNamedElements = projectAccessor.findElements(IClass.class);
        Arrays.sort(astahNamedElements, Comparator.comparing(
            element -> element.getFullNamespace(".") != null ? element.getFullNamespace(".") : "",
            Comparator.naturalOrder()
        ));

        for (INamedElement astahNamedElement : astahNamedElements) {
            IClass astahClass = (IClass)astahNamedElement;

            for (IAttribute astahAttribute : astahClass.getAttributes()) {
                // Association
                if (astahAttribute.getAssociation() != null) {
                    // Both navigabilities are unspecified
                    if (astahAttribute.getAssociation().getMemberEnds()[0].getNavigability().equals("Unspecified")
                        && astahAttribute.getAssociation().getMemberEnds()[1].getNavigability().equals("Unspecified")) {

                        if (astahAttribute.getAssociation().getMemberEnds()[0].getOwner() == astahClass) {
                            plantumlCode.append("\"" + astahClass.getName() + "\"").append(" --- ").append("\"" + ((IClass)(astahAttribute.getAssociation().getMemberEnds()[1].getOwner())).getName() + "\"").append("\n");
                        }

                    // One of the navigabilities is specified
                    } else {

                        if (astahAttribute.getAssociation().getMemberEnds()[0].getOwner() == astahClass
                            && astahAttribute.getAssociation().getMemberEnds()[0].getNavigability().equals("Navigable")) {
                            plantumlCode.append("\"" + astahClass.getName() + "\"").append(" --> ").append("\"" + ((IClass)(astahAttribute.getAssociation().getMemberEnds()[1].getOwner())).getName() + "\"").append("\n");
                        }

                        if (astahAttribute.getAssociation().getMemberEnds()[1].getOwner() == astahClass
                            && astahAttribute.getAssociation().getMemberEnds()[1].getNavigability().equals("Navigable")) {
                            plantumlCode.append("\"" + astahClass.getName() + "\"").append(" --> ").append("\"" + ((IClass)(astahAttribute.getAssociation().getMemberEnds()[0].getOwner())).getName() + "\"").append("\n");
                        }
                    }
                }
            }

            // Realization
            for (IRealization astahRealization : astahClass.getClientRealizations()) {
                if (astahRealization.getSupplier() != null) {
                    plantumlCode.append("\"" + astahClass.getName() + "\"").append(" ..|> ").append("\"" + astahRealization.getSupplier().getName() + "\"").append("\n");
                }
            }

            // Generalization
            for (IGeneralization astahGeneralization : astahClass.getGeneralizations()) {
                if (astahGeneralization.getSuperType() != null) {
                    plantumlCode.append("\"" + astahClass.getName() + "\"").append(" --|> ").append("\"" + astahGeneralization.getSuperType().getName() + "\"").append("\n");
                }
            }

            // Dependency
            for (IDependency astahDependency : astahClass.getClientDependencies()) {
                if (astahDependency.getSupplier() != null) {
                    plantumlCode.append("\"" + astahClass.getName() + "\"").append(" ..> ").append("\"" + astahDependency.getSupplier().getName() + "\"").append("\n");
                }
            }

            // Usage
            for (IUsage astahUsage : astahClass.getClientUsages()) {
                if (astahUsage.getSupplier() != null) {
                    plantumlCode.append("\"" + astahClass.getName() + "\"").append(" ..> ").append("\"" + astahUsage.getSupplier().getName() + "\"").append("\n");
                }
            }
        }

        plantumlCode.append("@enduml").append("\n");
        log.debug("Classifiers relationships as PlantUML: {}", plantumlCode.toString());

        return new PlantumlDTO(plantumlCode.toString());
    }

    private PlantumlDTO getRelationshipsAsPlantumlCode(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get relationships as PlantUML code: {}", param);

        StringBuilder plantumlCode = new StringBuilder("@startuml\n");

        List<INamedElement> namedElements = new ArrayList<>(Arrays.asList(projectAccessor.findElements(INamedElement.class)));
        namedElements.sort(Comparator.comparing(element -> element.getFullName(".")));

        for (INamedElement namedElement : namedElements) {
            if (namedElement.getName().isEmpty()) {
                continue;
            }

            if (namedElement instanceof IClass) {
                // Add nested classes
                for (IClass nestedClass : ((IClass) namedElement).getNestedClasses()) {
                    plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                            .append(" +-- ")
                            .append("\"").append(nestedClass.getFullName(".")).append("\"\n");
                }

                // Add inheritances
                for (IGeneralization generalization : ((IClass) namedElement).getGeneralizations()) {
                    INamedElement superType = generalization.getSuperType();
                    if (superType == null) {
                        continue;
                    }

                    plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                            .append(" --|> ")
                            .append("\"").append(superType.getFullName(".")).append("\"\n");
                }
            }

            // Add realizations
            for (IRealization realization : namedElement.getClientRealizations()) {
                INamedElement supplier = realization.getSupplier();
                if (supplier == null) {
                    continue;
                }

                plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                        .append(" ..|> ")
                        .append("\"").append(supplier.getFullName(".")).append("\"\n");
            }

            // Add dependencies
            for (IDependency dependency : namedElement.getClientDependencies()) {
                INamedElement supplier = dependency.getSupplier();
                if (supplier == null) {
                    continue;
                }

                plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                        .append(" ..> ")
                        .append("\"").append(supplier.getFullName(".")).append("\"\n");
            }

            // Add usages
            for (IUsage usage : namedElement.getClientUsages()) {
                INamedElement supplier = usage.getSupplier();
                if (supplier == null) {
                    continue;
                }

                plantumlCode.append("\"").append(namedElement.getFullName(".")).append("\"")
                        .append(" ..> ")
                        .append("\"").append(supplier.getFullName(".")).append("\"\n");
            }
        }
        plantumlCode.append("@enduml\n");

        return new PlantumlDTO(plantumlCode.toString());
    }
}
