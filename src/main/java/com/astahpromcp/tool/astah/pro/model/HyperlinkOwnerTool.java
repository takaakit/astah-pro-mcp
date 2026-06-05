package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NamedElementWithFilePathHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NamedElementWithNamedElementHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NamedElementWithUrlHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.NamedElementDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithFilePathHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithNamedElementHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithUrlHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IHyperlink;
import com.change_vision.jude.api.inf.model.IHyperlinkOwner;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IHyperlinkOwner.html
// However, for ease of use as a tool, it does not implement the Astah API as-is.
@Slf4j
public class HyperlinkOwnerTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public HyperlinkOwnerTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
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
            log.error("Failed to create hyperlink owner tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "add_url_hyperlink_to_named_elem",
                "Add a URL hyperlink (start with \"https://\") to the specified named element (specified by ID), and return the named element after it is edited.",
                this::addUrlHyperlinkToNamedElement,
                NamedElementWithUrlHyperlinkDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_file_path_hyperlink_to_named_elem",
                "Add a file path hyperlink (absolute path or relative path) to the specified named element (specified by ID), and return the named element after it is edited.",
                this::addFilePathHyperlinkToNamedElement,
                NamedElementWithFilePathHyperlinkDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_named_element_hyperlink_to_named_elem",
                "Add a named element hyperlink to the specified named element (specified by ID), and return the named element after it is edited.",
                this::addNamedElementHyperlinkToNamedElement,
                NamedElementWithNamedElementHyperlinkDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_url_hyperlinks_from_named_elem",
                "Remove all URL hyperlinks from the specified named element (specified by ID), and return the named element after it is edited.",
                this::removeAllUrlHyperlinksFromNamedElement,
                IdDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_file_path_hyperlinks_from_named_elem",
                "Remove all file path hyperlinks from the specified named element (specified by ID), and return the named element after it is edited.",
                this::removeAllFilePathHyperlinksFromNamedElement,
                IdDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_named_elem_hyperlinks_from_named_elem",
                "Remove all named element hyperlinks from the specified named element (specified by ID), and return the named element after it is edited.",
                this::removeAllNamedElementHyperlinksFromNamedElement,
                IdDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_url_hyperlink_to_node_prst",
                "Add a URL hyperlink (start with \"https://\") to the specified node presentation (specified by ID), and return the node presentation after it is edited.",
                this::addUrlHyperlinkToNodePresentation,
                NodePresentationWithUrlHyperlinkDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_file_path_hyperlink_to_node_prst",
                "Add a file path hyperlink (absolute path or relative path) to the specified node presentation (specified by ID), and return the node presentation after it is edited.",
                this::addFilePathHyperlinkToNodePresentation,
                NodePresentationWithFilePathHyperlinkDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_named_element_hyperlink_to_node_prst",
                "Add a named element hyperlink to the specified node presentation (specified by ID), and return the node presentation after it is edited.",
                this::addNamedElementHyperlinkToNodePresentation,
                NodePresentationWithNamedElementHyperlinkDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_url_hyperlinks_from_node_prst",
                "Remove all URL hyperlinks from the specified node presentation (specified by ID), and return the node presentation after it is edited.",
                this::removeAllUrlHyperlinksFromNodePresentation,
                IdDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_file_path_hyperlinks_from_node_prst",
                "Remove all file path hyperlinks from the specified node presentation (specified by ID), and return the node presentation after it is edited.",
                this::removeAllFilePathHyperlinksFromNodePresentation,
                IdDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_named_elem_hyperlinks_from_node_prst",
                "Remove all named element hyperlinks from the specified node presentation (specified by ID), and return the node presentation after it is edited.",
                this::removeAllNamedElementHyperlinksFromNodePresentation,
                IdDTO.class,
                NodePresentationDTO.class)
            );
    }

    private NamedElementDTO addUrlHyperlinkToNamedElement(McpSyncServerExchange exchange, NamedElementWithUrlHyperlinkDTO param) throws Exception {
        log.debug("Add URL hyperlink to named element: {}", param);

        String url = param.url();
        if (url == null || !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with \"https://\", but was: " + url);
        }

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        try {
            transactionManager.beginTransaction();
            astahNamedElement.createURLHyperlink(url, param.hyperlinkComment());
            transactionManager.endTransaction();

            return NamedElementDTOAssembler.toDTO(astahNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NamedElementDTO addFilePathHyperlinkToNamedElement(McpSyncServerExchange exchange, NamedElementWithFilePathHyperlinkDTO param) throws Exception {
        log.debug("Add file path hyperlink to named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());
        Path p = Paths.get(param.filePath()).normalize();
        
        try {
            transactionManager.beginTransaction();
            astahNamedElement.createFileHyperlink(
            p.getFileName() == null ? "" : p.getFileName().toString(),
            p.getParent() == null ? "" : p.getParent().toString(),
            param.hyperlinkComment());
            transactionManager.endTransaction();

            return NamedElementDTOAssembler.toDTO(astahNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NamedElementDTO addNamedElementHyperlinkToNamedElement(McpSyncServerExchange exchange, NamedElementWithNamedElementHyperlinkDTO param) throws Exception {
        log.debug("Add named element hyperlink to named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());
        INamedElement astahNamedElementToLink = astahProToolSupport.getNamedElement(param.namedElementIdToLink());

        try {
            transactionManager.beginTransaction();
            astahNamedElement.createElementHyperlink(
            astahNamedElementToLink,
            param.hyperlinkComment());
            transactionManager.endTransaction();

            return NamedElementDTOAssembler.toDTO(astahNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NamedElementDTO removeAllUrlHyperlinksFromNamedElement(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Remove all URL hyperlinks from named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        for (IHyperlink hyperlink : astahNamedElement.getHyperlinks()) {
            if (hyperlink.isURL()) {
            try {
                transactionManager.beginTransaction();
                astahNamedElement.deleteHyperlink(hyperlink);
                transactionManager.endTransaction();

            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
            }
        }

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO removeAllFilePathHyperlinksFromNamedElement(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Remove all file path hyperlinks from named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        for (IHyperlink hyperlink : astahNamedElement.getHyperlinks()) {
            if (hyperlink.isFile()) {
            try {
                transactionManager.beginTransaction();
                astahNamedElement.deleteHyperlink(hyperlink);
                transactionManager.endTransaction();

            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
            }
        }

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO removeAllNamedElementHyperlinksFromNamedElement(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Remove all named element hyperlinks from named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        for (IHyperlink hyperlink : astahNamedElement.getHyperlinks()) {
            if (hyperlink.isModel()) {
            try {
                transactionManager.beginTransaction();
                astahNamedElement.deleteHyperlink(hyperlink);
                transactionManager.endTransaction();

            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
            }
        }

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    // Returns the IHyperlinkOwner to edit for the given node presentation.
    // If a node presentation has a model element attached, the model element's hyperlink must be edited.
    // If not, the node presentation's own hyperlink must be edited.
    private IHyperlinkOwner getHyperlinkOwnerForNodePresentation(INodePresentation astahNodePresentation) {
        IHyperlinkOwner hyperlinkOwner = (IHyperlinkOwner) astahNodePresentation.getModel();
        if (hyperlinkOwner == null) {
            hyperlinkOwner = (IHyperlinkOwner) astahNodePresentation;
        }
        return hyperlinkOwner;
    }

    private NodePresentationDTO addUrlHyperlinkToNodePresentation(McpSyncServerExchange exchange, NodePresentationWithUrlHyperlinkDTO param) throws Exception {
        log.debug("Add URL hyperlink to node presentation: {}", param);

        String url = param.url();
        if (url == null || !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with \"https://\", but was: " + url);
        }

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        try {
            transactionManager.beginTransaction();
            hyperlinkOwner.createURLHyperlink(url, param.hyperlinkComment());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO addFilePathHyperlinkToNodePresentation(McpSyncServerExchange exchange, NodePresentationWithFilePathHyperlinkDTO param) throws Exception {
        log.debug("Add file path hyperlink to node presentation: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        Path p = Paths.get(param.filePath()).normalize();
        
        try {
            transactionManager.beginTransaction();
            hyperlinkOwner.createFileHyperlink(
            p.getFileName() == null ? "" : p.getFileName().toString(),
            p.getParent() == null ? "" : p.getParent().toString(),
            param.hyperlinkComment());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO addNamedElementHyperlinkToNodePresentation(McpSyncServerExchange exchange, NodePresentationWithNamedElementHyperlinkDTO param) throws Exception {
        log.debug("Add named element hyperlink to node presentation: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        INamedElement astahNamedElementToLink = astahProToolSupport.getNamedElement(param.namedElementIdToLink());

        try {
            transactionManager.beginTransaction();
            hyperlinkOwner.createElementHyperlink(
            astahNamedElementToLink,
            param.hyperlinkComment());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO removeAllUrlHyperlinksFromNodePresentation(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Remove all URL hyperlinks from node presentation: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        for (IHyperlink hyperlink : hyperlinkOwner.getHyperlinks()) {
            if (hyperlink.isURL()) {
            try {
                transactionManager.beginTransaction();
                hyperlinkOwner.deleteHyperlink(hyperlink);
                transactionManager.endTransaction();

            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
            }
        }

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }

    private NodePresentationDTO removeAllFilePathHyperlinksFromNodePresentation(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Remove all file path hyperlinks from node presentation: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        for (IHyperlink hyperlink : hyperlinkOwner.getHyperlinks()) {
            if (hyperlink.isFile()) {
            try {
                transactionManager.beginTransaction();
                hyperlinkOwner.deleteHyperlink(hyperlink);
                transactionManager.endTransaction();

            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
            }
        }

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }

    private NodePresentationDTO removeAllNamedElementHyperlinksFromNodePresentation(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Remove all named element hyperlinks from node presentation: {}", param);

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        for (IHyperlink hyperlink : hyperlinkOwner.getHyperlinks()) {
            if (hyperlink.isModel()) {
            try {
                transactionManager.beginTransaction();
                hyperlinkOwner.deleteHyperlink(hyperlink);
                transactionManager.endTransaction();

            } catch (Exception e) {
                transactionManager.abortTransaction();
                throw e;
            }
            }
        }

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }
}
