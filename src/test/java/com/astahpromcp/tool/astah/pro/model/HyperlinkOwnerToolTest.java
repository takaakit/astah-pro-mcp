package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
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
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IHyperlink;
import com.change_vision.jude.api.inf.model.IHyperlinkOwner;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/HyperlinkOwnerToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new HyperlinkOwnerTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // addUrlHyperlinkToNamedElement() method
        addUrlHyperlinkToNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addUrlHyperlinkToNamedElement",
            NamedElementWithUrlHyperlinkDTO.class);

        // addFilePathHyperlinkToNamedElement() method
        addFilePathHyperlinkToNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addFilePathHyperlinkToNamedElement",
            NamedElementWithFilePathHyperlinkDTO.class);

        // addNamedElementHyperlinkToNamedElement() method
        addNamedElementHyperlinkToNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addNamedElementHyperlinkToNamedElement",
            NamedElementWithNamedElementHyperlinkDTO.class);

        // removeAllUrlHyperlinksFromNamedElement() method
        removeAllUrlHyperlinksFromNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllUrlHyperlinksFromNamedElement",
            IdDTO.class);

        // removeAllFilePathHyperlinksFromNamedElement() method
        removeAllFilePathHyperlinksFromNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllFilePathHyperlinksFromNamedElement",
            IdDTO.class);

        // removeAllNamedElementHyperlinksFromNamedElement() method
        removeAllNamedElementHyperlinksFromNamedElement = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllNamedElementHyperlinksFromNamedElement",
            IdDTO.class);

        // addUrlHyperlinkToNodePresentation() method
        addUrlHyperlinkToNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addUrlHyperlinkToNodePresentation",
            NodePresentationWithUrlHyperlinkDTO.class);

        // addFilePathHyperlinkToNodePresentation() method
        addFilePathHyperlinkToNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addFilePathHyperlinkToNodePresentation",
            NodePresentationWithFilePathHyperlinkDTO.class);

        // addNamedElementHyperlinkToNodePresentation() method
        addNamedElementHyperlinkToNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "addNamedElementHyperlinkToNodePresentation",
            NodePresentationWithNamedElementHyperlinkDTO.class);

        // removeAllUrlHyperlinksFromNodePresentation() method
        removeAllUrlHyperlinksFromNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllUrlHyperlinksFromNodePresentation",
            IdDTO.class);

        // removeAllFilePathHyperlinksFromNodePresentation() method
        removeAllFilePathHyperlinksFromNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllFilePathHyperlinksFromNodePresentation",
            IdDTO.class);

        // removeAllNamedElementHyperlinksFromNodePresentation() method
        removeAllNamedElementHyperlinksFromNodePresentation = TestSupport.getAccessibleMethod(
            HyperlinkOwnerTool.class,
            "removeAllNamedElementHyperlinksFromNodePresentation",
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
        // Call addUrlHyperlinkToNamedElement()
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
        // Call addUrlHyperlinkToNamedElement()
        // ----------------------------------------
        assertThrows(Exception.class, () ->
            TestSupport.instance().invokeToolMethodReturningDto(
                addUrlHyperlinkToNamedElement,
                tool,
                inputDTO,
                NamedElementDTO.class));
    }

    @Test
    void addUrlHyperlinkToNamedElement_ok_withFileHyperlinkInvalidAsPath() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // A file hyperlink whose name Windows forbids in a path, as one made on another OS can carry
        projectAccessor.getTransactionManager().beginTransaction();
        namedElement.createFileHyperlink("report:v2.txt", "C:\\shared", "Made on another OS");
        projectAccessor.getTransactionManager().endTransaction();

        long urlHyperlinkCountBefore = Arrays.stream(namedElement.getHyperlinks()).filter(IHyperlink::isURL).count();

        // Create input DTO
        NamedElementWithUrlHyperlinkDTO inputDTO = new NamedElementWithUrlHyperlinkDTO(
            namedElement.getId(),
            "https://example.com/test",
            "Test comment");

        // ----------------------------------------
        // Call addUrlHyperlinkToNamedElement()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addUrlHyperlinkToNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.filePathHyperlinks().stream()
            .anyMatch(h -> h.filePath().startsWith("C:\\shared") && h.filePath().endsWith("report:v2.txt")));

        // Check URL hyperlinks after adding
        assertEquals(urlHyperlinkCountBefore + 1,
            Arrays.stream(namedElement.getHyperlinks()).filter(IHyperlink::isURL).count());
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
        // Call addFilePathHyperlinkToNamedElement()
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
    void addFilePathHyperlinkToNamedElement_ng_noFileName() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        long fileHyperlinkCountBefore = Arrays.stream(namedElement.getHyperlinks()).filter(IHyperlink::isFile).count();

        // Paths that name no file, which Astah would accept as a hyperlink with an empty file name
        for (String filePath : new String[] { null, "", ".", "a/.." }) {
            // Create input DTO
            NamedElementWithFilePathHyperlinkDTO inputDTO = new NamedElementWithFilePathHyperlinkDTO(
                namedElement.getId(),
                filePath,
                "File link comment");

            // ----------------------------------------
            // Call addFilePathHyperlinkToNamedElement()
            // ----------------------------------------
            InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () ->
                TestSupport.instance().invokeToolMethodReturningDto(
                    addFilePathHyperlinkToNamedElement,
                    tool,
                    inputDTO,
                    NamedElementDTO.class));

            // Check that the error names the argument and quotes its value, so that an empty one can be told apart
            IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
            assertTrue(cause.getMessage().startsWith("filePath"), cause.getMessage());
            assertTrue(cause.getMessage().endsWith(filePath == null ? "null" : "\"" + filePath + "\""), cause.getMessage());
        }

        // Check that no file path hyperlink was added
        assertEquals(fileHyperlinkCountBefore,
            Arrays.stream(namedElement.getHyperlinks()).filter(IHyperlink::isFile).count());
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
        // Call addNamedElementHyperlinkToNamedElement()
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
        // Call removeAllUrlHyperlinksFromNamedElement()
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
        // Call removeAllFilePathHyperlinksFromNamedElement()
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
        // Call removeAllNamedElementHyperlinksFromNamedElement()
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
    void removeAllUrlHyperlinksFromNamedElement_ok_noneToRemoveDoesNotModifyProject() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Baz");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());

        // Check that there is no URL hyperlink to remove and the project is not modified before removing
        assertFalse(Arrays.stream(namedElement.getHyperlinks()).anyMatch(IHyperlink::isURL));
        assertFalse(projectAccessor.isProjectModified());

        // ----------------------------------------
        // Call removeAllUrlHyperlinksFromNamedElement()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllUrlHyperlinksFromNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.urlHyperlinks().isEmpty());

        // Check that removing nothing does not mark the project modified
        assertFalse(projectAccessor.isProjectModified());
    }

    @Test
    void removeAllFilePathHyperlinksFromNamedElement_ok_noneToRemoveDoesNotModifyProject() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Baz");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());

        // Check that there is no file path hyperlink to remove and the project is not modified before removing
        assertFalse(Arrays.stream(namedElement.getHyperlinks()).anyMatch(IHyperlink::isFile));
        assertFalse(projectAccessor.isProjectModified());

        // ----------------------------------------
        // Call removeAllFilePathHyperlinksFromNamedElement()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllFilePathHyperlinksFromNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.filePathHyperlinks().isEmpty());

        // Check that removing nothing does not mark the project modified
        assertFalse(projectAccessor.isProjectModified());
    }

    @Test
    void removeAllNamedElementHyperlinksFromNamedElement_ok_noneToRemoveDoesNotModifyProject() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Baz");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());

        // Check that there is no named element hyperlink to remove and the project is not modified before removing
        assertFalse(Arrays.stream(namedElement.getHyperlinks()).anyMatch(IHyperlink::isModel));
        assertFalse(projectAccessor.isProjectModified());

        // ----------------------------------------
        // Call removeAllNamedElementHyperlinksFromNamedElement()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeAllNamedElementHyperlinksFromNamedElement,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.namedElementHyperlinks().isEmpty());

        // Check that removing nothing does not mark the project modified
        assertFalse(projectAccessor.isProjectModified());
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
    void addFilePathHyperlinkToNodePresentation_ng_noFileName() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");

        // Get a model element of the node presentation
        IHyperlinkOwner fileHyperlinkOwner = (IHyperlinkOwner) nodePresentation.getModel();

        long fileHyperlinkCountBefore = Arrays.stream(fileHyperlinkOwner.getHyperlinks()).filter(IHyperlink::isFile).count();

        // Paths that name no file, which Astah would accept as a hyperlink with an empty file name
        for (String filePath : new String[] { null, "", ".", "a/.." }) {
            // Create input DTO
            NodePresentationWithFilePathHyperlinkDTO inputDTO = new NodePresentationWithFilePathHyperlinkDTO(
                nodePresentation.getID(),
                filePath,
                "Node file link comment");

            // ----------------------------------------
            // Call addFilePathHyperlinkToNodePresentation()
            // ----------------------------------------
            InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () ->
                TestSupport.instance().invokeToolMethodReturningDto(
                    addFilePathHyperlinkToNodePresentation,
                    tool,
                    inputDTO,
                    NodePresentationDTO.class));

            // Check that the error names the argument and quotes its value, so that an empty one can be told apart
            IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
            assertTrue(cause.getMessage().startsWith("filePath"), cause.getMessage());
            assertTrue(cause.getMessage().endsWith(filePath == null ? "null" : "\"" + filePath + "\""), cause.getMessage());
        }

        // Check that no file path hyperlink was added
        assertEquals(fileHyperlinkCountBefore,
            Arrays.stream(fileHyperlinkOwner.getHyperlinks()).filter(IHyperlink::isFile).count());
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

    @Test
    void removeAllUrlHyperlinksFromNodePresentation_ok_noneToRemoveDoesNotModifyProject() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Frame",
            "stm Statemachine Diagram0");

        // Create input DTO
        IdDTO removeInputDTO = new IdDTO(nodePresentation.getID());

        // The frame draws no model element, so the node presentation itself owns the hyperlinks
        IHyperlinkOwner removeUrlOwner = (IHyperlinkOwner) nodePresentation;

        // Check that there is no URL hyperlink to remove and the project is not modified before removing
        assertFalse(Arrays.stream(removeUrlOwner.getHyperlinks()).anyMatch(IHyperlink::isURL));
        assertFalse(projectAccessor.isProjectModified());

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

        // Check that removing nothing does not mark the project modified
        assertFalse(projectAccessor.isProjectModified());
    }

    @Test
    void removeAllFilePathHyperlinksFromNodePresentation_ok_noneToRemoveDoesNotModifyProject() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Frame",
            "stm Statemachine Diagram0");

        // Create input DTO
        IdDTO removeInputDTO = new IdDTO(nodePresentation.getID());

        // The frame draws no model element, so the node presentation itself owns the hyperlinks
        IHyperlinkOwner removeFileOwner = (IHyperlinkOwner) nodePresentation;

        // Check that there is no file path hyperlink to remove and the project is not modified before removing
        assertFalse(Arrays.stream(removeFileOwner.getHyperlinks()).anyMatch(IHyperlink::isFile));
        assertFalse(projectAccessor.isProjectModified());

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

        // Check that removing nothing does not mark the project modified
        assertFalse(projectAccessor.isProjectModified());
    }

    @Test
    void removeAllNamedElementHyperlinksFromNodePresentation_ok_noneToRemoveDoesNotModifyProject() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Frame",
            "stm Statemachine Diagram0");

        // Create input DTO
        IdDTO removeInputDTO = new IdDTO(nodePresentation.getID());

        // The frame draws no model element, so the node presentation itself owns the hyperlinks
        IHyperlinkOwner removeElemOwner = (IHyperlinkOwner) nodePresentation;

        // Check that there is no named element hyperlink to remove and the project is not modified before removing
        assertFalse(Arrays.stream(removeElemOwner.getHyperlinks()).anyMatch(IHyperlink::isModel));
        assertFalse(projectAccessor.isProjectModified());

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

        // Check that removing nothing does not mark the project modified
        assertFalse(projectAccessor.isProjectModified());
    }
}
