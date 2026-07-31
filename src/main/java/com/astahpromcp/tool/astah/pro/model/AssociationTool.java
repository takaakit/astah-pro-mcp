package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.AssociationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.AssociationDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.AttributeDTOAssembler;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IAssociation.html
@Slf4j
public class AssociationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public AssociationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create association tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_asso_info",
                "Return the model element information about the specified association (specified by ID).",
                this::getAssociationInfo,
                IdDTO.class,
                AssociationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_asso_end_a_info",
                "Return the model element information about the specified association end A (specified by ID). Note that the input to this tool function is the association-end ID, not the association ID.",
                this::getAssociationEndAInfo,
                IdDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_asso_end_b_info",
                "Return the model element information about the specified association end B (specified by ID). Note that the input to this tool function is the association-end ID, not the association ID.",
                this::getAssociationEndBInfo,
                IdDTO.class,
                AttributeDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_init_val_of_asso_end_a",
                "Set the initial value of the specified association end A (specified by ID), and return the model element of the association end after it is set.",
                this::setInitialValueOfAssociationEndA,
                AttributeWithInitialValueDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_init_val_of_asso_end_b",
                "Set the initial value of the specified association end B (specified by ID), and return the model element of the association end after it is set.",
                this::setInitialValueOfAssociationEndB,
                AttributeWithInitialValueDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_static_of_asso_end_a",
                "Set the Static of the specified association end A (specified by ID), and return the model element of the association end after it is set.",
                this::setStaticOfAssociationEndA,
                AttributeWithStaticDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_static_of_asso_end_b",
                "Set the Static of the specified association end B (specified by ID), and return the model element of the association end after it is set.",
                this::setStaticOfAssociationEndB,
                AttributeWithStaticDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_multiplicity_of_asso_end_a",
                "Set the upper and lower multiplicity (specified by string) of the specified association end A (specified by ID), and return the model element of the association end after it is set. If there is only one multiplicity, set either the upper or the lower multiplicity, and set the other to an empty string.",
                this::setMultiplicityOfAssociationEndA,
                AttributeWithMultiplicityDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_multiplicity_of_asso_end_b",
                "Set the upper and lower multiplicity (specified by string) of the specified association end B (specified by ID), and return the model element of the association end after it is set. If there is only one multiplicity, set either the upper or the lower multiplicity, and set the other to an empty string.",
                this::setMultiplicityOfAssociationEndB,
                AttributeWithMultiplicityDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_aggregation_kind_of_asso_end_a",
                "Set the aggregation kind of the specified association end A (specified by ID), and return the model element of the association end after it is set. When 'aggregate' is specified, a hollow diamond is placed on association end A. When 'composite' is specified, a filled (black) diamond is placed on association end A.",
                this::setAggregationKindOfAssociationEndA,
                AssociationEndWithAggregationKindDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_aggregation_kind_of_asso_end_b",
                "Set the aggregation kind of the specified association end B (specified by ID), and return the model element of the association end after it is set. When 'aggregate' is specified, a hollow diamond is placed on association end B. When 'composite' is specified, a filled (black) diamond is placed on association end B.",
                this::setAggregationKindOfAssociationEndB,
                AssociationEndWithAggregationKindDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_navigability_of_asso_end_a",
                "Set the navigability of the specified association end A (specified by ID), and return the model element of the association end after it is set. Note that when you set the navigability of association end A to 'navigable', the arrowhead appears on the association end A side.",
                this::setNavigabilityOfAssociationEndA,
                AssociationEndWithNavigabilityDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_navigability_of_asso_end_b",
                "Set the navigability of the specified association end B (specified by ID), and return the model element of the association end after it is set. Note that when you set the navigability of association end B to 'navigable', the arrowhead appears on the association end B side.",
                this::setNavigabilityOfAssociationEndB,
                AssociationEndWithNavigabilityDTO.class,
                AttributeDTO.class)
        );
    }

    private AssociationDTO getAssociationInfo(IdDTO param) throws Exception {
        log.debug("Get association information: {}", param);

        IAssociation astahAssociation = astahProToolSupport.getAssociation(param.id());

        return AssociationDTOAssembler.toDTO(astahAssociation);
    }

    private AttributeDTO getAssociationEndAInfo(IdDTO param) throws Exception {
        log.debug("Get association end A information: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.id());

        return AttributeDTOAssembler.toDTO(astahAssociationEndA);
    }

    private AttributeDTO getAssociationEndBInfo(IdDTO param) throws Exception {
        log.debug("Get association end B information: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.id());

        return AttributeDTOAssembler.toDTO(astahAssociationEndB);
    }

    private AttributeDTO setInitialValueOfAssociationEndA(AttributeWithInitialValueDTO param) throws Exception {
        log.debug("Set initial value of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        txnAstah.run( () -> {
            astahAssociationEndA.setInitialValue(param.initialValue());
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndA);
    }

    private AttributeDTO setInitialValueOfAssociationEndB(AttributeWithInitialValueDTO param) throws Exception {
        log.debug("Set initial value of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        txnAstah.run( () -> {
            astahAssociationEndB.setInitialValue(param.initialValue());
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndB);
    }

    private AttributeDTO setStaticOfAssociationEndA(AttributeWithStaticDTO param) throws Exception {
        log.debug("Set static of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        txnAstah.run( () -> {
            astahAssociationEndA.setStatic(param.isStatic());
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndA);
    }

    private AttributeDTO setStaticOfAssociationEndB(AttributeWithStaticDTO param) throws Exception {
        log.debug("Set static of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        txnAstah.run( () -> {
            astahAssociationEndB.setStatic(param.isStatic());
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndB);
    }

    private AttributeDTO setMultiplicityOfAssociationEndA(AttributeWithMultiplicityDTO param) throws Exception {
        log.debug("Set multiplicity of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        String lowerStringMultiplicity = param.lowerMultiplicity();
        String upperStringMultiplicity = param.upperMultiplicity();

        if (!lowerStringMultiplicity.isEmpty() && !upperStringMultiplicity.isEmpty()) {
            txnAstah.run( () -> {
                astahAssociationEndA.setMultiplicityStrings(new String[][]{{lowerStringMultiplicity, upperStringMultiplicity}});
            });
        } else if (!lowerStringMultiplicity.isEmpty() && upperStringMultiplicity.isEmpty()) {
            txnAstah.run( () -> {
                astahAssociationEndA.setMultiplicityString(lowerStringMultiplicity);
            });
        } else if (lowerStringMultiplicity.isEmpty() && !upperStringMultiplicity.isEmpty()) {
            txnAstah.run( () -> {
                astahAssociationEndA.setMultiplicityString(upperStringMultiplicity);
            });
        } else {
            throw new IllegalArgumentException("Lower multiplicity and upper multiplicity are both empty.");
        }

        return AttributeDTOAssembler.toDTO(astahAssociationEndA);
    }

    private AttributeDTO setMultiplicityOfAssociationEndB(AttributeWithMultiplicityDTO param) throws Exception {
        log.debug("Set multiplicity of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAttributeId());

        String lowerStringMultiplicity = param.lowerMultiplicity();
        String upperStringMultiplicity = param.upperMultiplicity();

        if (!lowerStringMultiplicity.isEmpty() && !upperStringMultiplicity.isEmpty()) {
            txnAstah.run( () -> {
                astahAssociationEndB.setMultiplicityStrings(new String[][]{{lowerStringMultiplicity, upperStringMultiplicity}});
            });
        } else if (!lowerStringMultiplicity.isEmpty() && upperStringMultiplicity.isEmpty()) {
            txnAstah.run( () -> {
                astahAssociationEndB.setMultiplicityString(lowerStringMultiplicity);
            });
        } else if (lowerStringMultiplicity.isEmpty() && !upperStringMultiplicity.isEmpty()) {
            txnAstah.run( () -> {
                astahAssociationEndB.setMultiplicityString(upperStringMultiplicity);
            });
        } else {
            throw new IllegalArgumentException("Lower multiplicity and upper multiplicity are both empty.");
        }

        return AttributeDTOAssembler.toDTO(astahAssociationEndB);
    }

    private AttributeDTO setAggregationKindOfAssociationEndA(AssociationEndWithAggregationKindDTO param) throws Exception {
        log.debug("Set aggregation kind of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        txnAstah.run( () -> {
            astahAssociationEndA.setAggregationKind(param.aggregationKind().astahValue);
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndA);
    }

    private AttributeDTO setAggregationKindOfAssociationEndB(AssociationEndWithAggregationKindDTO param) throws Exception {
        log.debug("Set aggregation kind of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        txnAstah.run( () -> {
            astahAssociationEndB.setAggregationKind(param.aggregationKind().astahValue);
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndB);
    }

    private AttributeDTO setNavigabilityOfAssociationEndA(AssociationEndWithNavigabilityDTO param) throws Exception {
        log.debug("Set navigability of association end A: {}", param);

        IAttribute astahAssociationEndA = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        txnAstah.run( () -> {
            astahAssociationEndA.setNavigability(param.navigabilityKind().astahValue);
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndA);
    }

    private AttributeDTO setNavigabilityOfAssociationEndB(AssociationEndWithNavigabilityDTO param) throws Exception {
        log.debug("Set navigability of association end B: {}", param);

        IAttribute astahAssociationEndB = astahProToolSupport.getAssociationEnd(param.targetAssociationEndId());

        txnAstah.run( () -> {
            astahAssociationEndB.setNavigability(param.navigabilityKind().astahValue);
        });

        return AttributeDTOAssembler.toDTO(astahAssociationEndB);
    }
}
