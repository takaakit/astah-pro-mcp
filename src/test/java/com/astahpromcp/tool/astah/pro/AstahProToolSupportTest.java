package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AstahProToolSupportTest {

    private ProjectAccessor projectAccessor;
    private BasicModelEditor basicModelEditor;
    private TransactionSupport transactionSupport;
    private AstahProToolSupport astahProToolSupport;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/AstahProToolSupportTest.asta");
        astahProToolSupport = new AstahProToolSupport(projectAccessor);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void verifyDeleted_ok() throws Exception {
        // Get element
        INamedElement namedElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");

        // Delete element
        transactionSupport.run( () -> {
            basicModelEditor.delete(namedElement);
        });

        // ----------------------------------------
        // Call verifyDeleted()
        // ----------------------------------------
        astahProToolSupport.verifyDeleted(namedElement.getId());

        // Check element
        assertNull(TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo"));
    }

    @Test
    void getPrimitiveTypes_ok() throws Exception {

        // ----------------------------------------
        // Call getPrimitiveTypes()
        // ----------------------------------------
        List<IClass> primitiveTypes = astahProToolSupport.getPrimitiveTypes();

        // Check that the predefined primitive types are returned, sorted by name
        assertFalse(primitiveTypes.isEmpty());
        assertTrue(primitiveTypes.stream().anyMatch(primitiveType -> "int".equals(primitiveType.getName())));
        assertEquals(
            primitiveTypes.stream().map(INamedElement::getName).sorted().toList(),
            primitiveTypes.stream().map(INamedElement::getName).toList());
    }

    @Test
    void findPrimitiveType_ok() throws Exception {
        // Get primitive type
        IClass primitiveType = getPrimitiveTypeByName("int");

        // ----------------------------------------
        // Call findPrimitiveType()
        // ----------------------------------------
        IClass found = astahProToolSupport.findPrimitiveType(primitiveType.getId());

        // Check element
        assertNotNull(found);
        assertEquals("int", found.getName());
    }

    @Test
    void findPrimitiveType_ng() throws Exception {
        // Get element that is not a primitive type
        INamedElement namedElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");

        // ----------------------------------------
        // Call findPrimitiveType()
        // ----------------------------------------
        // Check that an ID that names anything but a primitive type is not resolved
        assertNull(astahProToolSupport.findPrimitiveType(namedElement.getId()));
        assertNull(astahProToolSupport.findPrimitiveType("no-such-id"));
    }

    @Test
    void getClassOrPrimitiveType_ok() throws Exception {
        // Get primitive type
        IClass primitiveType = getPrimitiveTypeByName("int");

        // ----------------------------------------
        // Call getClassOrPrimitiveType()
        // ----------------------------------------
        // A primitive type is reachable only through getPrimitiveTypes(), so this is what getClass() cannot do
        assertEquals("int", astahProToolSupport.getClassOrPrimitiveType(primitiveType.getId()).getName());
    }

    @Test
    void getClass_ng_primitiveType() throws Exception {
        // Get primitive type
        IClass primitiveType = getPrimitiveTypeByName("int");

        // ----------------------------------------
        // Call getClass()
        // ----------------------------------------
        // A primitive type can never be the target of an edit, so every getter but getClassOrPrimitiveType() refuses it
        Exception thrown = assertThrows(IllegalArgumentException.class, () ->
            astahProToolSupport.getClass(primitiveType.getId()));

        // Check exception
        assertTrue(thrown.getMessage().contains("'int' is a primitive type"));
        assertTrue(thrown.getMessage().contains(primitiveType.getId()));
    }

    private IClass getPrimitiveTypeByName(String name) throws Exception {
        for (IClass primitiveType : astahProToolSupport.getPrimitiveTypes()) {
            if (name.equals(primitiveType.getName())) {
                return primitiveType;
            }
        }
        throw new IllegalStateException("The project has no primitive type named " + name + ".");
    }

    @Test
    void verifyDeleted_ng() throws Exception {
        // Get element
        INamedElement namedElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");

        // ----------------------------------------
        // Call verifyDeleted()
        // ----------------------------------------
        Exception thrown = assertThrows(IllegalStateException.class, () ->
            astahProToolSupport.verifyDeleted(namedElement.getId()));

        // Check exception
        assertTrue(thrown.getMessage().contains(namedElement.getId()));

        // Check element
        assertNotNull(TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo"));
    }
}
