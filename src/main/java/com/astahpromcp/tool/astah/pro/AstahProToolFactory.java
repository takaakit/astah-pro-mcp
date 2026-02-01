package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.editor.*;
import com.astahpromcp.tool.astah.pro.guide.*;
import com.astahpromcp.tool.astah.pro.image.*;
import com.astahpromcp.tool.astah.pro.model.*;
import com.astahpromcp.tool.astah.pro.presentation.*;
import com.astahpromcp.tool.astah.pro.project.*;
import com.astahpromcp.tool.astah.pro.view.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Factory for creating Astah Pro tools
@Slf4j
public class AstahProToolFactory {

    private final Path imageOutputDir;

    public AstahProToolFactory() {
        this(McpServerConfig.WORKSPACE_DIR.resolve("diagram-images"));
    }

    public AstahProToolFactory(Path imageOutputDir) {
        this.imageOutputDir = imageOutputDir;
    }

    public List<ToolProvider> createToolProviders(ToolCategoryFlags categoryFlags, boolean includeEditorTools) {
        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            ProjectAccessor projectAccessor = api.getProjectAccessor();
            ITransactionManager transactionManager = projectAccessor.getTransactionManager();
            IModelEditorFactory modelEditorFactory = projectAccessor.getModelEditorFactory();
            BasicModelEditor basicModelEditor = modelEditorFactory.getBasicModelEditor();
            IViewManager viewManager = projectAccessor.getViewManager();
            IDiagramViewManager diagramViewManager = viewManager.getDiagramViewManager();
            IProjectViewManager projectViewManager = viewManager.getProjectViewManager();
            IDiagramEditorFactory diagramEditorFactory = projectAccessor.getDiagramEditorFactory();
            ClassDiagramEditor classDiagramEditor = diagramEditorFactory.getClassDiagramEditor();
            SequenceDiagramEditor sequenceDiagramEditor = diagramEditorFactory.getSequenceDiagramEditor();
            ActivityDiagramEditor activityDiagramEditor = diagramEditorFactory.getActivityDiagramEditor();
            StateMachineDiagramEditor stateMachineDiagramEditor = diagramEditorFactory.getStateMachineDiagramEditor();
            RequirementDiagramEditor requirementDiagramEditor = diagramEditorFactory.getRequirementDiagramEditor();
            UseCaseDiagramEditor useCaseDiagramEditor = diagramEditorFactory.getUseCaseDiagramEditor();
            CompositeStructureDiagramEditor compositeStructureDiagramEditor = diagramEditorFactory.getCompositeStructureDiagramEditor();
            UseCaseModelEditor useCaseModelEditor = modelEditorFactory.getUseCaseModelEditor();
            AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
            DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);
            ERModelEditor erModelEditor = modelEditorFactory.getERModelEditor();
            ERDiagramEditor erDiagramEditor = diagramEditorFactory.getERDiagramEditor();

            List<ToolProvider> providers = new ArrayList<>();
            
