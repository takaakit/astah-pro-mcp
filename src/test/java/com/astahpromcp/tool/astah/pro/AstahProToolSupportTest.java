package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDataStore;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void getClass_ng_incorrectId() throws Exception {

        // ----------------------------------------
        // Call getClass()
        // ----------------------------------------
        // An ID that names nothing is the caller's mistake, so it is reported as an invalid argument
        Exception thrown = assertThrows(IllegalArgumentException.class, () ->
            astahProToolSupport.getClass("no-such-id"));

        // Check exception
        assertEquals("Failed to get class (ID: no-such-id) due to an incorrect ID.", thrown.getMessage());
    }

    @Test
    void getSequenceDiagram_ng_classDiagram() throws Exception {
        // Create a class diagram
        IClassDiagram classDiagram = transactionSupport.call(() ->
            projectAccessor.getDiagramEditorFactory().getClassDiagramEditor().createClassDiagram(
                projectAccessor.getProject(),
                "Not Sequence Diagram"));

        // ----------------------------------------
        // Call getSequenceDiagram()
        // ----------------------------------------
        Exception thrown = assertThrows(IllegalArgumentException.class, () ->
            astahProToolSupport.getSequenceDiagram(classDiagram.getId()));

        // Check exception
        assertTrue(thrown.getMessage().contains("due to an incorrect type (actual type: ClassDiagram)"));
        assertTrue(thrown.getMessage().contains(classDiagram.getId()));
    }

    @Test
    void getSequenceDiagram_ng_presentation() throws Exception {
        // Create a class drawn on a class diagram
        INodePresentation classNode = transactionSupport.call(() -> {
            IClass clazz = basicModelEditor.createClass(projectAccessor.getProject(), "Drawn");
            ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
            classDiagramEditor.setDiagram(classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Class Diagram With Node"));
            return classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(0, 0));
        });

        // ----------------------------------------
        // Call getSequenceDiagram()
        // ----------------------------------------
        Exception thrown = assertThrows(IllegalArgumentException.class, () ->
            astahProToolSupport.getSequenceDiagram(classNode.getID()));

        // Check exception
        assertTrue(thrown.getMessage().contains("due to an incorrect type (actual type: Class presentation)"));
        assertTrue(thrown.getMessage().contains(classNode.getID()));
    }

    @Test
    void requireRrggbbColor_ok() throws Exception {
        // ----------------------------------------
        // Call requireRrggbbColor()
        // ----------------------------------------
        // Check that a color in #RRGGBB is returned as it is, in either case
        assertEquals("#FFFFFF", AstahProToolSupport.requireRrggbbColor("#FFFFFF"));
        assertEquals("#00ff7F", AstahProToolSupport.requireRrggbbColor("#00ff7F"));
    }

    @Test
    void requireRrggbbColor_ng() throws Exception {
        for (String color : Arrays.asList("#FFF", "#00FF0080", "red", "FF0000", "0xFF0000", "16711680", "0777", " #FFFFFF", "#FFFFFF ", "#GGGGGG", "", null)) {
            // ----------------------------------------
            // Call requireRrggbbColor()
            // ----------------------------------------
            Exception thrown = assertThrows(IllegalArgumentException.class, () ->
                AstahProToolSupport.requireRrggbbColor(color), "requireRrggbbColor() accepted " + color);

            // Check exception
            assertTrue(thrown.getMessage().contains("#RRGGBB"));
            assertTrue(thrown.getMessage().contains("'" + color + "'"));
        }
    }

    @Test
    void requireMultiplicityBound_ok() throws Exception {
        for (String bound : Arrays.asList("0", "10", "*", "MIN", "MAX", "max_1", "")) {
            // ----------------------------------------
            // Call requireMultiplicityBound()
            // ----------------------------------------
            // Check that a non-negative integer, '*', a name or an empty string is returned as it is
            assertEquals(bound, AstahProToolSupport.requireMultiplicityBound(bound));
        }
    }

    @Test
    void requireMultiplicityBound_ng() throws Exception {
        for (String bound : Arrays.asList("5..2", "-1..3", "-5", "1..", "..5", "0 .. 1", "0..1", "1.5", " 1 ", "1,3", null)) {
            // ----------------------------------------
            // Call requireMultiplicityBound()
            // ----------------------------------------
            Exception thrown = assertThrows(IllegalArgumentException.class, () ->
                AstahProToolSupport.requireMultiplicityBound(bound), "requireMultiplicityBound() accepted " + bound);

            // Check exception
            assertTrue(thrown.getMessage().contains("Invalid multiplicity bound"));
            assertTrue(thrown.getMessage().contains("'" + bound + "'"));
        }
    }

    @Test
    void getPresentationsOnDiagram_ok() throws Exception {
        // Create a class drawn on a class diagram
        ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        IClassDiagram classDiagram = transactionSupport.call(() ->
            classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Diagram With Node"));
        INodePresentation classNode = transactionSupport.call(() -> {
            classDiagramEditor.setDiagram(classDiagram);
            return classDiagramEditor.createNodePresentation(
                basicModelEditor.createClass(projectAccessor.getProject(), "OnDiagram"),
                new Point2D.Double(0, 0));
        });

        // ----------------------------------------
        // Call getPresentationsOnDiagram()
        // ----------------------------------------
        List<IPresentation> presentations = AstahProToolSupport.getPresentationsOnDiagram(classDiagram);

        // Check that nothing is dropped from a diagram that is not shown on a CRUD
        assertTrue(presentations.contains(classNode));
        assertEquals(List.of(classDiagram.getPresentations()), presentations);
    }

    @Test
    void getPresentationsOnDiagram_ok_headerCellOfCrud() throws Exception {
        // A diagram shown on a CRUD returns the header cell drawn on the CRUD as well, and the API cannot create a CRUD, so mocks stand in for it
        IDiagram useCaseDiagram = mock(IDiagram.class);
        IDiagram crud = mock(IDiagram.class);

        IPresentation useCaseNode = mock(IPresentation.class);
        when(useCaseNode.getDiagram()).thenReturn(useCaseDiagram);

        IPresentation headerCell = mock(IPresentation.class);
        when(headerCell.getDiagram()).thenReturn(crud);

        when(useCaseDiagram.getPresentations()).thenReturn(new IPresentation[] { useCaseNode, headerCell });
        when(crud.getPresentations()).thenReturn(new IPresentation[] { headerCell });

        // ----------------------------------------
        // Call getPresentationsOnDiagram()
        // ----------------------------------------
        // Check that the header cell belongs to the CRUD alone
        assertEquals(List.of(useCaseNode), AstahProToolSupport.getPresentationsOnDiagram(useCaseDiagram));
        assertEquals(List.of(headerCell), AstahProToolSupport.getPresentationsOnDiagram(crud));
    }

    @Test
    void getCorrespondingPresentations_ok() throws Exception {
        // Create a class drawn on a class diagram
        ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        IClassDiagram classDiagram = transactionSupport.call(() ->
            classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Diagram With Drawn Class"));
        IClass clazz = transactionSupport.call(() ->
            basicModelEditor.createClass(projectAccessor.getProject(), "Drawn"));
        INodePresentation classNode = transactionSupport.call(() -> {
            classDiagramEditor.setDiagram(classDiagram);
            return classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(0, 0));
        });

        // ----------------------------------------
        // Call getCorrespondingPresentations()
        // ----------------------------------------
        // Check that a class corresponds to its node, and a diagram not to what is drawn on it
        assertEquals(List.of(classNode), AstahProToolSupport.getCorrespondingPresentations(clazz));
        assertEquals(List.of(), AstahProToolSupport.getCorrespondingPresentations(classDiagram));
    }

    @Test
    void getCorrespondingPresentations_ok_headerCellOfCrud() throws Exception {
        // A diagram shown on a CRUD returns the header cell drawn on the CRUD as well, and the API cannot create a CRUD, so mocks stand in for it
        IDiagram useCaseDiagram = mock(IDiagram.class);
        IDiagram crud = mock(IDiagram.class);

        IPresentation useCaseNode = mock(IPresentation.class);
        when(useCaseNode.getDiagram()).thenReturn(useCaseDiagram);

        IPresentation headerCell = mock(IPresentation.class);
        when(headerCell.getDiagram()).thenReturn(crud);
        when(headerCell.getModel()).thenReturn(useCaseDiagram);

        when(useCaseDiagram.getPresentations()).thenReturn(new IPresentation[] { useCaseNode, headerCell });
        when(crud.getPresentations()).thenReturn(new IPresentation[] { headerCell });

        // ----------------------------------------
        // Call getCorrespondingPresentations()
        // ----------------------------------------
        // Check that a diagram corresponds to the header cell that shows it, and the CRUD to nothing
        assertEquals(List.of(headerCell), AstahProToolSupport.getCorrespondingPresentations(useCaseDiagram));
        assertEquals(List.of(), AstahProToolSupport.getCorrespondingPresentations(crud));
    }

    @Test
    void getCorrespondingPresentations_ok_cellWithoutModel() throws Exception {
        // A value cell of a CRUD corresponds to a dependency although its model is null, so the presentations of an element other than a diagram are not filtered by their model
        IDependency dependency = mock(IDependency.class);
        IPresentation valueCell = mock(IPresentation.class);
        when(valueCell.getModel()).thenReturn(null);
        when(dependency.getPresentations()).thenReturn(new IPresentation[] { valueCell });

        // ----------------------------------------
        // Call getCorrespondingPresentations()
        // ----------------------------------------
        assertEquals(List.of(valueCell), AstahProToolSupport.getCorrespondingPresentations(dependency));
    }

    @Test
    void getRenderedInDiagrams_ok() throws Exception {
        // Create a class drawn twice on one class diagram and once on another
        ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        IClassDiagram firstDiagram = transactionSupport.call(() ->
            classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "First Diagram"));
        IClassDiagram secondDiagram = transactionSupport.call(() ->
            classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Second Diagram"));
        IClass clazz = transactionSupport.call(() ->
            basicModelEditor.createClass(projectAccessor.getProject(), "DrawnThrice"));
        transactionSupport.run(() -> {
            classDiagramEditor.setDiagram(firstDiagram);
            classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(0, 0));
            classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(200, 0));
            classDiagramEditor.setDiagram(secondDiagram);
            classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(0, 0));
        });

        // Check that the class is drawn three times
        assertEquals(3, clazz.getPresentations().length);

        // ----------------------------------------
        // Call getRenderedInDiagrams()
        // ----------------------------------------
        // Check that each diagram is listed once, in the order the class was drawn on them, and that a diagram is not drawn on itself
        assertEquals(
            List.of(firstDiagram.getId(), secondDiagram.getId()),
            AstahProToolSupport.getRenderedInDiagrams(clazz).stream().map(IDiagram::getId).toList());
        assertEquals(List.of(), AstahProToolSupport.getRenderedInDiagrams(firstDiagram));
    }

    @Test
    void getRenderedInDiagrams_ok_cellWithoutModel() throws Exception {
        // A dependency shown on a CRUD corresponds to a value cell whose model is null, and the API cannot create a CRUD, so mocks stand in for it
        IDiagram crud = mock(IDiagram.class);
        when(crud.getId()).thenReturn("crud");

        IPresentation valueCell = mock(IPresentation.class);
        when(valueCell.getDiagram()).thenReturn(crud);
        when(valueCell.getModel()).thenReturn(null);

        IDependency dependency = mock(IDependency.class);
        when(dependency.getPresentations()).thenReturn(new IPresentation[] { valueCell });

        // ----------------------------------------
        // Call getRenderedInDiagrams()
        // ----------------------------------------
        // Check that the CRUD counts although the cell has no model
        assertEquals(List.of(crud), AstahProToolSupport.getRenderedInDiagrams(dependency));
    }

    @Test
    void getRenderedInDiagrams_ok_modelNotEqual() throws Exception {
        // The presentations of a data store hand back as their model another object with the same ID, which is not equal() to the data store, so mocks stand in for it
        IDataStore dataStore = mock(IDataStore.class);
        IDataStore sameDataStoreAsAnotherObject = mock(IDataStore.class);

        IDiagram firstDiagram = mock(IDiagram.class);
        when(firstDiagram.getId()).thenReturn("first");
        IDiagram secondDiagram = mock(IDiagram.class);
        when(secondDiagram.getId()).thenReturn("second");

        IPresentation firstNode = mock(IPresentation.class);
        when(firstNode.getDiagram()).thenReturn(firstDiagram);
        when(firstNode.getModel()).thenReturn(sameDataStoreAsAnotherObject);
        IPresentation secondNode = mock(IPresentation.class);
        when(secondNode.getDiagram()).thenReturn(secondDiagram);
        when(secondNode.getModel()).thenReturn(sameDataStoreAsAnotherObject);

        when(dataStore.getPresentations()).thenReturn(new IPresentation[] { firstNode, secondNode });

        // ----------------------------------------
        // Call getRenderedInDiagrams()
        // ----------------------------------------
        // Check that the diagrams are found without comparing the models
        assertEquals(List.of(firstDiagram, secondDiagram), AstahProToolSupport.getRenderedInDiagrams(dataStore));
    }

    @Test
    void getRenderedInDiagrams_ok_headerCellOfCrud() throws Exception {
        // A diagram shown on a CRUD is drawn on the CRUD in a header cell, and the API cannot create a CRUD, so mocks stand in for it
        IDiagram useCaseDiagram = mock(IDiagram.class);
        when(useCaseDiagram.getId()).thenReturn("useCaseDiagram");
        IDiagram crud = mock(IDiagram.class);
        when(crud.getId()).thenReturn("crud");

        IPresentation useCaseNode = mock(IPresentation.class);
        when(useCaseNode.getDiagram()).thenReturn(useCaseDiagram);

        IPresentation headerCell = mock(IPresentation.class);
        when(headerCell.getDiagram()).thenReturn(crud);
        when(headerCell.getModel()).thenReturn(useCaseDiagram);

        when(useCaseDiagram.getPresentations()).thenReturn(new IPresentation[] { useCaseNode, headerCell });

        // ----------------------------------------
        // Call getRenderedInDiagrams()
        // ----------------------------------------
        // Check that the diagram is drawn on the CRUD, not on itself
        assertEquals(List.of(crud), AstahProToolSupport.getRenderedInDiagrams(useCaseDiagram));
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
