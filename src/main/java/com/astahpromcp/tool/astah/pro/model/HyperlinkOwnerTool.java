package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
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
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IHyperlink;
import com.change_vision.jude.api.inf.model.IHyperlinkOwner;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IHyperlinkOwner.html
// However, for ease of use as a tool, it does not implement the Astah API as-is.
@Slf4j
public class HyperlinkOwnerTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public HyperlinkOwnerTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
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
                "Add a file path hyperlink (absolute path or relative path) to the specified node presentation (specified by ID), and return the node presentation after it is edited. If the node presentation draws a model element, the hyperlink is added to that model element and is shown on all of its presentations.",
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
                "Remove all URL hyperlinks from the specified node presentation (specified by ID), and return the node presentation after it is edited. If the node presentation draws a model element, the hyperlinks are removed from that model element and disappear from all of its presentations.",
                this::removeAllUrlHyperlinksFromNodePresentation,
                IdDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_file_path_hyperlinks_from_node_prst",
                "Remove all file path hyperlinks from the specified node presentation (specified by ID), and return the node presentation after it is edited. If the node presentation draws a model element, the hyperlinks are removed from that model element and disappear from all of its presentations.",
                this::removeAllFilePathHyperlinksFromNodePresentation,
                IdDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_all_named_elem_hyperlinks_from_node_prst",
                "Remove all named element hyperlinks from the specified node presentation (specified by ID), and return the node presentation after it is edited. If the node presentation draws a model element, the hyperlinks are removed from that model element and disappear from all of its presentations.",
                this::removeAllNamedElementHyperlinksFromNodePresentation,
                IdDTO.class,
                NodePresentationDTO.class)
            );
    }

    private NamedElementDTO addUrlHyperlinkToNamedElement(NamedElementWithUrlHyperlinkDTO param) throws Exception {
        String url = param.url();
        if (url == null || !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with \"https://\", but was: " + url);
        }

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        txnAstah.run( () -> {
            astahNamedElement.createURLHyperlink(url, param.hyperlinkComment());
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    // Return the normalized file path of a file hyperlink
    private static Path requireFilePath(String filePath) {
        Path p = filePath == null ? null : Paths.get(filePath).normalize();
        // A path that names no file ("", ".", "C:\") is rejected here
        if (p == null || p.getFileName() == null || p.getFileName().toString().isEmpty()) {
            throw new IllegalArgumentException("filePath must be a path that ends with a file name, but was: "
                + (filePath == null ? null : "\"" + filePath + "\""));
        }
        return p;
    }

    private NamedElementDTO addFilePathHyperlinkToNamedElement(NamedElementWithFilePathHyperlinkDTO param) throws Exception {
        Path p = requireFilePath(param.filePath());

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        txnAstah.run( () -> {
            astahNamedElement.createFileHyperlink(
                p.getFileName().toString(),
                p.getParent() == null ? "" : p.getParent().toString(),
                param.hyperlinkComment());
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO addNamedElementHyperlinkToNamedElement(NamedElementWithNamedElementHyperlinkDTO param) throws Exception {
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());
        INamedElement astahNamedElementToLink = astahProToolSupport.getNamedElement(param.namedElementIdToLink());

        txnAstah.run( () -> {
            astahNamedElement.createElementHyperlink(
                astahNamedElementToLink,
                param.hyperlinkComment());
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO removeAllUrlHyperlinksFromNamedElement(IdDTO param) throws Exception {
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        List<IHyperlink> urlHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : astahNamedElement.getHyperlinks()) {
            if (hyperlink.isURL()) {
                urlHyperlinks.add(hyperlink);
            }
        }

        if (!urlHyperlinks.isEmpty()) {
            txnAstah.run( () -> {
                for (IHyperlink hyperlink : urlHyperlinks) {
                    astahNamedElement.deleteHyperlink(hyperlink);
                }
            });
        }

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO removeAllFilePathHyperlinksFromNamedElement(IdDTO param) throws Exception {
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        List<IHyperlink> filePathHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : astahNamedElement.getHyperlinks()) {
            if (hyperlink.isFile()) {
                filePathHyperlinks.add(hyperlink);
            }
        }

        if (!filePathHyperlinks.isEmpty()) {
            txnAstah.run( () -> {
                for (IHyperlink hyperlink : filePathHyperlinks) {
                    astahNamedElement.deleteHyperlink(hyperlink);
                }
            });
        }

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO removeAllNamedElementHyperlinksFromNamedElement(IdDTO param) throws Exception {
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        List<IHyperlink> namedElementHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : astahNamedElement.getHyperlinks()) {
            if (hyperlink.isModel()) {
                namedElementHyperlinks.add(hyperlink);
            }
        }

        if (!namedElementHyperlinks.isEmpty()) {
            txnAstah.run( () -> {
                for (IHyperlink hyperlink : namedElementHyperlinks) {
                    astahNamedElement.deleteHyperlink(hyperlink);
                }
            });
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

    private NodePresentationDTO addUrlHyperlinkToNodePresentation(NodePresentationWithUrlHyperlinkDTO param) throws Exception {
        String url = param.url();
        if (url == null || !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with \"https://\", but was: " + url);
        }

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        txnAstah.run( () -> {
            hyperlinkOwner.createURLHyperlink(url, param.hyperlinkComment());
        });

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }

    private NodePresentationDTO addFilePathHyperlinkToNodePresentation(NodePresentationWithFilePathHyperlinkDTO param) throws Exception {
        Path p = requireFilePath(param.filePath());

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        txnAstah.run( () -> {
            hyperlinkOwner.createFileHyperlink(
                p.getFileName().toString(),
                p.getParent() == null ? "" : p.getParent().toString(),
                param.hyperlinkComment());
        });

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }

    private NodePresentationDTO addNamedElementHyperlinkToNodePresentation(NodePresentationWithNamedElementHyperlinkDTO param) throws Exception {
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        INamedElement astahNamedElementToLink = astahProToolSupport.getNamedElement(param.namedElementIdToLink());

        txnAstah.run( () -> {
            hyperlinkOwner.createElementHyperlink(
                astahNamedElementToLink,
                param.hyperlinkComment());
        });

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }

    private NodePresentationDTO removeAllUrlHyperlinksFromNodePresentation(IdDTO param) throws Exception {
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        List<IHyperlink> urlHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : hyperlinkOwner.getHyperlinks()) {
            if (hyperlink.isURL()) {
                urlHyperlinks.add(hyperlink);
            }
        }

        if (!urlHyperlinks.isEmpty()) {
            txnAstah.run( () -> {
                for (IHyperlink hyperlink : urlHyperlinks) {
                    hyperlinkOwner.deleteHyperlink(hyperlink);
                }
            });
        }

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }

    private NodePresentationDTO removeAllFilePathHyperlinksFromNodePresentation(IdDTO param) throws Exception {
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        List<IHyperlink> filePathHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : hyperlinkOwner.getHyperlinks()) {
            if (hyperlink.isFile()) {
                filePathHyperlinks.add(hyperlink);
            }
        }

        if (!filePathHyperlinks.isEmpty()) {
            txnAstah.run( () -> {
                for (IHyperlink hyperlink : filePathHyperlinks) {
                    hyperlinkOwner.deleteHyperlink(hyperlink);
                }
            });
        }

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }

    private NodePresentationDTO removeAllNamedElementHyperlinksFromNodePresentation(IdDTO param) throws Exception {
        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.id());
        IHyperlinkOwner hyperlinkOwner = getHyperlinkOwnerForNodePresentation(astahNodePresentation);

        List<IHyperlink> namedElementHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : hyperlinkOwner.getHyperlinks()) {
            if (hyperlink.isModel()) {
                namedElementHyperlinks.add(hyperlink);
            }
        }

        if (!namedElementHyperlinks.isEmpty()) {
            txnAstah.run( () -> {
                for (IHyperlink hyperlink : namedElementHyperlinks) {
                    hyperlinkOwner.deleteHyperlink(hyperlink);
                }
            });
        }

        return NodePresentationDTOAssembler.toDTO(astahNodePresentation);
    }
}