            // Common tools
            providers.add(new AstahProMcpGuideTool(projectAccessor));
            providers.add(new DiagramViewManagerTool(projectAccessor, diagramViewManager, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new ProjectViewManagerTool(projectAccessor, projectViewManager, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new BasicDiagramEditorTool(projectAccessor, transactionManager, astahProToolSupport, diagramEditorSupport, includeEditorTools));
            providers.add(new BasicModelEditorTool(basicModelEditor, projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new DiagramEditorTool(projectAccessor, transactionManager, astahProToolSupport, diagramEditorSupport, includeEditorTools));
            providers.add(new StructureDiagramEditorTool(projectAccessor, transactionManager, astahProToolSupport, diagramEditorSupport, includeEditorTools));
            providers.add(new CommentTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new DiagramTool(projectAccessor, transactionManager, astahProToolSupport, imageOutputDir, includeEditorTools));
            providers.add(new ElementTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new ConstraintTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new LinkPresentationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new NodePresentationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new PresentationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            providers.add(new ProjectAccessorTool(projectAccessor, astahProToolSupport, includeEditorTools));
            providers.add(new ProjectInfoTool(projectAccessor, astahProToolSupport, includeEditorTools));
            providers.add(new ImageCaptureTool(astahProToolSupport, imageOutputDir));

            // Activity diagram tools
            if (categoryFlags.activityDiagramEnabled()) {
                providers.add(new ActivityDiagramGuideTool());
                providers.add(new ActivityDiagramEditorTool(projectAccessor, transactionManager, activityDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new ActionTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ActivityDiagramTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ActivityNodeTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ActivityTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ControlNodeTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new FlowTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ObjectNodeTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new PartitionTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            }

            // Class diagram tools
            if (categoryFlags.classDiagramEnabled()) {
                providers.add(new ClassDiagramGuideTool());
                providers.add(new ClassDiagramEditorTool(projectAccessor, transactionManager, classDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new AssociationClassTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new AssociationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new AttributeTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ClassTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new DependencyTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new EnumerationLiteralTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new EnumerationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new GeneralizationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new InstanceSpecificationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new NamedElementTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new OperationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new PackageTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ParameterTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new RealizationTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new SlotTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new UsageTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            }

            // Sequence diagram tools
            if (categoryFlags.sequenceDiagramEnabled()) {
                providers.add(new SequenceDiagramGuideTool());
                providers.add(new SequenceDiagramEditorTool(projectAccessor, transactionManager, sequenceDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new CombinedFragmentTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new GateTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new InteractionOperandTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new InteractionTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new InteractionUseTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new LifelineTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new LinkEndTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new LinkTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new MessageTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            }

            // State machine diagram tools
            if (categoryFlags.stateMachineDiagramEnabled()) {
                providers.add(new StateMachineDiagramGuideTool());
                providers.add(new StateMachineDiagramEditorTool(projectAccessor, transactionManager, stateMachineDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new StateMachineDiagramTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new StateMachineTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new StateTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new TransitionTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new VertexTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new PseudostateTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            }

            // Use case diagram tools
            if (categoryFlags.useCaseDiagramEnabled()) {
                providers.add(new UseCaseDiagramGuideTool());
                providers.add(new UseCaseDiagramEditorTool(projectAccessor, transactionManager, useCaseDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new UseCaseModelEditorTool(projectAccessor, transactionManager, useCaseModelEditor, astahProToolSupport, includeEditorTools));
                providers.add(new UseCaseTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new IncludeTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new ExtendTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            }

            // Requirement diagram tools
            if (categoryFlags.requirementDiagramEnabled()) {
                providers.add(new RequirementDiagramGuideTool());
                providers.add(new RequirementDiagramEditorTool(projectAccessor, transactionManager, requirementDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new RequirementTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new TestCaseTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            }

            // Communication diagram tools
            if (categoryFlags.communicationDiagramEnabled()) {
                providers.add(new CommunicationDiagramGuideTool());
                providers.add(new CommunicationDiagramTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
                providers.add(new LifelineLinkTool(projectAccessor, transactionManager, astahProToolSupport, includeEditorTools));
            }

            // Composite structure diagram tools
            if (categoryFlags.compositeStructureDiagramEnabled()) {
                providers.add(new CompositeStructureDiagramGuideTool());

                // Note: The editing APIs for the Composite Structure Diagram don't work as expected right now, so specify that no editing tools should be included.
                providers.add(new CompositeStructureModelEditorTool(basicModelEditor, projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new CompositeStructureDiagramEditorTool(projectAccessor, transactionManager, compositeStructureDiagramEditor, astahProToolSupport, false));
                providers.add(new ConnectorTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new PortTool(projectAccessor, transactionManager, astahProToolSupport, false));
            }

            // ER diagram tools
            if (categoryFlags.erDiagramEnabled()) {
                providers.add(new ERDiagramGuideTool());

                // Note: Only expose the query tools for ER diagrams, because there are too many editing tools and exposing them may affect existing tools.
                providers.add(new ERModelEditorTool(erModelEditor, projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERDiagramEditorTool(projectAccessor, transactionManager, erDiagramEditor, astahProToolSupport, false));
                providers.add(new ERDiagramTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERModelTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERSchemaTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERPackageTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new EREntityTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERAttributeTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERDatatypeTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERIndexTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERRelationshipTool(projectAccessor, transactionManager, astahProToolSupport, false));
                providers.add(new ERSubtypeRelationshipTool(projectAccessor, transactionManager, astahProToolSupport, false));
            }

            return providers;
        
        } catch (ClassNotFoundException e) {
            log.error("Failed to get Astah Pro API classes: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Failed to create Astah Pro tools", e);
            return List.of();
        }
    }
}
