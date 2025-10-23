package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.PointIntDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.LinkPresentationWithPointsDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/ILinkPresentation.html
@Slf4j
public class LinkPresentationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public LinkPresentationTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_link_prst_info",
                            "Return detailed information about the specified link presentation (specified by ID).",
                            this::getInfo,
                            IdDTO.class,
                            LinkPresentationDTO.class),

                    ToolSupport.definition(
                            "set_points_of_link_prst",
                            "Set all points with the connection points in the rectangles of the specified link presentation (specified by ID), and return the link presentation information after it is set. Note that it must include the connection points with the rectangle (node presentation).",
                            this::setAllPoints,
                            LinkPresentationWithPointsDTO.class,
                            LinkPresentationDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create link presentation tools", e);
            return List.of();
        }
    }

    private LinkPresentationDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get link presentation information: {}", param);
        
        ILinkPresentation linkPresentation = astahProToolSupport.getLinkPresentation(param.id());
        
        return LinkPresentationDTOAssembler.toDTO(linkPresentation);
    }

    private LinkPresentationDTO setAllPoints(McpSyncServerExchange exchange, LinkPresentationWithPointsDTO param) throws Exception {
        log.debug("Set points of link presentation: {}", param);
        
        ILinkPresentation linkPresentation = astahProToolSupport.getLinkPresentation(param.targetLinkPresentationId());

		Point2D.Double[] pointArray = new Point2D.Double[param.drawPoints().size()];
		for (int i = 0; i < pointArray.length; i++) {
			PointIntDTO point = param.drawPoints().get(i);
			pointArray[i] = new Point2D.Double(point.x(), point.y());
		}

		try {
            transactionManager.beginTransaction();
			linkPresentation.setAllPoints(pointArray);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(linkPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
