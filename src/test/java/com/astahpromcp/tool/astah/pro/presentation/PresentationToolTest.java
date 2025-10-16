package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithColorDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithLabelDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class PresentationToolTest {

    private ProjectAccessor projectAccessor;
    private PresentationTool tool;
    private Method getElement;
    private Method setLabel;
    private Method changeFillColor;
    private Method changeLineColor;
    private Method changeFontColor;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/presentation/PresentationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new PresentationTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getElement() method
        getElement = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "getElement",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setLabel() method
        setLabel = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "setLabel",
            McpSyncServerExchange.class,
            PresentationWithLabelDTO.class);

        // changeFillColor() method
        changeFillColor = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "changeFillColor",
            McpSyncServerExchange.class,
            PresentationWithColorDTO.class);

        // changeLineColor() method
        changeLineColor = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "changeLineColor",
            McpSyncServerExchange.class,
            PresentationWithColorDTO.class);

        // changeFontColor() method
        changeFontColor = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "changeFontColor",
            McpSyncServerExchange.class,
            PresentationWithColorDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getElement_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(presentation.getID());

        // ----------------------------------------
        // Call getElement()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getElement,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setLabel_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        PresentationWithLabelDTO inputDTO = new PresentationWithLabelDTO(
            presentation.getID(),
            "UpdatedFoo");

        // Check label before setting
        assertNotEquals("UpdatedFoo", presentation.getLabel());

        // ----------------------------------------
        // Call setLabel()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLabel,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("UpdatedFoo", outputDTO.label());

        // Check presentation after setting
        assertEquals("UpdatedFoo", presentation.getLabel());
    }

    @Test
    void changeFillColor_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            presentation.getID(),
            "#FF0000");

        // Check fill color before setting
        assertNotEquals("#FF0000", presentation.getProperty(Key.FILL_COLOR));

        // ----------------------------------------
        // Call changeFillColor()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeFillColor,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("#FF0000", outputDTO.fillColor());

        // Check presentation after setting
        assertEquals("#FF0000", presentation.getProperty(Key.FILL_COLOR));
    }

    @Test
    void changeLineColor_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            presentation.getID(),
            "#00FF00");

        // ----------------------------------------
        // Call changeLineColor()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeLineColor,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("#00FF00", outputDTO.lineColor());

        // Check presentation after setting
        assertEquals("#00FF00", presentation.getProperty(Key.LINE_COLOR));
    }

    @Test
    void changeFontColor_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentation(
            "Class",
            "Foo");
        
        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            presentation.getID(),
            "#0000FF");

        // ----------------------------------------
        // Call changeFontColor()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeFontColor,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("#0000FF", outputDTO.fontColor());

        // Check presentation after setting
        assertEquals("#0000FF", presentation.getProperty(Key.FONT_COLOR));
    }
}
