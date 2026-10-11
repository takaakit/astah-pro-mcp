package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithActiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithInvariantDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithLeafDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClassToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private ClassTool tool;
    private Method getInfo;
    private Method setAbstract;
    private Method setActive;
    private Method setLeaf;
    private Method addInvariant;
    private Method removeInvariant;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        BasicModelEditor basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/ClassToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ClassTool(
            basicModelEditor,
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            new SystemPropertySupport());

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "getInfo",
            IdDTO.class);

        // setAbstract() method
        setAbstract = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "setAbstract",
            ClassWithAbstractDTO.class);

        // setActive() method
        setActive = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "setActive",
            ClassWithActiveDTO.class);

        // setLeaf() method
        setLeaf = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "setLeaf",
            ClassWithLeafDTO.class);

        // addInvariant() method
        addInvariant = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "addInvariant",
            ClassWithInvariantDTO.class);

        // removeInvariant() method
        removeInvariant = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "removeInvariant",
            ClassWithInvariantDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(clazz.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getInfo_ok_generalizationsHoldThoseWhoseSubTypeIsThisClass() throws Exception {
        IModel project = projectAccessor.getProject();
        BasicModelEditor basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();

        IClass subClass = transactionSupport.call(() -> basicModelEditor.createClass(project, "SubClass"));
        IClass superClass = transactionSupport.call(() -> basicModelEditor.createClass(project, "SuperClass"));
        transactionSupport.call(() -> basicModelEditor.createGeneralization(subClass, superClass, ""));

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ClassDTO subClassDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(subClass.getId()),
            ClassDTO.class);
        ClassDTO superClassDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(superClass.getId()),
            ClassDTO.class);

        // Check output DTO
        assertEquals(List.of(superClass.getId()), subClassDTO.generalizations().stream().map(generalization -> generalization.superType().id()).toList());
        assertTrue(subClassDTO.specializations().isEmpty());
        assertEquals(List.of(subClass.getId()), superClassDTO.specializations().stream().map(generalization -> generalization.subType().id()).toList());
        assertTrue(superClassDTO.generalizations().isEmpty());
    }

    @Test
    void setAbstract_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ClassWithAbstractDTO inputDTO = new ClassWithAbstractDTO(
            clazz.getId(),
            true);

        // Check abstract before setting
        assertFalse(clazz.isAbstract());

        // ----------------------------------------
        // Call setAbstract()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setAbstract,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check abstract after setting
        assertTrue(clazz.isAbstract());
    }

    @Test
    void setActive_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ClassWithActiveDTO inputDTO = new ClassWithActiveDTO(
            clazz.getId(),
            true);

        // Check active before setting
        assertFalse(clazz.isActive());

        // ----------------------------------------
        // Call setActive()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setActive,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check active after setting
        assertTrue(clazz.isActive());
    }

    @Test
    void setLeaf_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ClassWithLeafDTO inputDTO = new ClassWithLeafDTO(
            clazz.getId(),
            true);

        // Check leaf before setting
        assertFalse(clazz.isLeaf());

        // ----------------------------------------
        // Call setLeaf()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setLeaf,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check leaf after setting
        assertTrue(clazz.isLeaf());
    }

    @Test
    void addInvariant_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ClassWithInvariantDTO inputDTO = new ClassWithInvariantDTO(
            clazz.getId(),
            "x > 0");

        // Check invariants before adding
        int beforeCount = clazz.getConstraints().length;

        // ----------------------------------------
        // Call addInvariant()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addInvariant,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check invariants after adding
        IConstraint[] constraints = clazz.getConstraints();
        assertEquals(beforeCount + 1, constraints.length);
        assertEquals("x > 0", constraints[constraints.length - 1].getName());
    }

    @Test
    void removeInvariant_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Add an invariant first
        ClassWithInvariantDTO addDTO = new ClassWithInvariantDTO(
            clazz.getId(),
            "x > 0");
        TestSupport.instance().invokeToolMethodReturningDto(
            addInvariant,
            tool,
            addDTO,
            ClassDTO.class);

        // Check invariants before removing
        int beforeCount = clazz.getConstraints().length;
        assertTrue(beforeCount > 0);

        // Create input DTO
        ClassWithInvariantDTO inputDTO = new ClassWithInvariantDTO(
            clazz.getId(),
            "x > 0");

        // ----------------------------------------
        // Call removeInvariant()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeInvariant,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check invariants after removing
        IConstraint[] constraints = clazz.getConstraints();
        assertEquals(beforeCount - 1, constraints.length);
        for (IConstraint constraint : constraints) {
            assertNotEquals("x > 0", constraint.getName());
        }
    }

    @Test
    void addInvariant_ok_stripsSurroundingWhitespace() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // ----------------------------------------
        // Call addInvariant()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            addInvariant,
            tool,
            new ClassWithInvariantDTO(clazz.getId(), "  x > 0\n  y > 0 \n"),
            ClassDTO.class);

        // Check that the surrounding whitespace is removed and the inner whitespace is kept
        IConstraint[] constraints = clazz.getConstraints();
        assertEquals("x > 0\n  y > 0", constraints[constraints.length - 1].getSpecification());
    }

    @Test
    void addInvariant_ng_alreadyHas() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Add an invariant first
        TestSupport.instance().invokeToolMethodReturningDto(
            addInvariant,
            tool,
            new ClassWithInvariantDTO(clazz.getId(), "x > 0"),
            ClassDTO.class);
        int beforeCount = clazz.getConstraints().length;

        // ----------------------------------------
        // Call addInvariant()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addInvariant,
                tool,
                new ClassWithInvariantDTO(clazz.getId(), " x > 0 "),
                ClassDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("already has the invariant 'x > 0'"));
        assertTrue(exception.getCause().getMessage().contains("Existing invariants: ["));

        // Check that the invariant is not duplicated
        assertEquals(beforeCount, clazz.getConstraints().length);
    }

    @Test
    void addInvariant_ng_blank() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        int beforeCount = clazz.getConstraints().length;

        // ----------------------------------------
        // Call addInvariant()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addInvariant,
                tool,
                new ClassWithInvariantDTO(clazz.getId(), "   "),
                ClassDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("The invariant must not be empty or blank.", exception.getCause().getMessage());

        // Check that nothing was added
        assertEquals(beforeCount, clazz.getConstraints().length);
    }

    @Test
    void removeInvariant_ok_removesEveryDuplicate() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        int beforeCount = clazz.getConstraints().length;

        // Add duplicates directly, as they may have been added before the tool rejected them
        BasicModelEditor basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        transactionSupport.run(() -> {
            basicModelEditor.createConstraint(clazz, "x > 0");
            basicModelEditor.createConstraint(clazz, "x > 0");
        });
        assertEquals(beforeCount + 2, clazz.getConstraints().length);

        // ----------------------------------------
        // Call removeInvariant()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            removeInvariant,
            tool,
            new ClassWithInvariantDTO(clazz.getId(), " x > 0\n"),
            ClassDTO.class);

        // Check that both duplicates are removed
        assertEquals(beforeCount, clazz.getConstraints().length);
        for (IConstraint constraint : clazz.getConstraints()) {
            assertNotEquals("x > 0", constraint.getSpecification());
        }
    }

    @Test
    void removeInvariant_ng_notFound() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Add an invariant first
        TestSupport.instance().invokeToolMethodReturningDto(
            addInvariant,
            tool,
            new ClassWithInvariantDTO(clazz.getId(), "x >= 0"),
            ClassDTO.class);
        int beforeCount = clazz.getConstraints().length;

        // ----------------------------------------
        // Call removeInvariant()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                removeInvariant,
                tool,
                new ClassWithInvariantDTO(clazz.getId(), "x > 0"),
                ClassDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("has no invariant 'x > 0'"));
        assertTrue(exception.getCause().getMessage().contains("x >= 0"));

        // Check that nothing was removed
        assertEquals(beforeCount, clazz.getConstraints().length);
    }
}
