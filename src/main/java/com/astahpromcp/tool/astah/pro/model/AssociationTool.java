package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.AssociationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.AssociationDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IAssociation.html
@Slf4j
public class AssociationTool implements ToolProvider {
    
    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public AssociationTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_asso_info",
                            "Return detailed information about the specified association (specified by ID).",
                            this::getAssociationInfo,
                            IdDTO.class,
                            AssociationDTO.class),

                    ToolSupport.definition(
                            "get_asso_end_a_info",
                            "Return detailed information about the specified association end A (specified by ID). Note that the input to this tool function is the association-end ID, not the association ID.",
                            this::getAssociationEndAInfo,
                            IdDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "get_asso_end_b_info",
                            "Return detailed information about the specified association end B (specified by ID). Note that the input to this tool function is the association-end ID, not the association ID.",
                            this::getAssociationEndBInfo,
                            IdDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_init_val_of_asso_end_a",
                            "Set the initial value of the specified association end A (specified by ID), and return the association end information after it is set.",
                            this::setInitialValueOfAssociationEndA,
                            AttributeWithInitialValueDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_init_val_of_asso_end_b",
                            "Set the initial value of the specified association end B (specified by ID), and return the association end information after it is set.",
                            this::setInitialValueOfAssociationEndB,
                            AttributeWithInitialValueDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_static_of_asso_end_a",
                            "Set the Static of the specified association end A (specified by ID), and return the association end information after it is set.",
                            this::setStaticOfAssociationEndA,
                            AttributeWithStaticDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_static_of_asso_end_b",
                            "Set the Static of the specified association end A (specified by ID), and return the association end information after it is set.",
                            this::setStaticOfAssociationEndB,
                            AttributeWithStaticDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_multi_of_asso_end_a_by_int",
                            "Set the upper and lower multiplicity (specified by integer) of the specified association end A (specified by ID), and return the association end information after it is set. If you want to set '*', set this value to '-1'. If you want to set undefined, set this value to '-100'. If the upper value and the lower value are the same, set the same value for both.",
                            this::setMultiplicityByIntOfAssociationEndA,
                            AttributeWithIntMultiplicityDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_multi_of_asso_end_b_by_int",
                            "Set the upper and lower multiplicity (specified by integer) of the specified association end B (specified by ID), and return the association end information after it is set. If you want to set '*', set this value to '-1'. If you want to set undefined, set this value to '-100'. If the upper value and the lower value are the same, set the same value for both.",
                            this::setMultiplicityByIntOfAssociationEndB,
                            AttributeWithIntMultiplicityDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_aggr_kind_of_asso_end_a",
                            "Set the aggregation kind of the specified association end A (specified by ID), and return the association end information after it is set.",
                            this::setAggregationKindOfAssociationEndA,
                            AssociationEndWithAggregationKindDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_aggr_kind_of_asso_end_b",
                            "Set the aggregation kind of the specified association end B (specified by ID), and return the association end information after it is set.",
                            this::setAggregationKindOfAssociationEndB,
                            AssociationEndWithAggregationKindDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_navi_of_asso_end_a",
                            "Set the navigability of the specified association end A (specified by ID), and return the association end information after it is set. Note that when you set the navigability of association end A to 'navigable', the arrowhead appears on the association end B side.",
                            this::setNavigabilityOfAssociationEndA,
                            AssociationEndWithNavigabilityDTO.class,
                            AttributeDTO.class),

                    ToolSupport.definition(
                            "set_navi_of_asso_end_b",
                            "Set the navigability of the specified association end B (specified by ID), and return the association end information after it is set. Note that when you set the navigability of association end B to 'navigable', the arrowhead appears on the association end A side.",
                            this::setNavigabilityOfAssociationEndB,
                            AssociationEndWithNavigabilityDTO.class,
                            AttributeDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create association tools", e);
            return List.of();
        }
    }

    private AssociationDTO getAssociationInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get association information: {}", param);
        
        IAssociation astahAssociation = astahProToolSupport.getAssociation(param.id());

        return AssociationDTOAssembler.toDTO(astahAssociation);
    }

    private AttributeDTO getAssociationEndAInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get association end A information: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.id());

        return AttributeDTOAssembler.toDTO(astahAssociationEndA);
    }

    private AttributeDTO getAssociationEndBInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get association end B information: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.id());

        return AttributeDTOAssembler.toDTO(astahAssociationEndB);
    }

    private AttributeDTO setInitialValueOfAssociationEndA(McpSyncServerExchange exchange, AttributeWithInitialValueDTO param) throws Exception {
        log.debug("Set initial value of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndA.setInitialValue(param.initialValue());
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAssociationEndA);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private AttributeDTO setInitialValueOfAssociationEndB(McpSyncServerExchange exchange, AttributeWithInitialValueDTO param) throws Exception {
        log.debug("Set initial value of association end B: {}", param);
        
        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndB.setInitialValue(param.initialValue());
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAssociationEndB);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private AttributeDTO setStaticOfAssociationEndA(McpSyncServerExchange exchange, AttributeWithStaticDTO param) throws Exception {
        log.debug("Set static of association end A: {}", param);
        
        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndA.setStatic(param.isStatic());
            transactionManager.endTransaction();
            
            return AttributeDTOAssembler.toDTO(astahAssociationEndA);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private AttributeDTO setStaticOfAssociationEndB(McpSyncServerExchange exchange, AttributeWithStaticDTO param) throws Exception {
        log.debug("Set static of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndB.setStatic(param.isStatic());
            transactionManager.endTransaction();
            
            return AttributeDTOAssembler.toDTO(astahAssociationEndB);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO setMultiplicityByIntOfAssociationEndA(McpSyncServerExchange exchange, AttributeWithIntMultiplicityDTO param) throws Exception {
        log.debug("Set multiplicity by int of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndA.setMultiplicity(new int[][]{{param.lowerIntValue(), param.upperIntValue()}});
            transactionManager.endTransaction();
            
            return AttributeDTOAssembler.toDTO(astahAssociationEndA);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO setMultiplicityByIntOfAssociationEndB(McpSyncServerExchange exchange, AttributeWithIntMultiplicityDTO param) throws Exception {
        log.debug("Set multiplicity by int of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndB.setMultiplicity(new int[][]{{param.lowerIntValue(), param.upperIntValue()}});
            transactionManager.endTransaction();
            
            return AttributeDTOAssembler.toDTO(astahAssociationEndB);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO setAggregationKindOfAssociationEndA(McpSyncServerExchange exchange, AssociationEndWithAggregationKindDTO param) throws Exception {
        log.debug("Set aggregation kind of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndA.setAggregationKind(param.aggregationKind().toAstahValue());
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAssociationEndA);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private AttributeDTO setAggregationKindOfAssociationEndB(McpSyncServerExchange exchange, AssociationEndWithAggregationKindDTO param) throws Exception {
        log.debug("Set aggregation kind of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndB.setAggregationKind(param.aggregationKind().toAstahValue());
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(astahAssociationEndB);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private AttributeDTO setNavigabilityOfAssociationEndA(McpSyncServerExchange exchange, AssociationEndWithNavigabilityDTO param) throws Exception {
        log.debug("Set navigability of association end A: {}", param);
        
        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndA.setNavigability(param.navigabilityKind().toAstahValue());
            transactionManager.endTransaction();
            
            return AttributeDTOAssembler.toDTO(astahAssociationEndA);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private AttributeDTO setNavigabilityOfAssociationEndB(McpSyncServerExchange exchange, AssociationEndWithNavigabilityDTO param) throws Exception {
        log.debug("Set navigability of association end B: {}", param);
        
        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        try {
            transactionManager.beginTransaction();
            astahAssociationEndB.setNavigability(param.navigabilityKind().toAstahValue());
            transactionManager.endTransaction();
            
            return AttributeDTOAssembler.toDTO(astahAssociationEndB);
            
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
