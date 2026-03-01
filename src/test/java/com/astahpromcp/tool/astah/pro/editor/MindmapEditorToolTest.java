package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.MindmapEditor;
import com.change_vision.jude.api.inf.model.IMindMapDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class MindmapEditorToolTest {

    private ProjectAccessor projectAccessor;
    private MindmapEditorTool tool;
    private Method createMindmapDiagram;
    private Method changeToFloatingTopic;
    private Method createFloatingTopic;
    private Method createTopic;
    private Method createTopicLink;
    private Method changeParentOfTopic;
    private Method moveTopicWithinSiblingOrder;
    private Method insertSvgImageIntoTopic;
    private Method insertPngImageIntoTopic;
    private Method insertJpgImageIntoTopic;
    private Method deleteChildTopics;
    private Method deleteImageFromTopic;
    private Method setBoundaryOfTopic;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/editor/MindmapEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        MindmapEditor mindmapEditor = projectAccessor.getDiagramEditorFactory().getMindmapEditor();
        ImageConvertSupport imageConvertSupport = new ImageConvertSupport();

        // Tool
        tool = new MindmapEditorTool(
            projectAccessor,
            transactionManager,
            mindmapEditor,
            astahProToolSupport,
            imageConvertSupport,
            true);

        // createMindmapDiagram() method
        createMindmapDiagram = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createMindmapDiagram",
            McpSyncServerExchange.class,
            NewDiagramInPackageDTO.class);

        // changeToFloatingTopic() method
        changeToFloatingTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "changeToFloatingTopic",
            McpSyncServerExchange.class,
            ChangeToFloatingTopicDTO.class);

        // createFloatingTopic() method
        createFloatingTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createFloatingTopic",
            McpSyncServerExchange.class,
            NewFloatingTopicDTO.class);

        // createTopic() method
        createTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createTopic",
            McpSyncServerExchange.class,
            NewTopicDTO.class);

        // createTopicLink() method
        createTopicLink = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createTopicLink",
            McpSyncServerExchange.class,
            NewLinkBetweenTopicsDTO.class);

        // changeParentOfTopic() method
        changeParentOfTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "changeParentOfTopic",
            McpSyncServerExchange.class,
            ChangeParentOfTopicDTO.class);

        // moveTopicWithinSiblingOrder() method
        moveTopicWithinSiblingOrder = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "moveTopicWithinSiblingOrder",
            McpSyncServerExchange.class,
            MoveTopicWithinSiblingOrderDTO.class);

        // insertSvgImageIntoTopic() method
        insertSvgImageIntoTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "insertSvgImageIntoTopic",
            McpSyncServerExchange.class,
            NewSvgImageIntoTopicDTO.class);

        // insertPngImageIntoTopic() method
        insertPngImageIntoTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "insertPngImageIntoTopic",
            McpSyncServerExchange.class,
            NewPngImageIntoTopicDTO.class);

        // insertJpgImageIntoTopic() method
        insertJpgImageIntoTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "insertJpgImageIntoTopic",
            McpSyncServerExchange.class,
            NewJpgImageIntoTopicDTO.class);

        // deleteChildTopics() method
        deleteChildTopics = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "deleteChildTopics",
            McpSyncServerExchange.class,
            DeleteChildTopicsDTO.class);

        // deleteImageFromTopic() method
        deleteImageFromTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "deleteImageFromTopic",
            McpSyncServerExchange.class,
            DeleteImageFromTopicDTO.class);

        // setBoundaryOfTopic() method
        setBoundaryOfTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "setBoundaryOfTopic",
            McpSyncServerExchange.class,
            TopicWithBoundaryVisibilityDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createMindmapDiagram_ok() throws Exception {
        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");

        // Create input DTO
        NewDiagramInPackageDTO inputDTO = new NewDiagramInPackageDTO(
            astahPackage.getId(),
            "Test Mind Map Diagram");

        // ----------------------------------------
        // Call createMindmapDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createMindmapDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createFloatingTopic_ok() throws Exception {
        // Get mind map diagram
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");

        // Create input DTO
        NewFloatingTopicDTO inputDTO = new NewFloatingTopicDTO(
            mindMapDiagram.getId(),
            "Test Floating Topic",
            200,
            150);

        // ----------------------------------------
        // Call createFloatingTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createFloatingTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTopic_ok() throws Exception {
        // Get mind map diagram and root topic
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();

        // Create input DTO
        NewTopicDTO inputDTO = new NewTopicDTO(
            mindMapDiagram.getId(),
            rootTopic.getID(),
            "Test Child Topic");

        // ----------------------------------------
        // Call createTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTopicLink_ok() throws Exception {
        // Get mind map diagram, root topic and a floating topic
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();
        INodePresentation[] floatingTopics = mindMapDiagram.getFloatingTopics();

        assumeTrue(floatingTopics.length > 0, "Mindmap0 needs at least one floating topic");
        INodePresentation floatingTopic = floatingTopics[0];

        // Create input DTO
        NewLinkBetweenTopicsDTO inputDTO = new NewLinkBetweenTopicsDTO(
            mindMapDiagram.getId(),
            rootTopic.getID(),
            floatingTopic.getID());

        // ----------------------------------------
        // Call createTopicLink()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTopicLink,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void changeToFloatingTopic_ok() throws Exception {
        // Get mind map diagram and a child topic (not root, not floating)
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();
        INodePresentation[] children = rootTopic.getChildren();

        assumeTrue(children.length > 0, "Root topic needs at least one child for changeToFloatingTopic");
        INodePresentation childTopic = children[0];

        // Create input DTO
        ChangeToFloatingTopicDTO inputDTO = new ChangeToFloatingTopicDTO(
            mindMapDiagram.getId(),
            childTopic.getID());

        // ----------------------------------------
        // Call changeToFloatingTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeToFloatingTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void changeParentOfTopic_ok() throws Exception {
        // Get mind map diagram, target topic and new parent
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();
        INodePresentation[] children = rootTopic.getChildren();

        assumeTrue(children.length >= 2, "Root topic needs at least two children for changeParentOfTopic");
        INodePresentation targetTopic = children[0];
        INodePresentation newParentTopic = children[1];

        // Create input DTO
        ChangeParentOfTopicDTO inputDTO = new ChangeParentOfTopicDTO(
            mindMapDiagram.getId(),
            targetTopic.getID(),
            newParentTopic.getID());

        // ----------------------------------------
        // Call changeParentOfTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeParentOfTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void moveTopicWithinSiblingOrder_ok() throws Exception {
        // Get mind map diagram and a topic with siblings
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();
        INodePresentation[] children = rootTopic.getChildren();

        assumeTrue(children.length >= 2, "Root topic needs at least two children for moveTopicWithinSiblingOrder");
        INodePresentation targetTopic = children[0];

        // Create input DTO (move to index 1)
        MoveTopicWithinSiblingOrderDTO inputDTO = new MoveTopicWithinSiblingOrderDTO(
            mindMapDiagram.getId(),
            targetTopic.getID(),
            1);

        // ----------------------------------------
        // Call moveTopicWithinSiblingOrder()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            moveTopicWithinSiblingOrder,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void insertSvgImageIntoTopic_ok() throws Exception {
        // Get mind map diagram and a topic
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();

        String svgCode = "<svg xmlns='http://www.w3.org/2000/svg' width='20' height='20'><rect width='20' height='20' fill='blue'/></svg>";

        // Create input DTO
        NewSvgImageIntoTopicDTO inputDTO = new NewSvgImageIntoTopicDTO(
            mindMapDiagram.getId(),
            rootTopic.getID(),
            svgCode);

        // ----------------------------------------
        // Call insertSvgImageIntoTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            insertSvgImageIntoTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void insertPngImageIntoTopic_ok() throws Exception {
        // Get mind map diagram and a topic
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();

        URL pngResource = getClass().getClassLoader().getResource("img/topic-image.png");
        assumeTrue(pngResource != null, "Test image img/topic-image.png must exist in src/test/resources/img/");
        String imageUrl = pngResource.toExternalForm();

        // Create input DTO
        NewPngImageIntoTopicDTO inputDTO = new NewPngImageIntoTopicDTO(
            mindMapDiagram.getId(),
            rootTopic.getID(),
            imageUrl);

        // ----------------------------------------
        // Call insertPngImageIntoTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            insertPngImageIntoTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void insertJpgImageIntoTopic_ok() throws Exception {
        // Get mind map diagram and a topic
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();

        URL jpgResource = getClass().getClassLoader().getResource("img/topic-image.jpg");
        assumeTrue(jpgResource != null, "Test image img/topic-image.jpg must exist in src/test/resources/img/");
        String imageUrl = jpgResource.toExternalForm();

        // Create input DTO
        NewJpgImageIntoTopicDTO inputDTO = new NewJpgImageIntoTopicDTO(
            mindMapDiagram.getId(),
            rootTopic.getID(),
            imageUrl);

        // ----------------------------------------
        // Call insertJpgImageIntoTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            insertJpgImageIntoTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void deleteChildTopics_ok() throws Exception {
        // Get mind map diagram and a topic (use root - deleteChildren on empty is no-op)
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();

        // Create input DTO
        DeleteChildTopicsDTO inputDTO = new DeleteChildTopicsDTO(
            mindMapDiagram.getId(),
            rootTopic.getID());

        // ----------------------------------------
        // Call deleteChildTopics()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            deleteChildTopics,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void deleteImageFromTopic_ok() throws Exception {
        // Get mind map diagram and root topic
        // First insert an image, then delete it
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();

        // Create input DTO for delete
        DeleteImageFromTopicDTO inputDTO = new DeleteImageFromTopicDTO(
            mindMapDiagram.getId(),
            rootTopic.getID());

        // ----------------------------------------
        // Call deleteImageFromTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            deleteImageFromTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setBoundaryOfTopic_ok() throws Exception {
        // Get mind map diagram and a topic
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElement(
            IMindMapDiagram.class,
            "Mindmap0");
        INodePresentation rootTopic = mindMapDiagram.getRoot();

        // Create input DTO
        TopicWithBoundaryVisibilityDTO inputDTO = new TopicWithBoundaryVisibilityDTO(
            mindMapDiagram.getId(),
            rootTopic.getID(),
            true);

        // ----------------------------------------
        // Call setBoundaryOfTopic()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setBoundaryOfTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
