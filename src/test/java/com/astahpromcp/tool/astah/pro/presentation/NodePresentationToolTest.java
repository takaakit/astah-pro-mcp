package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithHeightDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithLocationDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithWidthDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class NodePresentationToolTest {

    private ProjectAccessor projectAccessor;
    private NodePresentationTool tool;
    private Method getInfo;
    private Method getNodePresentationRectangle;
    private Method setNodePresentationLocation;
    private Method setNodePresentationWidth;
    private Method setNodePresentationHeight;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/presentation/NodePresentationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new NodePresentationTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // getNodePresentationRectangle() method
        getNodePresentationRectangle = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "getNodePresentationRectangle",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setNodePresentationLocation() method
        setNodePresentationLocation = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "setNodePresentationLocation",
            McpSyncServerExchange.class,
            NodePresentationWithLocationDTO.class);

        // setNodePresentationWidth() method
        setNodePresentationWidth = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "setNodePresentationWidth",
            McpSyncServerExchange.class,
            NodePresentationWithWidthDTO.class);

        // setNodePresentationHeight() method
        setNodePresentationHeight = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "setNodePresentationHeight",
            McpSyncServerExchange.class,
            NodePresentationWithHeightDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(nodePresentation.getID());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nodePresentation.getID(), outputDTO.presentation().id());
    }

    @Test
    void getNodePresentationRectangle_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(nodePresentation.getID());

        // ----------------------------------------
        // Call getNodePresentationRectangle()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getNodePresentationRectangle,
            tool,
            inputDTO,
            RectangleDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.width() > 0);
        assertTrue(outputDTO.height() > 0);
    }

    @Test
    void setNodePresentationLocation_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        NodePresentationWithLocationDTO inputDTO = new NodePresentationWithLocationDTO(
            nodePresentation.getID(),
            100,
            200);
        
        // Check points before setting
        assertNotEquals(nodePresentation.getLocation().getX(), 100, 0.1);
        assertNotEquals(nodePresentation.getLocation().getY(), 200, 0.1);

        // ----------------------------------------
        // Call setNodePresentationLocation()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNodePresentationLocation,
            tool,
            inputDTO,
            RectangleDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(100, outputDTO.x(), 0.1);
        assertEquals(200, outputDTO.y(), 0.1);

        // Check points after setting
        assertEquals(nodePresentation.getLocation().getX(), 100, 0.1);
        assertEquals(nodePresentation.getLocation().getY(), 200, 0.1);
    }

    @Test
    void setNodePresentationWidth_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        NodePresentationWithWidthDTO inputDTO = new NodePresentationWithWidthDTO(
            nodePresentation.getID(),
            150);
        
        // Check width before setting
        assertNotEquals(nodePresentation.getWidth(), 150, 0.1);

        // ----------------------------------------
        // Call setNodePresentationWidth()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNodePresentationWidth,
            tool,
            inputDTO,
            RectangleDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(150, outputDTO.width(), 0.1);

        // Check width after setting
        assertEquals(nodePresentation.getWidth(), 150, 0.1);
    }

    @Test
    void setNodePresentationHeight_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        NodePresentationWithHeightDTO inputDTO = new NodePresentationWithHeightDTO(
            nodePresentation.getID(),
            100);

        // Check height before setting
        assertNotEquals(nodePresentation.getHeight(), 100, 0.1);

        // ----------------------------------------
        // Call setNodePresentationHeight()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNodePresentationHeight,
            tool,
            inputDTO,
            RectangleDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(100, outputDTO.height(), 0.1);

        // Check height after setting
        assertEquals(nodePresentation.getHeight(), 100, 0.1);
    }
}
