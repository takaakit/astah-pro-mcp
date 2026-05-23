package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NamedElementWithFilePathHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NamedElementWithNamedElementHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NamedElementWithUrlHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithFilePathHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithNamedElementHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithUrlHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IHyperlinkOwner;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.change_vision.jude.api.inf.model.IHyperlink;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class HyperlinkOwnerToolTest {

    private ProjectAccessor projectAccessor;
    private HyperlinkOwnerTool tool;
    private Method addUrlHyperlinkToNamedElement;
    private Method addFilePathHyperlinkToNamedElement;
    private Method addNamedElementHyperlinkToNamedElement;
    private Method removeAllUrlHyperlinksFromNamedElement;
    private Method removeAllFilePathHyperlinksFromNamedElement;
    private Method removeAllNamedElementHyperlinksFromNamedElement;
    private Method addUrlHyperlinkToNodePresentation;
    private Method addFilePathHyperlinkToNodePresentation;
    private Method addNamedElementHyperlinkToNodePresentation;
    private Method removeAllUrlHyperlinksFromNodePresentation;
    private Method removeAllFilePathHyperlinksFromNodePresentation;
    private Method removeAllNamedElementHyperlinksFromNodePresentation;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/HyperlinkOwnerToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new HyperlinkOwnerTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // addUrlHyperlinkToNamedElement() method
        addUrlHyperlinkToNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addUrlHyperlinkToNamedElement",
            McpSyncServerExchange.class,
            NamedElementWithUrlHyperlinkDTO.class);

        // addFilePathHyperlinkToNamedElement() method
        addFilePathHyperlinkToNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addFilePathHyperlinkToNamedElement",
            McpSyncServerExchange.class,
            NamedElementWithFilePathHyperlinkDTO.class);

        // addNamedElementHyperlinkToNamedElement() method
        addNamedElementHyperlinkToNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addNamedElementHyperlinkToNamedElement",
            McpSyncServerExchange.class,
            NamedElementWithNamedElementHyperlinkDTO.class);

        // removeAllUrlHyperlinksFromNamedElement() method
        removeAllUrlHyperlinksFromNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllUrlHyperlinksFromNamedElement",
            McpSyncServerExchange.class,
            IdDTO.class);

        // removeAllFilePathHyperlinksFromNamedElement() method
        removeAllFilePathHyperlinksFromNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllFilePathHyperlinksFromNamedElement",
            McpSyncServerExchange.class,
            IdDTO.class);

        // removeAllNamedElementHyperlinksFromNamedElement() method
        removeAllNamedElementHyperlinksFromNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllNamedElementHyperlinksFromNamedElement",
            McpSyncServerExchange.class,
            IdDTO.class);

        // addUrlHyperlinkToNodePresentation() method
        addUrlHyperlinkToNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addUrlHyperlinkToNodePresentation",
            McpSyncServerExchange.class,
            NodePresentationWithUrlHyperlinkDTO.class);

        // addFilePathHyperlinkToNodePresentation() method
        addFilePathHyperlinkToNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addFilePathHyperlinkToNodePresentation",
            McpSyncServerExchange.class,
            NodePresentationWithFilePathHyperlinkDTO.class);

        // addNamedElementHyperlinkToNodePresentation() method
        addNamedElementHyperlinkToNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addNamedElementHyperlinkToNodePresentation",
            McpSyncServerExchange.class,
            NodePresentationWithNamedElementHyperlinkDTO.class);

        // removeAllUrlHyperlinksFromNodePresentation() method
        removeAllUrlHyperlinksFromNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllUrlHyperlinksFromNodePresentation",
            McpSyncServerExchange.class,
            IdDTO.class);

        // removeAllFilePathHyperlinksFromNodePresentation() method
        removeAllFilePathHyperlinksFromNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllFilePathHyperlinksFromNodePresentation",
            McpSyncServerExchange.class,
            IdDTO.class);

        // removeAllNamedElementHyperlinksFromNodePresentation() method
        removeAllNamedElementHyperlinksFromNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllNamedElementHyperlinksFromNodePresentation",
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
    void addUrlHyperlinkToNamedElement_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        NamedElementWithUrlHyperlinkDTO inputDTO = new NamedElementWithUrlHyperlinkDTO(
            namedElement.getId(),
            "https://example.com/test",
            "Test comment");
        
        // Check URL hyperlinks before adding
        assertFalse(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(h -> h.isURL() && "https://example.com/test".equals(h.getName())));

        // ----------------------------------------
        // Call addUrlHyperlink()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addUrlHyperlinkToNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.urlHyperlinks().stream()
            .anyMatch(h -> "https://example.com/test".equals(h.url())));

        // Check URL hyperlinks after adding
        assertTrue(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(h -> h.isURL() && "https://example.com/test".equals(h.getName())));
    }

    @Test
    void addUrlHyperlinkToNamedElement_ng() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        NamedElementWithUrlHyperlinkDTO inputDTO = new NamedElementWithUrlHyperlinkDTO(
            namedElement.getId(),
            "http://example.com",
            null);

        // ----------------------------------------
        // Call addUrlHyperlink()
        // ----------------------------------------
        Exception e = assertThrows(Exception.class, () ->
            TestSupport.instance().invokeToolMethodReturningDto(
                addUrlHyperlinkToNamedElement,
                tool,
                inputDTO,
                NamedElementDTO.class));
    }

    @Test
    void addFilePathHyperlinkToNamedElement_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        NamedElementWithFilePathHyperlinkDTO inputDTO = new NamedElementWithFilePathHyperlinkDTO(
            namedElement.getId(),
            "C:\\data\\test.txt",
            "File link comment");

        // Check file path hyperlinks before adding
        assertFalse(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(h -> h.isFile() && h.getPath().contains("test.txt")));

        // ----------------------------------------
        // Call addFilePathHyperlink()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addFilePathHyperlinkToNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.filePathHyperlinks().stream()
            .anyMatch(h -> h.filePath() != null && h.filePath().contains("test.txt")));

        // Check file path hyperlinks after adding
        assertTrue(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(h -> h.isFile() && h.getName().contains("test.txt")));
    }

    @Test
    void addNamedElementHyperlinkToNamedElement_ok() throws Exception {
        // Get target element
        INamedElement targetElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Get element to link
        INamedElement elementToLink = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Baz");

        // Create input DTO
        NamedElementWithNamedElementHyperlinkDTO inputDTO = new NamedElementWithNamedElementHyperlinkDTO(
            targetElement.getId(),
            elementToLink.getId(),
            "Element link comment");

        // Check named element hyperlinks before adding
        assertFalse(Arrays.stream(targetElement.getHyperlinks())
            .anyMatch(h -> h.isModel() && elementToLink.getId().equals(h.getName())));

        // ----------------------------------------
        // Call addNamedElementHyperlink()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addNamedElementHyperlinkToNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.namedElementHyperlinks().stream()
            .anyMatch(h -> elementToLink.getId().equals(h.namedElementId())));

        // Check named element hyperlinks after adding
        assertTrue(Arrays.stream(targetElement.getHyperlinks())
            .anyMatch(h -> h.isModel() && elementToLink.getId().equals(h.getName())));
    }

    @Test
    void removeAllUrlHyperlinksFromNamedElement_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());

        // Check URL hyperlinks before removing (model should have URL hyperlinks)
        assertTrue(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(IHyperlink::isURL));

        // ----------------------------------------
        // Call removeAllUrlHyperlinks()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllUrlHyperlinksFromNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.urlHyperlinks().isEmpty());

        // Check URL hyperlinks after removing
        assertFalse(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(IHyperlink::isURL));
    }

    @Test
    void removeAllFilePathHyperlinksFromNamedElement_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());

        // Check file path hyperlinks before removing
        assertTrue(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(IHyperlink::isFile));

        // ----------------------------------------
        // Call removeAllFilePathHyperlinks()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllFilePathHyperlinksFromNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.filePathHyperlinks().isEmpty());

        // Check file path hyperlinks after removing
        assertFalse(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(IHyperlink::isFile));
    }

    @Test
    void removeAllNamedElementHyperlinksFromNamedElement_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());

        // Check named element hyperlinks before removing
        assertTrue(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(IHyperlink::isModel));

        // ----------------------------------------
        // Call removeAllNamedElementHyperlinks()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllNamedElementHyperlinksFromNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.namedElementHyperlinks().isEmpty());

        // Check named element hyperlinks after removing
        assertFalse(Arrays.stream(namedElement.getHyperlinks())
            .anyMatch(IHyperlink::isModel));
    }

    @Test
    void addUrlHyperlinkToNodePresentation_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Create input DTO
        NodePresentationWithUrlHyperlinkDTO inputDTO = new NodePresentationWithUrlHyperlinkDTO(
            nodePresentation.getID(),
            "https://example.com/node-test",
            "Node URL comment");

        // Get a model element of the node presentation
        IHyperlinkOwner urlHyperlinkOwner = (IHyperlinkOwner) nodePresentation.getModel();

        // Check URL hyperlinks before adding
        assertFalse(Arrays.stream(urlHyperlinkOwner.getHyperlinks())
            .anyMatch(h -> h.isURL() && "https://example.com/node-test".equals(h.getName())));

        // ----------------------------------------
        // Call addUrlHyperlinkToNodePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addUrlHyperlinkToNodePresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.urlHyperlinks().stream()
            .anyMatch(h -> "https://example.com/node-test".equals(h.url())));

        // Check URL hyperlinks after adding
        assertTrue(Arrays.stream(urlHyperlinkOwner.getHyperlinks())
            .anyMatch(h -> h.isURL() && "https://example.com/node-test".equals(h.getName())));
    }

    @Test
    void addUrlHyperlinkToNodePresentation_ng() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Create input DTO
        NodePresentationWithUrlHyperlinkDTO inputDTO = new NodePresentationWithUrlHyperlinkDTO(
            nodePresentation.getID(),
            "http://example.com",
            null);

        // ----------------------------------------
        // Call addUrlHyperlinkToNodePresentation()
        // ----------------------------------------
        assertThrows(Exception.class, () ->
            TestSupport.instance().invokeToolMethodReturningDto(
                addUrlHyperlinkToNodePresentation,
                tool,
                inputDTO,
                NodePresentationDTO.class));
    }

    @Test
    void addFilePathHyperlinkToNodePresentation_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Create input DTO
        NodePresentationWithFilePathHyperlinkDTO inputDTO = new NodePresentationWithFilePathHyperlinkDTO(
            nodePresentation.getID(),
            "C:\\data\\node-file.txt",
            "Node file link comment");

        // Get a model element of the node presentation
        IHyperlinkOwner fileHyperlinkOwner = (IHyperlinkOwner) nodePresentation.getModel();

        // Check file path hyperlinks before adding
        assertFalse(Arrays.stream(fileHyperlinkOwner.getHyperlinks())
            .anyMatch(h -> h.isFile() && h.getName().contains("node-file.txt")));

        // ----------------------------------------
        // Call addFilePathHyperlinkToNodePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addFilePathHyperlinkToNodePresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.filePathHyperlinks().stream()
            .anyMatch(h -> h.filePath() != null && h.filePath().contains("node-file.txt")));

        // Check file path hyperlinks after adding
        assertTrue(Arrays.stream(fileHyperlinkOwner.getHyperlinks())
            .anyMatch(h -> h.isFile() && h.getName().contains("node-file.txt")));
    }

    @Test
    void addNamedElementHyperlinkToNodePresentation_ok() throws Exception {
        // Get target node presentation
        INodePresentation targetNodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Get element to link
        INamedElement elementToLink = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Baz");

        // Create input DTO
        NodePresentationWithNamedElementHyperlinkDTO inputDTO = new NodePresentationWithNamedElementHyperlinkDTO(
            targetNodePresentation.getID(),
            elementToLink.getId(),
            "Node element link comment");

        // Get a model element of the node presentation
        IHyperlinkOwner elemHyperlinkOwner = (IHyperlinkOwner) targetNodePresentation.getModel();

        // Check named element hyperlinks before adding
        assertFalse(Arrays.stream(elemHyperlinkOwner.getHyperlinks())
            .anyMatch(h -> h.isModel() && elementToLink.getId().equals(h.getName())));

        // ----------------------------------------
        // Call addNamedElementHyperlinkToNodePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addNamedElementHyperlinkToNodePresentation,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.namedElementHyperlinks().stream()
            .anyMatch(h -> elementToLink.getId().equals(h.namedElementId())));

        // Check named element hyperlinks after adding
        assertTrue(Arrays.stream(elemHyperlinkOwner.getHyperlinks())
            .anyMatch(h -> h.isModel() && elementToLink.getId().equals(h.getName())));
    }

    @Test
    void removeAllUrlHyperlinksFromNodePresentation_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Create input DTO
        IdDTO removeInputDTO = new IdDTO(nodePresentation.getID());

        // Get a model element of the node presentation
        IHyperlinkOwner removeUrlOwner = (IHyperlinkOwner) nodePresentation.getModel();

        // Check URL hyperlinks before removing
        assertTrue(Arrays.stream(removeUrlOwner.getHyperlinks())
            .anyMatch(IHyperlink::isURL));

        // ----------------------------------------
        // Call removeAllUrlHyperlinksFromNodePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllUrlHyperlinksFromNodePresentation,
            tool,
            removeInputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.urlHyperlinks().isEmpty());

        // Check URL hyperlinks after removing
        assertFalse(Arrays.stream(removeUrlOwner.getHyperlinks())
            .anyMatch(IHyperlink::isURL));
    }

    @Test
    void removeAllFilePathHyperlinksFromNodePresentation_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Create input DTO
        IdDTO removeInputDTO = new IdDTO(nodePresentation.getID());

        // Get a model element of the node presentation
        IHyperlinkOwner removeFileOwner = (IHyperlinkOwner) nodePresentation.getModel();

        // Check file path hyperlinks before removing
        assertTrue(Arrays.stream(removeFileOwner.getHyperlinks())
            .anyMatch(IHyperlink::isFile));

        // ----------------------------------------
        // Call removeAllFilePathHyperlinksFromNodePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllFilePathHyperlinksFromNodePresentation,
            tool,
            removeInputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.filePathHyperlinks().isEmpty());

        // Check file path hyperlinks after removing
        assertFalse(Arrays.stream(removeFileOwner.getHyperlinks())
            .anyMatch(IHyperlink::isFile));
    }

    @Test
    void removeAllNamedElementHyperlinksFromNodePresentation_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Create input DTO
        IdDTO removeInputDTO = new IdDTO(nodePresentation.getID());

        // Get a model element of the node presentation
        IHyperlinkOwner removeElemOwner = (IHyperlinkOwner) nodePresentation.getModel();

        // Check named element hyperlinks before removing
        assertTrue(Arrays.stream(removeElemOwner.getHyperlinks())
            .anyMatch(IHyperlink::isModel));

        // ----------------------------------------
        // Call removeAllNamedElementHyperlinksFromNodePresentation()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllNamedElementHyperlinksFromNodePresentation,
            tool,
            removeInputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.namedElementHyperlinks().isEmpty());

        // Check named element hyperlinks after removing
        assertFalse(Arrays.stream(removeElemOwner.getHyperlinks())
            .anyMatch(IHyperlink::isModel));
    }
}
