package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithStereotypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTaggedValueDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTypeModifierDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.model.IRequirementTable;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElementToolTest {

    private ProjectAccessor projectAccessor;
    private ElementTool tool;
    private Method addStereotype;
    private Method removeStereotype;
    private Method setTypeModifier;
    private Method changeTaggedValue;
    private Method getDiagramsOfElement;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/ElementToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ElementTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // addStereotype() method
        addStereotype = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "addStereotype",
            ElementWithStereotypeDTO.class);

        // removeStereotype() method
        removeStereotype = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "removeStereotype",
            ElementWithStereotypeDTO.class);

        // setTypeModifier() method
        setTypeModifier = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "setTypeModifier",
            ElementWithTypeModifierDTO.class);

        // changeTaggedValue() method
        changeTaggedValue = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "changeTaggedValue",
            ElementWithTaggedValueDTO.class);

        // getDiagramsOfElement() method
        getDiagramsOfElement = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "getDiagramsOfElement",
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void addStereotype_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "testStereotype");

        // Check stereotype before adding
        assertFalse(Arrays.asList(clazz.getStereotypes()).contains("testStereotype"));

        // ----------------------------------------
        // Call addStereotype()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addStereotype,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check stereotype after adding
        assertTrue(Arrays.asList(clazz.getStereotypes()).contains("testStereotype"));
    }

    @Test
    void addStereotype_ng_alreadyHas() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "singleton");

        // ----------------------------------------
        // Call addStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("already has the stereotype 'singleton'"));

        // Check that the stereotype is not duplicated
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void addStereotype_ng_alreadyHasInDifferentCase() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "Singleton");

        // ----------------------------------------
        // Call addStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("already has the stereotype 'singleton'"));
        assertTrue(exception.getCause().getMessage().contains("It matches 'Singleton'"));

        // Check that nothing is added
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void addStereotype_ok_trimmed() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            " testStereotype ");

        // ----------------------------------------
        // Call addStereotype()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addStereotype,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check that the stereotype is added without the surrounding whitespace
        assertTrue(Arrays.asList(clazz.getStereotypes()).contains("testStereotype"));
        assertFalse(Arrays.asList(clazz.getStereotypes()).contains(" testStereotype "));
    }

    @Test
    void addStereotype_ng_blank() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "  ");

        // ----------------------------------------
        // Call addStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("empty or blank"));

        // Check that nothing was added
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void addStereotype_ng_guillemets() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "«entity»");

        // ----------------------------------------
        // Call addStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("without guillemets"));

        // Check that nothing was added
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void removeStereotype_ok() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "singleton");

        // Check stereotype before removing
        assertTrue(Arrays.asList(clazz.getStereotypes()).contains("singleton"));

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeStereotype,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check stereotype after removing
        assertFalse(Arrays.asList(clazz.getStereotypes()).contains("singleton"));
    }

    @Test
    void removeStereotype_ng_notFound() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "nonexistent");

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                removeStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("has no stereotype 'nonexistent'"));
        assertTrue(exception.getCause().getMessage().contains("Existing stereotypes: " + Arrays.asList(stereotypesBefore)));
        assertFalse(exception.getCause().getMessage().contains("Did you mean"));

        // Check that nothing was removed
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void removeStereotype_ng_differentCase() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "Singleton");

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                removeStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("has no stereotype 'Singleton'"));
        assertTrue(exception.getCause().getMessage().contains("Did you mean 'singleton'?"));

        // Check that nothing was removed
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void removeStereotype_ok_trimmed() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            " singleton ");

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeStereotype,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check that the stereotype is removed even though the name was given with the surrounding whitespace
        assertFalse(Arrays.asList(clazz.getStereotypes()).contains("singleton"));
    }

    @Test
    void removeStereotype_ng_blank() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "  ");

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                removeStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("empty or blank"));

        // Check that nothing was removed
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void removeStereotype_ng_guillemets() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String[] stereotypesBefore = clazz.getStereotypes();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "«singleton»");

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                removeStereotype,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("without guillemets"));

        // Check that nothing was removed
        assertArrayEquals(stereotypesBefore, clazz.getStereotypes());
    }

    @Test
    void removeStereotype_ok_duplicated() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // The Astah API lets an element carry the same stereotype more than once
        projectAccessor.getTransactionManager().beginTransaction();
        clazz.addStereotype("singleton");
        projectAccessor.getTransactionManager().endTransaction();

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "singleton");

        // Check stereotype before removing
        assertEquals(2, Arrays.stream(clazz.getStereotypes()).filter("singleton"::equals).count());

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeStereotype,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check that every occurrence is removed
        assertFalse(Arrays.asList(clazz.getStereotypes()).contains("singleton"));
    }

    @Test
    void setTypeModifier_ok() throws Exception {
        // Get element
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "sum");

        // Create input DTO
        ElementWithTypeModifierDTO inputDTO = new ElementWithTypeModifierDTO(
            attribute.getId(),
            "*");

        // Check type modifier before setting
        assertNotEquals("*", attribute.getTypeModifier());

        // ----------------------------------------
        // Call setTypeModifier()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setTypeModifier,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check type modifier after setting
        assertEquals("*", attribute.getTypeModifier());
    }

    @Test
    void changeTaggedValue_ok() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ElementWithTaggedValueDTO inputDTO = new ElementWithTaggedValueDTO(
            clazz.getId(),
            "author",
            "John Doe");

        // Check tagged value before setting
        assertNotEquals("John Doe", clazz.getTaggedValue("author"));

        // ----------------------------------------
        // Call changeTaggedValue()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            changeTaggedValue,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        assertEquals("John Doe", clazz.getTaggedValue("author"));
    }

    @Test
    void changeTaggedValue_ng_missingKey() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        String authorBefore = clazz.getTaggedValue("author");

        // Create input DTO
        ElementWithTaggedValueDTO inputDTO = new ElementWithTaggedValueDTO(
            clazz.getId(),
            "noSuchKey",
            "John Doe");

        // ----------------------------------------
        // Call changeTaggedValue()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                changeTaggedValue,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("'noSuchKey'"));
        assertTrue(exception.getCause().getMessage().contains("author"));
        assertTrue(exception.getCause().getMessage().contains("create_tagged_val"));

        // Check that nothing was changed or created
        assertEquals(authorBefore, clazz.getTaggedValue("author"));
        assertEquals("", clazz.getTaggedValue("noSuchKey"));
    }

    @Test
    void changeTaggedValue_ng_caseMismatch() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ElementWithTaggedValueDTO inputDTO = new ElementWithTaggedValueDTO(
            clazz.getId(),
            "Author",
            "John Doe");

        // ----------------------------------------
        // Call changeTaggedValue()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                changeTaggedValue,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Did you mean 'author'?"));

        // Check tagged value is not changed
        assertNotEquals("John Doe", clazz.getTaggedValue("author"));
    }

    @Test
    void changeTaggedValue_ng_astahInternalKey() throws Exception {
        // Create requirement, which carries the Astah-internal tagged value jude.require.kind.requirement=true
        IRequirement requirement = new TransactionSupport(projectAccessor.getTransactionManager()).call(() ->
            projectAccessor.getModelEditorFactory().getBasicModelEditor().createRequirement(
                projectAccessor.getProject(),
                "InternalKeyRequirement"));
        assertEquals("true", requirement.getTaggedValue("jude.require.kind.requirement"));

        // Create input DTO
        ElementWithTaggedValueDTO inputDTO = new ElementWithTaggedValueDTO(
            requirement.getId(),
            "jude.require.kind.requirement",
            "false");

        // ----------------------------------------
        // Call changeTaggedValue()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                changeTaggedValue,
                tool,
                inputDTO,
                ElementDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("'jude.require.kind.requirement'"));
        assertTrue(exception.getCause().getMessage().contains("managed by Astah"));

        // Check tagged value is not changed
        assertEquals("true", requirement.getTaggedValue("jude.require.kind.requirement"));
    }

    @Test
    void getDiagramsOfElement_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(clazz.getId());

        // ----------------------------------------
        // Call getDiagramsOfElement()
        // ----------------------------------------
        NameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getDiagramsOfElement,
            tool,
            inputDTO,
            NameIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(outputDTO.value());
        assertEquals(22, outputDTO.value().size());
    }

    @Test
    void getDiagramsOfElement_ok_rowOfRequirementTable() throws Exception {
        // A requirement table lists none of its rows among its presentations, and the API cannot create one, so mocks stand in for it
        IRequirementTable requirementTable = mock(IRequirementTable.class);
        when(requirementTable.getId()).thenReturn("requirementTable");
        when(requirementTable.getName()).thenReturn("Requirement Table0");
        when(requirementTable.getPresentations()).thenReturn(new IPresentation[0]);

        IRequirement requirement = mock(IRequirement.class);
        IPresentation row = mock(IPresentation.class);
        when(row.getDiagram()).thenReturn(requirementTable);
        when(row.getModel()).thenReturn(requirement);
        when(requirement.getPresentations()).thenReturn(new IPresentation[] { row });

        ProjectAccessor mockedProjectAccessor = mock(ProjectAccessor.class);
        when(mockedProjectAccessor.findElements(IDiagram.class)).thenReturn(new INamedElement[] { requirementTable });
        AstahProToolSupport mockedAstahProToolSupport = mock(AstahProToolSupport.class);
        when(mockedAstahProToolSupport.getElement("requirement")).thenReturn(requirement);

        ElementTool mockedTool = new ElementTool(
            mockedProjectAccessor,
            mock(TransactionSupport.class),
            mockedAstahProToolSupport);

        // ----------------------------------------
        // Call getDiagramsOfElement()
        // ----------------------------------------
        NameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getDiagramsOfElement,
            mockedTool,
            new IdDTO("requirement"),
            NameIdTypeListDTO.class);

        // Check that the requirement table is returned although it does not list the row
        assertNotNull(outputDTO);
        assertEquals(List.of("requirementTable"), outputDTO.value().stream().map(NameIdTypeDTO::id).toList());
    }
}
