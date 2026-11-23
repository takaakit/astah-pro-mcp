package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.astah.pro.AstahElementType;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.MatrixDirection;
import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.astahpromcp.tool.astah.pro.common.MatrixShowElements;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.MatrixColumnCriteriaDTO;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.MatrixRowCriteriaDTO;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.RelationshipMatrixDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ActivityDiagramEditor;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.editor.CompositeStructureModelEditor;
import com.change_vision.jude.api.inf.editor.ERModelEditor;
import com.change_vision.jude.api.inf.editor.IDiagramEditorFactory;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.RequirementDiagramEditor;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.editor.StateMachineDiagramEditor;
import com.change_vision.jude.api.inf.editor.UseCaseModelEditor;
import com.change_vision.jude.api.inf.model.AggregationKind;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IERModel;
import com.change_vision.jude.api.inf.model.IERSchema;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.IMatrixDiagram;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.model.IRequirementDiagram;
import com.change_vision.jude.api.inf.model.IRequirementTable;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.ITestCase;
import com.change_vision.jude.api.inf.model.IUseCase;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RelationshipMatrixToolTest {
    
    private static final String TOOL_NAME = "retrieve_relationship_matrix";
    
    @TempDir
    Path tempDir;
    
    private ProjectAccessor projectAccessor;
    private RelationshipMatrixTool tool;
    private Method retrieveRelationshipMatrix;
    
    // Packages
    private IPackage requirements;
    private IPackage legacy;
    private IPackage design;
    private IPackage chassis;
    private IPackage tests;
    private IPackage useCases;
    
    // Requirements, with Wet road nested under Stopping distance, and Legacy limit in a subpackage
    private IRequirement noiseLevel;
    private IRequirement outputPower;
    private IRequirement stoppingDistance;
    private IRequirement wetRoad;
    private IRequirement weight;
    private IRequirement legacyLimit;
    
    // Classes of Design: three blocks, an interface, and a class in the subsystem Chassis
    private IClass brakeSystem;
    private IClass engine;
    private IClass wheel;
    private IClass caliper;
    private IClass pad;
    private IClass braking;
    private IClass vehicle;
    private IClass car;
    private IClass logger;
    private IClass sensor;
    
    // Test cases, with Wet brake test nested under Brake test
    private ITestCase brakeTest;
    private ITestCase wetBrakeTest;
    
    // Use cases, beside an actor named Driver
    private IUseCase drive;
    private IUseCase brake;
    private IUseCase park;
    
    private IAssociation carWheel;
    private IDependency satisfyStoppingDistance;
    
    private IClassDiagram brakeOverview;
    private IRequirementDiagram brakingRequirements;
    private ISequenceDiagram brakingSequence;
    private ICompositeStructureDiagram brakeStructure;
    
    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.create();
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        
        // Tool
        tool = new RelationshipMatrixTool(
            projectAccessor,
            astahProToolSupport);
        
        // retrieveRelationshipMatrix() method
        retrieveRelationshipMatrix = TestSupport.getAccessibleMethod(
            RelationshipMatrixTool.class,
            "retrieveRelationshipMatrix",
            RelationshipMatrixDTO.class);
        
        buildModel();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }
    
    // The model each test reads, built through the Astah API, holding every kind of relationship the tool shows
    private void buildModel() throws Exception {
        BasicModelEditor basic = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        UseCaseModelEditor useCase = projectAccessor.getModelEditorFactory().getUseCaseModelEditor();
        IDiagramEditorFactory diagrams = projectAccessor.getDiagramEditorFactory();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        
        transactionManager.beginTransaction();
        try {
            IModel root = projectAccessor.getProject();
            
            requirements = basic.createPackage(root, "Requirements");
            noiseLevel = requirement(basic.createRequirement(requirements, "Noise level"), "REQ-001");
            outputPower = requirement(basic.createRequirement(requirements, "Output power"), "REQ-002");
            stoppingDistance = requirement(basic.createRequirement(requirements, "Stopping distance"), "REQ-003");
            wetRoad = requirement(basic.createRequirement(stoppingDistance, "Wet road"), "REQ-003-1");
            weight = requirement(basic.createRequirement(requirements, "Weight"), "REQ-004");
            legacy = basic.createPackage(requirements, "Legacy");
            legacyLimit = requirement(basic.createRequirement(legacy, "Legacy limit"), "REQ-900");
            
            design = basic.createPackage(root, "Design");
            brakeSystem = block(basic, "BrakeSystem");
            engine = block(basic, "Engine");
            wheel = block(basic, "Wheel");
            caliper = basic.createClass(design, "Caliper");
            pad = basic.createClass(design, "Pad");
            braking = basic.createInterface(design, "Braking");
            vehicle = basic.createClass(design, "Vehicle");
            car = basic.createClass(design, "Car");
            logger = basic.createClass(design, "Logger");
            sensor = basic.createClass(design, "Sensor");
            chassis = basic.createSubsystem(design, "Chassis");
            basic.createClass(chassis, "Frame");
            basic.createEnumeration(design, "Mode");
            basic.createComponent(design, "ECU");
            basic.createNode(design, "Server");
            basic.createArtifact(design, "firmware.bin");
            
            // Associations: by default, navigable one way, navigable both ways, to itself, a composition and an aggregation, the whole on end A of the last two
            carWheel = basic.createAssociation(car, wheel, "", "", "");
            IAssociation carEngine = basic.createAssociation(car, engine, "", "", "");
            carEngine.getMemberEnds()[1].setNavigability("Navigable");
            IAssociation carBrakeSystem = basic.createAssociation(car, brakeSystem, "", "", "");
            carBrakeSystem.getMemberEnds()[0].setNavigability("Navigable");
            carBrakeSystem.getMemberEnds()[1].setNavigability("Navigable");
            basic.createAssociation(engine, engine, "", "", "");
            IAssociation brakeSystemCaliper = basic.createAssociation(brakeSystem, caliper, "", "", "");
            brakeSystemCaliper.getMemberEnds()[0].setAggregationKind(AggregationKind.COMPOSITE);
            IAssociation caliperPad = basic.createAssociation(caliper, pad, "", "", "");
            caliperPad.getMemberEnds()[0].setAggregationKind(AggregationKind.AGGREGATE);
            
            basic.createGeneralization(car, vehicle, "");
            basic.createRealization(brakeSystem, braking, "");
            basic.createUsage(car, sensor, "");
            // createDependency takes the supplier first: Car depends on Logger, and Design on Requirements
            basic.createDependency(logger, car, "");
            basic.createDependency(requirements, design, "");
            
            tests = basic.createPackage(root, "Tests");
            brakeTest = basic.createTestCase(tests, "Brake test");
            brakeTest.setTestCaseID("TC-1");
            wetBrakeTest = basic.createTestCase(brakeTest, "Wet brake test");
            wetBrakeTest.setTestCaseID("TC-1-1");
            
            useCases = basic.createPackage(root, "UseCases");
            drive = useCase.createUseCase(useCases, "Drive");
            brake = useCase.createUseCase(useCases, "Brake");
            park = useCase.createUseCase(useCases, "Park");
            useCase.createActor(useCases, "Driver");
            useCase.createInclude(drive, brake, "");
            useCase.createExtend(park, drive, "");
            
            // Requirement relationships, as the tools that create them make them: a dependency with a stereotype, from the client to the supplier
            satisfyStoppingDistance = requirementRelationship(basic, brakeSystem, stoppingDistance, "satisfy");
            requirementRelationship(basic, brakeSystem, wetRoad, "satisfy");
            requirementRelationship(basic, engine, outputPower, "satisfy");
            requirementRelationship(basic, wheel, noiseLevel, "satisfy", "trace");
            requirementRelationship(basic, brakeTest, stoppingDistance, "verify");
            requirementRelationship(basic, wetBrakeTest, wetRoad, "verify");
            requirementRelationship(basic, caliper, stoppingDistance, "refine");
            requirementRelationship(basic, wetRoad, stoppingDistance, "deriveReqt");
            requirementRelationship(basic, legacyLimit, weight, "copy");
            
            ClassDiagramEditor classDiagramEditor = diagrams.getClassDiagramEditor();
            brakeOverview = classDiagramEditor.createClassDiagram(design, "Brake overview");
            classDiagramEditor.setDiagram(brakeOverview);
            classDiagramEditor.createNodePresentation(brakeSystem, new Point2D.Double(0, 0));
            classDiagramEditor.createNodePresentation(caliper, new Point2D.Double(300, 0));
            
            RequirementDiagramEditor requirementDiagramEditor = diagrams.getRequirementDiagramEditor();
            brakingRequirements = requirementDiagramEditor.createRequirementDiagram(requirements, "Braking requirements");
            requirementDiagramEditor.setDiagram(brakingRequirements);
            requirementDiagramEditor.createNodePresentation(stoppingDistance, new Point2D.Double(0, 0));
            requirementDiagramEditor.createNodePresentation(wetRoad, new Point2D.Double(300, 0));
            
            // A lifeline whose base class is Engine
            SequenceDiagramEditor sequenceDiagramEditor = diagrams.getSequenceDiagramEditor();
            brakingSequence = sequenceDiagramEditor.createSequenceDiagram(design, "Braking sequence");
            sequenceDiagramEditor.setDiagram(brakingSequence);
            INodePresentation lifeline = sequenceDiagramEditor.createLifeline("engine", 0);
            ((ILifeline) lifeline.getModel()).setBase(engine);
            
            // BrakeSystem with a part typed by Caliper: the end of the composition that BrakeSystem owns
            CompositeStructureDiagramEditor compositeStructureDiagramEditor = diagrams.getCompositeStructureDiagramEditor();
            brakeStructure = compositeStructureDiagramEditor.createCompositeStructureDiagram(design, "Brake structure");
            compositeStructureDiagramEditor.setDiagram(brakeStructure);
            INodePresentation structuredClass = compositeStructureDiagramEditor.createStructuredClassPresentation(brakeSystem, new Point2D.Double(0, 0));
            compositeStructureDiagramEditor.createStructuredClassPresentation(brakeSystemCaliper.getMemberEnds()[1], structuredClass, new Point2D.Double(20, 60));
            
            transactionManager.endTransaction();
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private static IRequirement requirement(IRequirement requirement, String requirementId) throws Exception {
        requirement.setRequirementID(requirementId);
        return requirement;
    }
    
    private IClass block(BasicModelEditor basic, String name) throws Exception {
        IClass block = basic.createClass(design, name);
        block.addStereotype("block");
        return block;
    }
    
    private static IDependency requirementRelationship(BasicModelEditor basic, INamedElement client, INamedElement supplier, String... stereotypes) throws Exception {
        IDependency dependency = basic.createDependency(supplier, client, "");
        for (String stereotype : stereotypes) {
            dependency.addStereotype(stereotype);
        }
        return dependency;
    }
    
    // The criteria of an axis
    private record Axis(List<String> scopeIds, boolean includeNestedElements, List<String> elementTypes, List<String> stereotypes) {
        
        MatrixRowCriteriaDTO asRows() {
            return new MatrixRowCriteriaDTO(scopeIds, includeNestedElements, elementTypes, stereotypes);
        }
        
        MatrixColumnCriteriaDTO asColumns() {
            return new MatrixColumnCriteriaDTO(scopeIds, includeNestedElements, elementTypes, stereotypes);
        }
        
        Axis withStereotypes(String... stereotypes) {
            return new Axis(scopeIds, includeNestedElements, elementTypes, List.of(stereotypes));
        }
        
        Axis withoutNestedElements() {
            return new Axis(scopeIds, false, elementTypes, stereotypes);
        }
    }
    
    // The elements of the types in the scopes, nested elements included; no scope searches the whole project, and no type takes every one but the packages
    private static Axis axis(List<String> elementTypes, INamedElement... scopes) {
        return new Axis(Arrays.stream(scopes).map(INamedElement::getId).toList(), true, elementTypes, List.of());
    }
    
    private static List<String> types(String... typeNames) {
        return List.of(typeNames);
    }
    
    private MatrixMarkdown matrix(Axis rows, Axis columns, MatrixDirection direction, MatrixRelationshipKind... kinds) throws Exception {
        return matrix(rows, columns, List.of(kinds), direction, MatrixShowElements.ALL, false);
    }
    
    private MatrixMarkdown matrix(
            Axis rows,
            Axis columns,
            List<MatrixRelationshipKind> kinds,
            MatrixDirection direction,
            MatrixShowElements showElements,
            boolean listRelationships) throws Exception {
        return matrix(rows, columns, kinds, direction, showElements, false, false, listRelationships);
    }
    
    private MatrixMarkdown matrix(
            Axis rows,
            Axis columns,
            List<MatrixRelationshipKind> kinds,
            MatrixDirection direction,
            MatrixShowElements showElements,
            boolean includeImpliedRelationships,
            boolean groupByPackage,
            boolean listRelationships) throws Exception {
        // Create input DTO
        RelationshipMatrixDTO inputDTO = createInputDTO(
            rows,
            columns,
            kinds,
            direction,
            showElements,
            includeImpliedRelationships,
            groupByPackage,
            listRelationships);
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        List<McpSchema.Content> contents = TestSupport.instance().invokeToolMethodReturningContents(
            retrieveRelationshipMatrix,
            tool,
            inputDTO);
        
        // Check the contents
        assertEquals(1, contents.size());
        return new MatrixMarkdown(assertInstanceOf(McpSchema.TextContent.class, contents.get(0)).text());
    }
    
    // The row elements of a matrix whose rows are the given axis, by name
    private List<String> rowNames(Axis rows) throws Exception {
        return names(matrix(rows, axis(types()), MatrixDirection.BOTH, SATISFY), "Rows");
    }
    
    private static List<String> names(MatrixMarkdown markdown, String section) {
        if (!markdown.section(section).get(0).startsWith("|")) {
            return List.of();
        }
        return markdown.table(section).values().stream().map(row -> row.get("Name")).toList();
    }
    
    private static Map<String, String> rowOf(MatrixMarkdown markdown, String section, INamedElement element) {
        return markdown.table(section).values().stream()
                .filter(row -> row.get("ID").equals(element.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(element.getName() + " is not listed in " + section + ":\n" + markdown.text()));
    }
    
    // The Related cell of the row of the element, with the key of each other element replaced by its name, such as "Engine ←satisfy; Wheel ←satisfy"
    private static String related(MatrixMarkdown markdown, INamedElement element) {
        Map<String, String> namesByKey = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> column : markdown.table("Columns").entrySet()) {
            namesByKey.put(column.getKey(), column.getValue().get("Name"));
        }
        
        List<String> parts = new ArrayList<>();
        for (String part : rowOf(markdown, "Rows", element).get("Related").split("; ")) {
            if (part.isEmpty()) {
                continue;
            }
            int space = part.indexOf(' ');
            String key = part.substring(0, space);
            parts.add(namesByKey.containsKey(key) ? namesByKey.get(key) + part.substring(space) : part);
        }
        return String.join("; ", parts);
    }
    
    // With both directions, every element listed, and no option set
    private static RelationshipMatrixDTO createInputDTO(Axis rows, Axis columns, List<MatrixRelationshipKind> kinds) {
        return createInputDTO(rows, columns, kinds, MatrixDirection.BOTH, MatrixShowElements.ALL, false, false, false);
    }
    
    private static RelationshipMatrixDTO createInputDTO(
            Axis rows,
            Axis columns,
            List<MatrixRelationshipKind> kinds,
            MatrixDirection direction,
            MatrixShowElements showElements,
            boolean includeImpliedRelationships,
            boolean groupByPackage,
            boolean listRelationships) {
        return new RelationshipMatrixDTO(
            rows.asRows(),
            columns.asColumns(),
            kinds,
            direction,
            showElements,
            includeImpliedRelationships,
            groupByPackage,
            listRelationships);
    }
    
    // The numbers a line is made of, in order, as the counts of elements and packages in the criteria
    private static List<Integer> numbersIn(String text) {
        return Pattern.compile("\\d+").matcher(text).results()
            .map(match -> Integer.parseInt(match.group()))
            .toList();
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_listsTheBlocksThatSatisfyEachRequirement() throws Exception {
        MatrixMarkdown markdown = matrix(axis(types("Requirement"), requirements), axis(types("Class")).withStereotypes("block"), MatrixDirection.COLUMN_TO_ROW, SATISFY);
        
        // Check output Markdown
        // The criteria name the element type, the stereotype and the scope, and count the elements
        String rowCriteria = markdown.item("Criteria", "Rows");
        assertTrue(rowCriteria.contains("Requirement"), rowCriteria);
        assertTrue(rowCriteria.contains("\"Requirements\""), rowCriteria);
        assertEquals(List.of(6), numbersIn(rowCriteria));
        String columnCriteria = markdown.item("Criteria", "Columns");
        assertTrue(columnCriteria.contains("Class"), columnCriteria);
        assertTrue(columnCriteria.contains("block"), columnCriteria);
        assertEquals(List.of(3), numbersIn(columnCriteria));
        assertEquals("satisfy", markdown.item("Criteria", "Relationship kinds"));
        assertEquals("column_to_row", markdown.item("Criteria", "Direction"));
        
        assertEquals(List.of("Legacy limit", "Noise level", "Output power", "Stopping distance", "Wet road", "Weight"), names(markdown, "Rows"));
        assertEquals(List.of("BrakeSystem", "Engine", "Wheel"), names(markdown, "Columns"));
        
        assertEquals("", related(markdown, legacyLimit));
        assertEquals("Wheel ←satisfy", related(markdown, noiseLevel), "the dependency that is also a trace shows only the kind asked for");
        assertEquals("Engine ←satisfy", related(markdown, outputPower));
        assertEquals("BrakeSystem ←satisfy", related(markdown, stoppingDistance));
        assertEquals("BrakeSystem ←satisfy", related(markdown, wetRoad));
        assertEquals("", related(markdown, weight));
        
        assertEquals("4", markdown.item("Summary", "Relationships matching the criteria"));
        assertEquals("4 / 6 (67%)", markdown.item("Summary", "Rows with relationships"));
        assertEquals("3 / 3 (100%)", markdown.item("Summary", "Columns with relationships"));
        
        Map<String, String> wetRoadRow = rowOf(markdown, "Rows", wetRoad);
        assertEquals("Requirements.Stopping distance", wetRoadRow.get("Namespace"));
        assertEquals("Requirement", wetRoadRow.get("Type"));
        assertEquals("REQ-003-1", wetRoadRow.get("Req/Test ID"));
        assertEquals("1", wetRoadRow.get("Count"));
        assertEquals("Design", rowOf(markdown, "Columns", brakeSystem).get("Namespace"));
        assertEquals("2", rowOf(markdown, "Columns", brakeSystem).get("Count"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_listsTheRequirementsNoBlockSatisfiesWithEveryBlockToRelateThemTo() throws Exception {
        MatrixMarkdown markdown = matrix(
            axis(types("Requirement"), requirements),
            axis(types("Class")).withStereotypes("block"),
            List.of(SATISFY),
            MatrixDirection.COLUMN_TO_ROW,
            MatrixShowElements.ROWS_WITHOUT_RELATIONSHIPS,
            false);
        
        // Check output Markdown
        assertEquals(List.of("Legacy limit", "Weight"), names(markdown, "Rows"));
        assertEquals(List.of("BrakeSystem", "Engine", "Wheel"), names(markdown, "Columns"));
        assertEquals(brakeSystem.getId(), rowOf(markdown, "Columns", brakeSystem).get("ID"));
        assertEquals("(+2 not listed)", rowOf(markdown, "Columns", brakeSystem).get("Related"));
        assertEquals(MatrixMarkdownRenderer.NO_RELATIONSHIP_BETWEEN_LISTED, markdown.line("Matrix"));
        assertEquals("R1, R6", markdown.item("Summary", "Rows without relationships"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsOnlyTheRelationshipsThatGoTheWayAsked() throws Exception {
        Axis rows = axis(types("Requirement"), requirements);
        Axis columns = axis(types("Class")).withStereotypes("block");
        
        // Check output Markdown
        // A block satisfies a requirement, so a satisfy goes from a column to a row here
        assertEquals("0", matrix(rows, columns, MatrixDirection.ROW_TO_COLUMN, SATISFY).item("Summary", "Relationships matching the criteria"));
        assertEquals("4", matrix(rows, columns, MatrixDirection.COLUMN_TO_ROW, SATISFY).item("Summary", "Relationships matching the criteria"));
        assertEquals("4", matrix(rows, columns, MatrixDirection.BOTH, SATISFY).item("Summary", "Relationships matching the criteria"));
        
        // With the axes swapped, the same relationships go from a row to a column
        MatrixMarkdown swapped = matrix(columns, rows, MatrixDirection.ROW_TO_COLUMN, SATISFY);
        assertEquals("Stopping distance →satisfy; Wet road →satisfy", related(swapped, brakeSystem));
        assertEquals("4", swapped.item("Summary", "Relationships matching the criteria"));
        assertEquals("0", matrix(columns, rows, MatrixDirection.COLUMN_TO_ROW, SATISFY).item("Summary", "Relationships matching the criteria"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_tellsTheRequirementRelationshipsApartByTheirStereotypes() throws Exception {
        Axis allRequirements = axis(types("Requirement"), requirements);
        
        MatrixMarkdown verify = matrix(allRequirements, axis(types("TestCase"), tests), MatrixDirection.BOTH, VERIFY);
        
        // Check output Markdown
        assertEquals("Brake test ←verify", related(verify, stoppingDistance));
        assertEquals("Wet brake test ←verify", related(verify, wetRoad));
        assertEquals("2", verify.item("Summary", "Relationships matching the criteria"));
        
        MatrixMarkdown refine = matrix(allRequirements, axis(types("Class"), design), MatrixDirection.BOTH, REFINE);
        assertEquals("Caliper ←refine", related(refine, stoppingDistance));
        assertEquals("1", refine.item("Summary", "Relationships matching the criteria"));
        
        MatrixMarkdown deriveAndCopy = matrix(allRequirements, allRequirements, MatrixDirection.ROW_TO_COLUMN, DERIVE_REQT, COPY);
        assertEquals("Stopping distance →derive_reqt", related(deriveAndCopy, wetRoad));
        assertEquals("Weight →copy", related(deriveAndCopy, legacyLimit));
        assertEquals("2", deriveAndCopy.item("Summary", "Relationships matching the criteria"));
        
        MatrixMarkdown trace = matrix(allRequirements, axis(types("Class")).withStereotypes("block"), MatrixDirection.BOTH, TRACE);
        assertEquals("Wheel ←trace", related(trace, noiseLevel));
        assertEquals("1", trace.item("Summary", "Relationships matching the criteria"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsADependencyWithTwoRequirementStereotypesAsOneRelationship() throws Exception {
        MatrixMarkdown markdown = matrix(axis(types("Requirement"), requirements), axis(types("Class")).withStereotypes("block"), MatrixDirection.BOTH, SATISFY, TRACE);
        
        // Check output Markdown
        assertEquals("Wheel ←satisfy+trace", related(markdown, noiseLevel));
        assertEquals("1", rowOf(markdown, "Rows", noiseLevel).get("Count"));
        assertEquals("4", markdown.item("Summary", "Relationships matching the criteria"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_leavesTheRequirementRelationshipsOutOfTheDependencies() throws Exception {
        Axis everything = axis(types());
        
        MatrixMarkdown markdown = matrix(everything, everything, MatrixDirection.ROW_TO_COLUMN, DEPENDENCY);
        
        // Check output Markdown
        assertEquals("1", markdown.item("Summary", "Relationships matching the criteria"));
        assertEquals("Logger →dependency", related(markdown, car));
        
        // Between packages, which are rows and columns only when named
        MatrixMarkdown packages = matrix(axis(types("Package")), axis(types("Package")), MatrixDirection.ROW_TO_COLUMN, DEPENDENCY);
        assertEquals("Requirements →dependency", related(packages, design));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_givesAnAssociationTheDirectionOfItsNavigableEnds() throws Exception {
        Axis designClasses = axis(types("Class"), design);
        Axis carOnly = axis(types(), car);
        
        // Check output Markdown
        assertEquals("BrakeSystem ↔association; Engine →association; Wheel —association", related(matrix(carOnly, designClasses, MatrixDirection.ROW_TO_COLUMN, ASSOCIATION), car));
        assertEquals("BrakeSystem ↔association; Wheel —association", related(matrix(carOnly, designClasses, MatrixDirection.COLUMN_TO_ROW, ASSOCIATION), car));
        assertEquals("BrakeSystem ↔association; Engine →association; Wheel —association", related(matrix(carOnly, designClasses, MatrixDirection.BOTH, ASSOCIATION), car));
        
        // From the other end, with the association of Engine to itself
        assertEquals("Car ←association; Engine —association", related(matrix(axis(types(), engine), designClasses, MatrixDirection.COLUMN_TO_ROW, ASSOCIATION), engine));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_directsAggregationAndCompositionFromTheWholeToThePart() throws Exception {
        Axis designClasses = axis(types("Class"), design);
        
        MatrixMarkdown markdown = matrix(axis(types(), caliper), designClasses, MatrixDirection.BOTH, AGGREGATION, COMPOSITION);
        
        // Check output Markdown
        assertEquals("BrakeSystem ←composition; Pad →aggregation", related(markdown, caliper));
        
        assertEquals("Caliper →composition", related(matrix(axis(types(), brakeSystem), designClasses, MatrixDirection.ROW_TO_COLUMN, COMPOSITION), brakeSystem));
        
        // They are not plain associations
        assertEquals("0", matrix(axis(types(), caliper), designClasses, MatrixDirection.BOTH, ASSOCIATION).item("Summary", "Relationships matching the criteria"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_directsTheOtherKindsFromTheElementThatHasThem() throws Exception {
        Axis designClasses = axis(types("Class"), design);
        MatrixMarkdown classes = matrix(designClasses, designClasses, MatrixDirection.ROW_TO_COLUMN, GENERALIZATION, REALIZATION, USAGE);
        
        // Check output Markdown
        assertEquals("Sensor →usage; Vehicle →generalization", related(classes, car));
        assertEquals("Braking →realization", related(classes, brakeSystem));
        assertEquals("3", classes.item("Summary", "Relationships matching the criteria"));
        
        Axis allUseCases = axis(types("UseCase"), useCases);
        MatrixMarkdown useCaseMatrix = matrix(allUseCases, allUseCases, MatrixDirection.ROW_TO_COLUMN, INCLUDE, EXTEND);
        assertEquals("Brake →include", related(useCaseMatrix, drive));
        assertEquals("Drive →extend", related(useCaseMatrix, park));
        assertEquals("", related(useCaseMatrix, brake));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsTheContainmentOfAPackageAndOfANestedElement() throws Exception {
        MatrixMarkdown packages = matrix(axis(types("Package")), axis(types("Requirement"), requirements), MatrixDirection.ROW_TO_COLUMN, CONTAINMENT);
        
        // Check output Markdown
        assertEquals("Noise level →containment; Output power →containment; Stopping distance →containment; Weight →containment", related(packages, requirements));
        assertEquals("Legacy limit →containment", related(packages, legacy));
        assertEquals("5", packages.item("Summary", "Relationships matching the criteria"));
        
        Axis allRequirements = axis(types("Requirement"), requirements);
        assertEquals("Wet road →containment", related(matrix(allRequirements, allRequirements, MatrixDirection.ROW_TO_COLUMN, CONTAINMENT), stoppingDistance));
        
        Axis allTestCases = axis(types("TestCase"), tests);
        assertEquals("Wet brake test →containment", related(matrix(allTestCases, allTestCases, MatrixDirection.ROW_TO_COLUMN, CONTAINMENT), brakeTest));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_listsAContainmentOnceWithoutAnId() throws Exception {
        MatrixMarkdown markdown = matrix(axis(types("Package")), axis(types("Requirement"), requirements), List.of(CONTAINMENT), MatrixDirection.BOTH, MatrixShowElements.ALL, true);
        
        // Check output Markdown
        List<String> relationships = markdown.section("Relationships");
        assertEquals(2 + 5, relationships.size(), String.join("\n", relationships));
        for (String line : relationships.subList(2, relationships.size())) {
            assertTrue(line.endsWith("| →containment | | " + MatrixMarkdownRenderer.NO_ID + " |"), line);
        }
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_listsEachRelationshipWithItsId() throws Exception {
        MatrixMarkdown markdown = matrix(axis(types("Requirement"), requirements), axis(types("Class")).withStereotypes("block"), List.of(SATISFY), MatrixDirection.BOTH, MatrixShowElements.ALL, true);
        
        // Check output Markdown
        List<String> relationships = markdown.section("Relationships");
        assertEquals(2 + 4, relationships.size(), String.join("\n", relationships));
        assertTrue(relationships.stream().anyMatch(line -> line.endsWith("| ←satisfy | | " + satisfyStoppingDistance.getId() + " |")), String.join("\n", relationships));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_placesARelationshipBetweenElementsThatAreRowsAndColumnsInTwoCells() throws Exception {
        Axis allRequirements = axis(types("Requirement"), requirements);
        
        MatrixMarkdown markdown = matrix(allRequirements, allRequirements, MatrixDirection.BOTH, DERIVE_REQT);
        
        // Check output Markdown
        assertEquals("Stopping distance →derive_reqt", related(markdown, wetRoad));
        assertEquals("Wet road ←derive_reqt", related(markdown, stoppingDistance));
        assertEquals("1", markdown.item("Summary", "Relationships matching the criteria"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_placesTheAssociationOfAnElementWithItselfOnceWithoutADirection() throws Exception {
        Axis engineOnly = axis(types(), engine);
        
        for (MatrixDirection direction : MatrixDirection.values()) {
            MatrixMarkdown markdown = matrix(engineOnly, engineOnly, direction, ASSOCIATION);
            
            // Check output Markdown
            assertEquals("Engine —association", related(markdown, engine), direction.label());
            assertEquals("1", rowOf(markdown, "Rows", engine).get("Count"), direction.label());
            assertEquals("1", markdown.item("Summary", "Relationships matching the criteria"), direction.label());
        }
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_takesWhatAPackageOwnsAndWithNestedElementsWhatLiesDeeper() throws Exception {
        Axis owned = axis(types("Requirement"), requirements).withoutNestedElements();
        
        // Check output Markdown
        assertEquals(List.of("Noise level", "Output power", "Stopping distance", "Weight"), rowNames(owned));
        
        Axis nested = axis(types("Requirement"), requirements);
        assertEquals(List.of("Legacy limit", "Noise level", "Output power", "Stopping distance", "Wet road", "Weight"), rowNames(nested));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_takesAnElementAndWithNestedElementsWhatIsNestedUnderIt() throws Exception {
        // Check output Markdown
        assertEquals(List.of("Stopping distance"), rowNames(axis(types(), stoppingDistance).withoutNestedElements()));
        assertEquals(List.of("Stopping distance", "Wet road"), rowNames(axis(types(), stoppingDistance)));
        assertEquals(List.of("Brake test", "Wet brake test"), rowNames(axis(types(), brakeTest)));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_takesTheElementsDrawnOnADiagram() throws Exception {
        // Check output Markdown
        assertEquals(List.of("BrakeSystem", "Caliper"), rowNames(axis(types(), brakeOverview)));
        assertEquals(List.of("BrakeSystem", "Caliper"), rowNames(axis(types(), brakeOverview).withoutNestedElements()), "includeNestedElements does nothing for a diagram");
        assertEquals(List.of("Stopping distance", "Wet road"), rowNames(axis(types(), brakingRequirements)));
        
        MatrixMarkdown markdown = matrix(axis(types(), brakeOverview), axis(types()), MatrixDirection.BOTH, SATISFY);
        String rowCriteria = markdown.item("Criteria", "Rows");
        for (AstahElementType type : MatrixElementCollector.DEFAULT_TYPES) {
            assertTrue(rowCriteria.contains(type.typeName), rowCriteria);
        }
        assertTrue(rowCriteria.contains("\"Design.Brake overview\""), rowCriteria);
        assertEquals(List.of(2), numbersIn(rowCriteria));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_takesNoClassOfALifelineOrOfAPart() throws Exception {
        MatrixMarkdown sequence = matrix(axis(types(), brakingSequence), axis(types()), MatrixDirection.BOTH, SATISFY);
        
        // Check output Markdown
        assertEquals("No element matched the row criteria.", sequence.line("Rows"));
        
        assertEquals(List.of("BrakeSystem"), rowNames(axis(types(), brakeStructure)), "the structured class is drawn as itself, its part is not");
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_combinesScopesAndTakesASubsystemAsAPackage() throws Exception {
        Axis combined = axis(types(), tests, brakingRequirements).withoutNestedElements();
        
        // Check output Markdown
        assertEquals(List.of("Stopping distance", "Wet road", "Brake test"), rowNames(combined));
        
        assertEquals(List.of("Frame"), rowNames(axis(types(), chassis)));
        assertEquals(List.of("Legacy limit", "Noise level", "Output power", "Stopping distance", "Wet road", "Weight"), rowNames(axis(types("Requirement"))));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_neverTakesARequirementATestCaseOrAUseCaseAsAClass() throws Exception {
        MatrixMarkdown markdown = matrix(axis(types("Class")), axis(types()), MatrixDirection.BOTH, SATISFY);
        
        // Check output Markdown
        List<String> rowNames = names(markdown, "Rows");
        assertTrue(rowNames.containsAll(List.of("Braking", "Driver", "Frame", "BrakeSystem")), rowNames.toString());
        assertTrue(rowNames.stream().noneMatch(List.of("Stopping distance", "Brake test", "Drive")::contains), rowNames.toString());
        assertEquals(Set.of("Class"), Set.copyOf(markdown.table("Rows").values().stream().map(row -> row.get("Type")).toList()));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_takesPackagesOnlyWhenTheirTypesAreNamed() throws Exception {
        MatrixMarkdown everything = matrix(axis(types()), axis(types()), MatrixDirection.BOTH, SATISFY);
        
        // Check output Markdown
        Set<String> typeNames = new TreeSet<>(everything.table("Rows").values().stream().map(row -> row.get("Type")).toList());
        assertEquals(new TreeSet<>(Set.of("Artifact", "Class", "Component", "Enumeration", "Node", "Requirement", "TestCase", "UseCase")), typeNames);
        
        // The project itself is a Model, not a Package
        assertEquals(List.of("Design", "Chassis", "Requirements", "Legacy", "Tests", "UseCases"), rowNames(axis(types("Package", "Subsystem"))));
        assertEquals(List.of("ECU", "Mode", "Server", "firmware.bin"), rowNames(axis(types("Component", "Node", "Artifact", "Enumeration"))));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_comparesStereotypesCaseSensitively() throws Exception {
        // Check output Markdown
        assertEquals(List.of(), rowNames(axis(types("Class")).withStereotypes("Block")));
        assertEquals(List.of("BrakeSystem", "Engine", "Wheel"), rowNames(axis(types("Class")).withStereotypes("block")));
        assertEquals(List.of("Braking"), rowNames(axis(types("Class")).withStereotypes("interface")));
        assertEquals(List.of("Driver"), rowNames(axis(types("Class")).withStereotypes("actor")));
        assertEquals(List.of("Braking", "Driver"), rowNames(axis(types("Class")).withStereotypes("interface", "actor")));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_leavesTheProjectUnmodified() throws Exception {
        projectAccessor.saveAs(tempDir.resolve("RelationshipMatrixToolTest.asta").toString());
        assertFalse(projectAccessor.isProjectModified());
        
        matrix(axis(types()), axis(types()), List.of(MatrixRelationshipKind.values()), MatrixDirection.BOTH, MatrixShowElements.ALL, true);
        
        // Check that the project is not modified
        assertFalse(projectAccessor.isProjectModified());
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_keepsTheKeysWhateverShowElementsIs() throws Exception {
        Axis rows = axis(types("Requirement"), requirements);
        Axis columns = axis(types("Class")).withStereotypes("block");
        
        MatrixMarkdown all = matrix(rows, columns, List.of(SATISFY), MatrixDirection.BOTH, MatrixShowElements.ALL, false);
        MatrixMarkdown gaps = matrix(rows, columns, List.of(SATISFY), MatrixDirection.BOTH, MatrixShowElements.ROWS_WITHOUT_RELATIONSHIPS, false);
        
        // Check output Markdown
        for (Map.Entry<String, Map<String, String>> row : gaps.table("Rows").entrySet()) {
            assertEquals(all.table("Rows").get(row.getKey()).get("ID"), row.getValue().get("ID"), row.getKey());
        }
    }
    
    @Test
    void retrieveRelationshipMatrix_ng_unknownElementType() throws Exception {
        // Create input DTO
        RelationshipMatrixDTO inputDTO = createInputDTO(axis(types("Interface")), axis(types()), List.of(SATISFY));
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                tool,
                inputDTO);
        });
        
        // Check exception
        // An argument the caller got wrong, which ToolSupport logs as a warning rather than as a fault of the server
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Unknown element type 'Interface' in rows.elementTypes."));
        // Every element type a row or a column can take is named
        for (AstahElementType type : MatrixElementCollector.ELEMENT_TYPES) {
            assertTrue(exception.getCause().getMessage().contains(type.typeName));
        }
        
        // Compared exactly
        RelationshipMatrixDTO lowerCaseInputDTO = createInputDTO(axis(types()), axis(types("class")), List.of(SATISFY));
        Exception lowerCaseException = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                tool,
                lowerCaseInputDTO);
        });
        assertInstanceOf(IllegalArgumentException.class, lowerCaseException.getCause());
        assertTrue(lowerCaseException.getCause().getMessage().contains("Unknown element type 'class' in columns.elementTypes."));
    }
    
    @Test
    void retrieveRelationshipMatrix_ng_elementTypeThatCannotBeARowOrAColumn() throws Exception {
        for (String typeName : List.of("Operation", "ClassDiagram", "AssociationClass", "Model", "StateMachine")) {
            // Create input DTO
            RelationshipMatrixDTO inputDTO = createInputDTO(axis(types(typeName)), axis(types()), List.of(SATISFY));
            
            // ----------------------------------------
            // Call retrieveRelationshipMatrix()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningContents(
                    retrieveRelationshipMatrix,
                    tool,
                    inputDTO);
            });
            
            // Check exception
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("The element type '" + typeName + "' in rows.elementTypes cannot be a row or a column."));
        }
    }
    
    @Test
    void retrieveRelationshipMatrix_ng_scopeIdThatNamesNothing() throws Exception {
        // Create input DTO
        Axis rows = new Axis(List.of("no-such-id"), true, types(), List.of());
        RelationshipMatrixDTO inputDTO = createInputDTO(rows, axis(types()), List.of(SATISFY));
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                tool,
                inputDTO);
        });
        
        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Failed to get named element (ID: no-such-id) due to an incorrect ID.", exception.getCause().getMessage());
    }
    
    @Test
    void retrieveRelationshipMatrix_ng_scopeThatCannotBoundAnAxis() throws Exception {
        // Create input DTO
        RelationshipMatrixDTO inputDTO = createInputDTO(axis(types()), axis(types(), carWheel), List.of(SATISFY));
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                tool,
                inputDTO);
        });
        
        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The Association (ID: " + carWheel.getId() + ") in columns.scopeIds cannot bound the columns."));
        
        RelationshipMatrixDTO dependencyInputDTO = createInputDTO(axis(types(), satisfyStoppingDistance), axis(types()), List.of(SATISFY));
        Exception dependencyException = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                tool,
                dependencyInputDTO);
        });
        assertInstanceOf(IllegalArgumentException.class, dependencyException.getCause());
        assertTrue(dependencyException.getCause().getMessage().contains("The Dependency (ID: " + satisfyStoppingDistance.getId() + ") in rows.scopeIds cannot bound the rows."));
    }
    
    // The API cannot create a requirement table or a CRUD, so mocks stand in for them
    @Test
    void retrieveRelationshipMatrix_ng_tableAsAScope() throws Exception {
        IRequirementTable requirementTable = mock(IRequirementTable.class);
        when(requirementTable.getId()).thenReturn("requirementTable");
        when(requirementTable.getName()).thenReturn("Requirement Table0");
        IMatrixDiagram crud = mock(IMatrixDiagram.class);
        when(crud.getId()).thenReturn("crud");
        when(crud.getName()).thenReturn("CRUD0");
        
        AstahProToolSupport mockedSupport = mock(AstahProToolSupport.class);
        when(mockedSupport.getNamedElement("requirementTable")).thenReturn(requirementTable);
        when(mockedSupport.getNamedElement("crud")).thenReturn(crud);
        RelationshipMatrixTool mockedTool = new RelationshipMatrixTool(projectAccessor, mockedSupport);
        
        // Create input DTO
        RelationshipMatrixDTO inputDTO = createInputDTO(new Axis(List.of("requirementTable"), true, types(), List.of()), axis(types()), List.of(SATISFY));
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                mockedTool,
                inputDTO);
        });
        
        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("The requirement table \"Requirement Table0\" (ID: requirementTable) in rows.scopeIds cannot bound the rows"));
        
        RelationshipMatrixDTO crudInputDTO = createInputDTO(axis(types()), new Axis(List.of("crud"), true, types(), List.of()), List.of(SATISFY));
        Exception crudException = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                mockedTool,
                crudInputDTO);
        });
        assertInstanceOf(IllegalArgumentException.class, crudException.getCause());
        assertTrue(crudException.getCause().getMessage().contains("The CRUD \"CRUD0\" (ID: crud) in columns.scopeIds cannot bound the columns"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ng_noRelationshipKind() throws Exception {
        // Create input DTO
        RelationshipMatrixDTO inputDTO = createInputDTO(axis(types()), axis(types()), List.of());
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                tool,
                inputDTO);
        });
        
        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("relationshipKinds is empty. Specify at least one kind of relationship, such as satisfy.", exception.getCause().getMessage());
    }
    
    @Test
    void retrieveRelationshipMatrix_ng_noDirection() throws Exception {
        // Create input DTO
        RelationshipMatrixDTO inputDTO = createInputDTO(
            axis(types()),
            axis(types()),
            List.of(SATISFY),
            null,
            MatrixShowElements.ALL,
            false,
            false,
            false);
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                tool,
                inputDTO);
        });
        
        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("direction is missing. Specify row_to_column, column_to_row or both.", exception.getCause().getMessage());
    }
    
    // A response cut short could make the caller take a relationship it does not see for one that does not exist
    @Test
    void retrieveRelationshipMatrix_ng_responseOverTheLimit() throws Exception {
        RelationshipMatrixTool smallTool = new RelationshipMatrixTool(projectAccessor, new AstahProToolSupport(projectAccessor), 50, 20, 1_000);
        
        // Create input DTO
        RelationshipMatrixDTO inputDTO = createInputDTO(axis(types("Requirement"), requirements), axis(types("Class")).withStereotypes("block"), List.of(SATISFY));
        
        // ----------------------------------------
        // Call retrieveRelationshipMatrix()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningContents(
                retrieveRelationshipMatrix,
                smallTool,
                inputDTO);
        });
        
        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        // The limit the tool was given, 1_000 bytes
        assertTrue(exception.getCause().getMessage().contains("1,000"));
    }
    
    private McpSchema.CallToolResult call(Map<String, Object> arguments) {
        List<ToolDefinition> definitions = tool.createToolDefinitions();
        assertEquals(1, definitions.size());
        assertEquals(TOOL_NAME, definitions.get(0).toolSchema().name());
        assertEquals(ToolDefinition.ResultKind.CONTENTS, definitions.get(0).resultKind());
        return definitions.get(0).toolHandler().apply(null, new McpSchema.CallToolRequest(TOOL_NAME, arguments, null));
    }
    
    private Map<String, Object> arguments(List<String> rowTypes) {
        return Map.of(
            "rows", Map.of("scopeIds", List.of(requirements.getId()), "includeNestedElements", true, "elementTypes", rowTypes, "stereotypes", List.of()),
            "columns", Map.of("scopeIds", List.of(), "includeNestedElements", true, "elementTypes", List.of("Class"), "stereotypes", List.of("block")),
            "relationshipKinds", List.of("satisfy"),
            "direction", "column_to_row",
            "showElements", "rows_without_relationships",
            "includeImpliedRelationships", false,
            "groupByPackage", false,
            "listRelationships", false);
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_returnsTheMatrixAsOneTextContent() throws Exception {
        // ----------------------------------------
        // Call retrieve_relationship_matrix through its tool handler
        // ----------------------------------------
        McpSchema.CallToolResult result = call(arguments(List.of("Requirement")));
        
        // Check the result
        assertNotEquals(Boolean.TRUE, result.isError());
        assertEquals(1, result.content().size());
        String text = assertInstanceOf(McpSchema.TextContent.class, result.content().get(0)).text();
        assertTrue(text.startsWith("# Relationship Matrix\n"), text);
        assertEquals(List.of("Legacy limit", "Weight"), names(new MatrixMarkdown(text), "Rows"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ng_returnsTheMessageOfAnArgumentGivenWrongly() throws Exception {
        // ----------------------------------------
        // Call retrieve_relationship_matrix through its tool handler
        // ----------------------------------------
        McpSchema.CallToolResult result = call(arguments(List.of("Interface")));
        
        // Check the result
        assertEquals(Boolean.TRUE, result.isError());
        String text = assertInstanceOf(McpSchema.TextContent.class, result.content().get(0)).text();
        // The error names the tool function, the element type given and where it was given
        assertTrue(text.contains(TOOL_NAME), text);
        assertTrue(text.contains("'Interface'"), text);
        assertTrue(text.contains("rows.elementTypes"), text);
    }
    
    @FunctionalInterface
    private interface ModelEdit {
        void apply(BasicModelEditor basic) throws Exception;
    }
    
    private void edit(ModelEdit modelEdit) throws Exception {
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        transactionManager.beginTransaction();
        try {
            modelEdit.apply(projectAccessor.getModelEditorFactory().getBasicModelEditor());
            transactionManager.endTransaction();
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    // A composition whose whole is the first class, marked on the end typed by the whole as Astah does, with the part, the other end, named
    private static IAssociation composition(BasicModelEditor basic, IClass whole, IClass part, String partName) throws Exception {
        IAssociation association = basic.createAssociation(whole, part, "", "", "");
        association.getMemberEnds()[0].setAggregationKind(AggregationKind.COMPOSITE);
        association.getMemberEnds()[1].setName(partName);
        return association;
    }
    
    private static boolean anyLineMatches(List<String> lines, String regex) {
        return lines.stream().anyMatch(line -> line.matches(regex));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_takesAClassToSatisfyWhatItsPartsSatisfyWhenAsked() throws Exception {
        // Caliper, a part of BrakeSystem, satisfies Weight, and Piston, a part of Caliper, satisfies Noise level
        edit(basic -> {
            IClass piston = basic.createClass(design, "Piston");
            composition(basic, caliper, piston, "piston");
            requirementRelationship(basic, caliper, weight, "satisfy");
            requirementRelationship(basic, piston, noiseLevel, "satisfy");
        });
        Axis rows = axis(types("Requirement"), requirements);
        Axis blocks = axis(types("Class")).withStereotypes("block");
        
        // Check output Markdown
        assertEquals("", related(matrix(rows, blocks, List.of(SATISFY), MatrixDirection.COLUMN_TO_ROW, MatrixShowElements.ALL, false), weight));
        
        MatrixMarkdown markdown = matrix(rows, blocks, List.of(SATISFY), MatrixDirection.COLUMN_TO_ROW, MatrixShowElements.ALL, true, false, true);
        assertEquals("BrakeSystem ←satisfy (implied)", related(markdown, weight));
        // Wheel satisfies Noise level itself, and BrakeSystem through Piston, two compositions down
        assertEquals("BrakeSystem ←satisfy (implied); Wheel ←satisfy", related(markdown, noiseLevel));
        // BrakeSystem satisfies Stopping distance itself, which leaves no implied relationship beside it
        assertEquals("BrakeSystem ←satisfy", related(markdown, stoppingDistance));
        assertEquals("6", markdown.item("Summary", "Relationships matching the criteria"));
        assertEquals(MatrixMarkdownRenderer.IMPLIED_RELATIONSHIPS, markdown.item("Criteria", "Implied relationships"));
        
        List<String> relationships = markdown.section("Relationships");
        assertTrue(anyLineMatches(relationships, "\\| R\\d+ Weight \\| C1 BrakeSystem \\| ←satisfy \\(implied\\) \\| via Caliper \\| — \\|"), String.join("\n", relationships));
        assertTrue(anyLineMatches(relationships, "\\| R\\d+ Noise level \\| C1 BrakeSystem \\| ←satisfy \\(implied\\) \\| via Piston \\| — \\|"), String.join("\n", relationships));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_impliesNothingUnlessSatisfyIsAskedFor() throws Exception {
        MatrixMarkdown markdown = matrix(axis(types("Requirement"), requirements), axis(types("Class")).withStereotypes("block"),
            List.of(TRACE), MatrixDirection.BOTH, MatrixShowElements.ALL, true, false, false);
        
        // Check output Markdown
        assertFalse(markdown.text().contains("- Implied relationships:"), markdown.text());
        assertEquals("1", markdown.item("Summary", "Relationships matching the criteria"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsHowThePackagesThatHoldTheElementsRelate() throws Exception {
        MatrixMarkdown markdown = matrix(axis(types("Requirement")), axis(types("Class")).withStereotypes("block"),
            List.of(SATISFY), MatrixDirection.COLUMN_TO_ROW, MatrixShowElements.ALL, false, true, true);
        
        // Check output Markdown
        // The criteria count the elements and the packages that hold them
        String rowCriteria = markdown.item("Criteria", "Rows");
        assertTrue(rowCriteria.contains("Requirement"), rowCriteria);
        assertEquals(List.of(6, 2), numbersIn(rowCriteria));
        String columnCriteria = markdown.item("Criteria", "Columns");
        assertTrue(columnCriteria.contains("Class"), columnCriteria);
        assertTrue(columnCriteria.contains("block"), columnCriteria);
        assertEquals(List.of(3, 1), numbersIn(columnCriteria));
        assertEquals(MatrixMarkdownRenderer.GROUPED_BY_PACKAGE, markdown.item("Criteria", "Grouped by package"));
        // Wet road, nested under Stopping distance, belongs to the package Requirements as well
        assertEquals(List.of(
            "| | C1 Design |",
            "|---|---|",
            "| R1 Requirements | ←satisfy ×4 |",
            "| R2 Legacy | |"),
            markdown.section("Matrix"));
        assertEquals(requirements.getId(), rowOf(markdown, "Rows", requirements).get("ID"));
        assertEquals("4", markdown.item("Summary", "Relationships matching the criteria"));
        assertTrue(anyLineMatches(markdown.section("Relationships"), "\\| R1 Requirements \\| C1 Design \\| ←satisfy \\| Wet road ← BrakeSystem \\| .+ \\|"),
            String.join("\n", markdown.section("Relationships")));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_leavesOutTheRelationshipsWithinOnePackage() throws Exception {
        IClass frame = (IClass) TestSupport.instance().getNamedElementByClassAndName(IClass.class, "Frame");
        edit(basic -> basic.createDependency(engine, frame, ""));
        Axis designClasses = axis(types("Class"), design);
        
        MatrixMarkdown markdown = matrix(designClasses, designClasses, List.of(ASSOCIATION, COMPOSITION, GENERALIZATION, REALIZATION, USAGE, DEPENDENCY),
            MatrixDirection.BOTH, MatrixShowElements.ALL, false, true, false);
        
        // Check output Markdown
        // Frame belongs to the subsystem Chassis, a package of its own, and only its dependency on Engine crosses packages
        assertEquals(List.of("Design", "Chassis"), names(markdown, "Rows"));
        assertEquals("1", markdown.item("Summary", "Relationships matching the criteria"));
        assertEquals("C1 →dependency", markdown.table("Rows").get("R2").get("Related"));
        assertEquals("C2 ←dependency", markdown.table("Rows").get("R1").get("Related"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsTheTransitionsBetweenStates() throws Exception {
        INamedElement[] vertices = new INamedElement[3];
        INamedElement[] diagrams = new INamedElement[1];
        edit(basic -> {
            StateMachineDiagramEditor editor = projectAccessor.getDiagramEditorFactory().getStateMachineDiagramEditor();
            IStateMachineDiagram carStates = editor.createStatemachineDiagram(car, "Car states");
            editor.setDiagram(carStates);
            INodePresentation initial = editor.createInitialPseudostate(null, new Point2D.Double(0, 0));
            INodePresentation parked = editor.createState("Parked", null, new Point2D.Double(100, 0));
            INodePresentation driving = editor.createState("Driving", null, new Point2D.Double(300, 0));
            editor.createTransition(initial, parked);
            editor.createTransition(parked, driving);
            editor.createTransition(driving, parked);
            vertices[0] = (INamedElement) initial.getModel();
            vertices[1] = (INamedElement) parked.getModel();
            vertices[2] = (INamedElement) driving.getModel();
            diagrams[0] = carStates;
        });
        Axis vertexAxis = axis(types("State", "Pseudostate"), diagrams[0]);
        
        MatrixMarkdown markdown = matrix(vertexAxis, vertexAxis, List.of(TRANSITION), MatrixDirection.ROW_TO_COLUMN, MatrixShowElements.ALL, false);
        
        // Check output Markdown
        assertEquals("Parked →transition", related(markdown, vertices[0]));
        assertEquals("Driving →transition", related(markdown, vertices[1]));
        assertEquals("Parked →transition", related(markdown, vertices[2]));
        assertEquals("3", markdown.item("Summary", "Relationships matching the criteria"));
        
        // A state has no owner, and belongs to Car, which the diagram it is drawn on belongs to
        assertEquals(List.of("Driving", "Parked"), rowNames(axis(types("State"), car)));
        assertEquals(List.of("Design"), names(matrix(vertexAxis, vertexAxis, List.of(TRANSITION), MatrixDirection.BOTH, MatrixShowElements.ALL, false, true, false), "Rows"));
        assertFalse(rowNames(axis(types())).contains("Parked"), "a state is taken only when elementTypes names its type");
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsTheFlowsBetweenActivityNodes() throws Exception {
        INamedElement[] nodes = new INamedElement[3];
        INamedElement[] diagrams = new INamedElement[1];
        edit(basic -> {
            ActivityDiagramEditor editor = projectAccessor.getDiagramEditorFactory().getActivityDiagramEditor();
            IActivityDiagram brakingFlow = editor.createActivityDiagram(design, "Braking flow");
            editor.setDiagram(brakingFlow);
            INodePresentation start = editor.createInitialNode("start", new Point2D.Double(0, 0));
            INodePresentation press = editor.createAction("Press pedal", new Point2D.Double(0, 100));
            INodePresentation apply = editor.createAction("Apply brake", new Point2D.Double(0, 200));
            editor.createFlow(start, press);
            editor.createFlow(press, apply);
            nodes[0] = (INamedElement) start.getModel();
            nodes[1] = (INamedElement) press.getModel();
            nodes[2] = (INamedElement) apply.getModel();
            diagrams[0] = brakingFlow;
        });
        Axis nodeAxis = axis(types("Action", "ControlNode"), diagrams[0]);
        
        MatrixMarkdown markdown = matrix(nodeAxis, nodeAxis, List.of(FLOW), MatrixDirection.ROW_TO_COLUMN, MatrixShowElements.ALL, false);
        
        // Check output Markdown
        assertEquals("Press pedal →flow", related(markdown, nodes[0]));
        assertEquals("Apply brake →flow", related(markdown, nodes[1]));
        assertEquals("", related(markdown, nodes[2]));
        
        // An action has no owner, and belongs to the package Design, which the diagram it is drawn on belongs to
        assertEquals(List.of("Apply brake", "Press pedal"), rowNames(axis(types("Action"), design).withoutNestedElements()));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsTheConnectorsBetweenPartsAndPorts() throws Exception {
        INamedElement[] elements = new INamedElement[5];
        edit(basic -> {
            IClass hub = basic.createClass(design, "Hub");
            IClass rotor = basic.createClass(design, "Rotor");
            IClass shoe = basic.createClass(design, "Shoe");
            IAttribute rotorPart = composition(basic, hub, rotor, "rotor").getMemberEnds()[1];
            IAttribute shoePart = composition(basic, hub, shoe, "shoe").getMemberEnds()[1];
            IPort out = basic.createPort(rotor, "out");
            CompositeStructureModelEditor editor = projectAccessor.getModelEditorFactory().getCompositeStructureModelEditor();
            editor.createConnector(rotorPart, null, shoePart, null);
            editor.createConnector(rotorPart, out, shoePart, null);
            elements[0] = hub;
            elements[1] = rotor;
            elements[2] = rotorPart;
            elements[3] = shoePart;
            elements[4] = out;
        });
        Axis partsAndPorts = axis(types("Attribute", "Port"), elements[0], elements[1]);
        
        MatrixMarkdown markdown = matrix(partsAndPorts, partsAndPorts, List.of(CONNECTOR), MatrixDirection.ROW_TO_COLUMN, MatrixShowElements.ALL, false);
        
        // Check output Markdown
        // A connector has no direction, and its end on a port is the port rather than the part
        assertEquals("shoe —connector", related(markdown, elements[2]));
        assertEquals("rotor —connector; out —connector", related(markdown, elements[3]));
        assertEquals("shoe —connector", related(markdown, elements[4]));
        assertEquals("2", markdown.item("Summary", "Relationships matching the criteria"));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsTheLinksBetweenInstanceSpecifications() throws Exception {
        INamedElement[] instances = new INamedElement[2];
        edit(basic -> {
            ClassDiagramEditor editor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
            editor.setDiagram(brakeOverview);
            INodePresentation frontWheel = editor.createInstanceSpecification("frontWheel", new Point2D.Double(0, 300));
            INodePresentation frontBrake = editor.createInstanceSpecification("frontBrake", new Point2D.Double(300, 300));
            ILinkPresentation link = editor.createInstanceSpecificationLink(frontWheel, frontBrake);
            ((ILink) link.getModel()).getMemberEnds()[1].setNavigability("Navigable");
            instances[0] = (INamedElement) frontWheel.getModel();
            instances[1] = (INamedElement) frontBrake.getModel();
        });
        Axis instanceAxis = axis(types("InstanceSpecification"));
        
        MatrixMarkdown markdown = matrix(instanceAxis, instanceAxis, List.of(LINK), MatrixDirection.ROW_TO_COLUMN, MatrixShowElements.ALL, false);
        
        // Check output Markdown
        // The link goes the way its navigable end does, as an association does
        assertEquals("frontBrake →link", related(markdown, instances[0]));
        assertEquals("", related(markdown, instances[1]));
        assertEquals("frontWheel ←link", related(matrix(instanceAxis, instanceAxis, List.of(LINK), MatrixDirection.COLUMN_TO_ROW, MatrixShowElements.ALL, false), instances[1]));
    }
    
    @Test
    void retrieveRelationshipMatrix_ok_showsTheERRelationshipsFromTheParentToTheChild() throws Exception {
        INamedElement[] entities = new INamedElement[2];
        edit(basic -> {
            ERModelEditor editor = projectAccessor.getModelEditorFactory().getERModelEditor();
            IERModel erModel = editor.createERModel(projectAccessor.getProject(), "ER Model");
            IERSchema schema = erModel.getSchemata()[0];
            IEREntity customer = editor.createEREntity(schema, "Customer", "CUSTOMER");
            IEREntity order = editor.createEREntity(schema, "Order", "ORDER");
            editor.createNonIdentifyingRelationship(customer, order, "places", "PLACES");
            entities[0] = customer;
            entities[1] = order;
        });
        Axis entityAxis = axis(types("EREntity"));
        
        MatrixMarkdown markdown = matrix(entityAxis, entityAxis, List.of(ER_RELATIONSHIP), MatrixDirection.BOTH, MatrixShowElements.ALL, false);
        
        // Check output Markdown
        assertEquals("Order →er_relationship", related(markdown, entities[0]));
        assertEquals("Customer ←er_relationship", related(markdown, entities[1]));
        assertEquals("1", markdown.item("Summary", "Relationships matching the criteria"));
    }
}
