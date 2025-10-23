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
        this(McpServerConfig.DEFAULT_WORKSPACE_DIR.resolve("diagram-images"));
    }

    public AstahProToolFactory(Path imageOutputDir) {
        this.imageOutputDir = imageOutputDir;
    }

    public List<ToolProvider> createToolProviders(ToolCategoryFlags categoryFlags) {
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
            AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
            DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);

            List<ToolProvider> providers = new ArrayList<>();
            
            // Common tools
            providers.add(new AstahProMcpGuideTool());
            providers.add(new DiagramViewManagerTool(projectAccessor, diagramViewManager, transactionManager, astahProToolSupport));
            providers.add(new ProjectViewManagerTool(projectAccessor, projectViewManager, transactionManager, astahProToolSupport));
            providers.add(new BasicDiagramEditorTool(projectAccessor, transactionManager, astahProToolSupport, diagramEditorSupport));
            providers.add(new BasicModelEditorTool(basicModelEditor, projectAccessor, transactionManager, astahProToolSupport));
            providers.add(new DiagramEditorTool(projectAccessor, transactionManager, astahProToolSupport, diagramEditorSupport));
            providers.add(new StructureDiagramEditorTool(projectAccessor, transactionManager, astahProToolSupport, diagramEditorSupport));
            providers.add(new CommentTool(projectAccessor, transactionManager, astahProToolSupport));
            providers.add(new DiagramTool(projectAccessor, transactionManager, astahProToolSupport, imageOutputDir));
            providers.add(new ElementTool(projectAccessor, transactionManager, astahProToolSupport));
            providers.add(new LinkPresentationTool(projectAccessor, transactionManager, astahProToolSupport));
            providers.add(new NodePresentationTool(projectAccessor, transactionManager, astahProToolSupport));
            providers.add(new PresentationTool(projectAccessor, transactionManager, astahProToolSupport));
            providers.add(new ProjectAccessorTool(projectAccessor, astahProToolSupport));
            providers.add(new ProjectInfoTool(projectAccessor, astahProToolSupport));
            providers.add(new ImageCaptureTool(astahProToolSupport, imageOutputDir));

            // Activity diagram tools
            if (categoryFlags.isActivityDiagramEnabled()) {
                providers.add(new ActivityDiagramGuideTool());
                providers.add(new ActivityDiagramEditorTool(projectAccessor, transactionManager, activityDiagramEditor, astahProToolSupport));
                providers.add(new ActionTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ActivityDiagramTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ActivityNodeTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ActivityTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ControlNodeTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new FlowTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ObjectNodeTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new PartitionTool(projectAccessor, transactionManager, astahProToolSupport));
            }

            // Class diagram tools
            if (categoryFlags.isClassDiagramEnabled()) {
                providers.add(new ClassDiagramGuideTool());
                providers.add(new ClassDiagramEditorTool(projectAccessor, transactionManager, classDiagramEditor, astahProToolSupport));
                providers.add(new AssociationClassTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new AssociationTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new AttributeTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ClassTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new DependencyTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new EnumerationLiteralTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new EnumerationTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new GeneralizationTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new InstanceSpecificationTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new NamedElementTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new OperationTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new PackageTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ParameterTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new RealizationTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new SlotTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new UsageTool(projectAccessor, transactionManager, astahProToolSupport));
            }

            // Sequence diagram tools
            if (categoryFlags.isSequenceDiagramEnabled()) {
                providers.add(new SequenceDiagramGuideTool());
                providers.add(new SequenceDiagramEditorTool(projectAccessor, transactionManager, sequenceDiagramEditor, astahProToolSupport));
                providers.add(new CombinedFragmentTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new GateTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new InteractionOperandTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new InteractionTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new InteractionUseTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new LifelineTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new LinkEndTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new LinkTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new MessageTool(projectAccessor, transactionManager, astahProToolSupport));
            }

            // State machine diagram tools
            if (categoryFlags.isStateMachineDiagramEnabled()) {
                providers.add(new StateMachineDiagramGuideTool());
                providers.add(new StateMachineDiagramEditorTool(projectAccessor, transactionManager, stateMachineDiagramEditor, astahProToolSupport));
                providers.add(new StateMachineDiagramTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new StateMachineTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new TransitionTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new VertexTool(projectAccessor, transactionManager, astahProToolSupport));
            }

            // Use case diagram tools
            if (categoryFlags.isUseCaseDiagramEnabled()) {
                providers.add(new UseCaseDiagramGuideTool());
                providers.add(new UseCaseDiagramEditorTool(projectAccessor, transactionManager, useCaseDiagramEditor, astahProToolSupport));
                providers.add(new UseCaseTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new IncludeTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new ExtendTool(projectAccessor, transactionManager, astahProToolSupport));
            }

            // Requirement diagram tools
            if (categoryFlags.isRequirementDiagramEnabled()) {
                providers.add(new RequirementDiagramGuideTool());
                providers.add(new RequirementDiagramEditorTool(projectAccessor, transactionManager, requirementDiagramEditor, astahProToolSupport));
                providers.add(new RequirementTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new TestCaseTool(projectAccessor, transactionManager, astahProToolSupport));
            }

            // Communication diagram tools
            if (categoryFlags.isCommunicationDiagramEnabled()) {
                providers.add(new CommunicationDiagramGuideTool());
                providers.add(new CommunicationDiagramTool(projectAccessor, transactionManager, astahProToolSupport));
                providers.add(new LifelineLinkTool(projectAccessor, transactionManager, astahProToolSupport));
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
