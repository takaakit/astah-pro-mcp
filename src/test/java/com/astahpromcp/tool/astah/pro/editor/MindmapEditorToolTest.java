package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.common.ImageConvertSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.MindmapEditor;
import com.change_vision.jude.api.inf.model.IMindMapDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

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
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/editor/MindmapEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        MindmapEditor mindmapEditor = projectAccessor.getDiagramEditorFactory().getMindmapEditor();
        ImageConvertSupport imageConvertSupport = new ImageConvertSupport();
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createSmallImageContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new MindmapEditorTool(
            projectAccessor,
            transactionSupport,
            mindmapEditor,
            astahProToolSupport,
            imageConvertSupport,
            imageCaptureSupport,
            true);

        // createMindmapDiagram() method
        createMindmapDiagram = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createMindmapDiagram",
            NewDiagramInPackageDTO.class);

        // changeToFloatingTopic() method
        changeToFloatingTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "changeToFloatingTopic",
            ChangeToFloatingTopicDTO.class);

        // createFloatingTopic() method
        createFloatingTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createFloatingTopic",
            NewFloatingTopicDTO.class);

        // createTopic() method
        createTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createTopic",
            NewTopicDTO.class);

        // createTopicLink() method
        createTopicLink = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "createTopicLink",
            NewLinkBetweenTopicsDTO.class);

        // changeParentOfTopic() method
        changeParentOfTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "changeParentOfTopic",
            ChangeParentOfTopicDTO.class);

        // moveTopicWithinSiblingOrder() method
        moveTopicWithinSiblingOrder = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "moveTopicWithinSiblingOrder",
            MoveTopicWithinSiblingOrderDTO.class);

        // insertSvgImageIntoTopic() method
        insertSvgImageIntoTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "insertSvgImageIntoTopic",
            NewSvgImageIntoTopicDTO.class);

        // insertPngImageIntoTopic() method
        insertPngImageIntoTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "insertPngImageIntoTopic",
            NewPngImageIntoTopicDTO.class);

        // insertJpgImageIntoTopic() method
        insertJpgImageIntoTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "insertJpgImageIntoTopic",
            NewJpgImageIntoTopicDTO.class);

        // deleteChildTopics() method
        deleteChildTopics = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "deleteChildTopics",
            DeleteChildTopicsDTO.class);

        // deleteImageFromTopic() method
        deleteImageFromTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "deleteImageFromTopic",
            DeleteImageFromTopicDTO.class);

        // setBoundaryOfTopic() method
        setBoundaryOfTopic = TestSupport.getAccessibleMethod(
            MindmapEditorTool.class,
            "setBoundaryOfTopic",
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
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");

        // Create input DTO
        NewDiagramInPackageDTO inputDTO = new NewDiagramInPackageDTO(
            astahPackage.getId(),
            "Test Mind Map Diagram");

        // ----------------------------------------
        // Call createMindmapDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
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
        IMindMapDiagram mindMapDiagram = (IMindMapDiagram) TestSupport.instance().getNamedElementByClassAndName(
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
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setBoundaryOfTopic,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
