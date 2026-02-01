package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithAlignAttributeItemsDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithInitialDisplayLevelDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithModelTypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithNotationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDiagramDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ERDiagramToolTest {

    private ProjectAccessor projectAccessor;
    private ERDiagramTool tool;
    private Method getInfo;
    private Method setAlignAttributeItems;
    private Method setInitialDisplayLevel;
    private Method setModelType;
    private Method setNotation;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ERDiagramToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERDiagramTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ERDiagramTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setAlignAttributeItems() method
        setAlignAttributeItems = TestSupport.getAccessibleMethod(
            ERDiagramTool.class,
            "setAlignAttributeItems",
            McpSyncServerExchange.class,
            ERDiagramWithAlignAttributeItemsDTO.class);

        // setInitialDisplayLevel() method
        setInitialDisplayLevel = TestSupport.getAccessibleMethod(
            ERDiagramTool.class,
            "setInitialDisplayLevel",
            McpSyncServerExchange.class,
            ERDiagramWithInitialDisplayLevelDTO.class);

        // setModelType() method
        setModelType = TestSupport.getAccessibleMethod(
            ERDiagramTool.class,
            "setModelType",
            McpSyncServerExchange.class,
            ERDiagramWithModelTypeDTO.class);

        // setNotation() method
        setNotation = TestSupport.getAccessibleMethod(
            ERDiagramTool.class,
            "setNotation",
            McpSyncServerExchange.class,
            ERDiagramWithNotationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erDiagram.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ERDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ERDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erDiagram.getId(), outputDTO.diagram().namedElement().element().id());
        assertEquals(erDiagram.getName(), outputDTO.diagram().namedElement().name());
    }

    @Test
    void setAlignAttributeItems_ok() throws Exception {
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");
        boolean nextValue = !erDiagram.isAlignAttributeItems();

        // Create input DTO
        ERDiagramWithAlignAttributeItemsDTO inputDTO = new ERDiagramWithAlignAttributeItemsDTO(
            erDiagram.getId(),
            nextValue);

        // ----------------------------------------
        // Call setAlignAttributeItems()
        // ----------------------------------------
        ERDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAlignAttributeItems,
            tool,
            inputDTO,
            ERDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erDiagram.isAlignAttributeItems());
    }

    @Test
    void setInitialDisplayLevel_ok() throws Exception {
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");

        // Create input DTO
        ERDiagramWithInitialDisplayLevelDTO inputDTO = new ERDiagramWithInitialDisplayLevelDTO(
            erDiagram.getId(),
            "Attribute");

        // ----------------------------------------
        // Call setInitialDisplayLevel()
        // ----------------------------------------
        ERDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setInitialDisplayLevel,
            tool,
            inputDTO,
            ERDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Attribute", erDiagram.getInitialDisplayLevel());
    }

    @Test
    void setModelType_ok() throws Exception {
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");

        // Create input DTO
        ERDiagramWithModelTypeDTO inputDTO = new ERDiagramWithModelTypeDTO(
            erDiagram.getId(),
            "Physical Model");

        // ----------------------------------------
        // Call setModelType()
        // ----------------------------------------
        ERDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setModelType,
            tool,
            inputDTO,
            ERDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Physical Model", erDiagram.getModelType());
    }

    @Test
    void setNotation_ok() throws Exception {
        // Get ER diagram
        IERDiagram erDiagram = (IERDiagram) TestSupport.instance().getNamedElement(
            IERDiagram.class,
            "ER Diagram0");

        // Create input DTO
        ERDiagramWithNotationDTO inputDTO = new ERDiagramWithNotationDTO(
            erDiagram.getId(),
            "IE");

        // ----------------------------------------
        // Call setNotation()
        // ----------------------------------------
        ERDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNotation,
            tool,
            inputDTO,
            ERDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("IE", erDiagram.getNotation());
    }
}
