package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.common.ImageConvertSupport;
import com.astahpromcp.tool.astah.pro.editor.*;
import com.astahpromcp.tool.astah.pro.guide.*;
import com.astahpromcp.tool.astah.pro.image.*;
import com.astahpromcp.tool.astah.pro.review.*;
import com.astahpromcp.tool.astah.pro.script.*;
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
            ERModelEditor erModelEditor = modelEditorFactory.getERModelEditor();
            ERDiagramEditor erDiagramEditor = diagramEditorFactory.getERDiagramEditor();
            MindmapEditor mindmapEditor = diagramEditorFactory.getMindmapEditor();
            AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
            DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);
            ImageConvertSupport imageConvertSupport = new ImageConvertSupport();
            ImageCaptureSupport imageCaptureSupport = new ImageCaptureSupport(astahProToolSupport, imageOutputDir);
            TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());

            List<ToolProvider> providers = new ArrayList<>();
            
            // Common tools
            providers.add(new AstahProMcpGuideTool(projectAccessor));
            providers.add(new DiagramLayoutGuideTool());
            providers.add(new DiagramViewManagerTool(projectAccessor, diagramViewManager, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new ProjectViewManagerTool(projectAccessor, projectViewManager, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new BasicDiagramEditorTool(projectAccessor, transactionSupport, astahProToolSupport, diagramEditorSupport, imageCaptureSupport, includeEditorTools));
            providers.add(new BasicModelEditorTool(basicModelEditor, projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new DiagramEditorTool(projectAccessor, transactionSupport, astahProToolSupport, diagramEditorSupport, imageConvertSupport, imageCaptureSupport, includeEditorTools));
            providers.add(new StructureDiagramEditorTool(projectAccessor, transactionSupport, astahProToolSupport, diagramEditorSupport, imageCaptureSupport, includeEditorTools));
            providers.add(new CommentTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new DiagramTool(projectAccessor, transactionSupport, astahProToolSupport, imageOutputDir, includeEditorTools));
            providers.add(new ElementTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new ConstraintTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new LinkPresentationTool(projectAccessor, transactionSupport, astahProToolSupport, imageCaptureSupport, includeEditorTools));
            providers.add(new NodePresentationTool(projectAccessor, transactionSupport, astahProToolSupport, imageCaptureSupport, includeEditorTools));
            providers.add(new PresentationTool(projectAccessor, transactionSupport, astahProToolSupport, imageCaptureSupport, includeEditorTools));
            providers.add(new ProjectAccessorTool(projectAccessor, astahProToolSupport, includeEditorTools));
            providers.add(new ProjectInfoTool(projectAccessor, astahProToolSupport, includeEditorTools));
            providers.add(new ImageCaptureTool(imageCaptureSupport));
            providers.add(new HyperlinkOwnerTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new DiagramLayoutLintTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            providers.add(new TerminologyConsistencyTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));

            // Activity diagram tools
            if (categoryFlags.activityDiagramEnabled()) {
                providers.add(new ActivityDiagramGuideTool());
                providers.add(new ActivityDiagramEditorTool(projectAccessor, transactionSupport, activityDiagramEditor, astahProToolSupport, imageCaptureSupport, includeEditorTools));
                providers.add(new ActionTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ActivityDiagramTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ActivityNodeTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ActivityTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ControlNodeTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new FlowTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ObjectNodeTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new PartitionTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // Class diagram tools
            if (categoryFlags.classDiagramEnabled()) {
                providers.add(new ClassDiagramGuideTool());
                providers.add(new ClassDiagramEditorTool(projectAccessor, transactionSupport, classDiagramEditor, astahProToolSupport, imageCaptureSupport, includeEditorTools));
                providers.add(new AssociationClassTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new AssociationTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new AttributeTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ClassTool(basicModelEditor, projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new DependencyTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new EnumerationLiteralTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new EnumerationTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new GeneralizationTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new InstanceSpecificationTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new NamedElementTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new OperationTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new PackageTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ParameterTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new RealizationTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new SlotTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new UsageTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // Sequence diagram tools
            if (categoryFlags.sequenceDiagramEnabled()) {
                providers.add(new SequenceDiagramGuideTool());
                providers.add(new SequenceDiagramEditorTool(projectAccessor, transactionSupport, sequenceDiagramEditor, astahProToolSupport, imageCaptureSupport, includeEditorTools));
                providers.add(new CombinedFragmentTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new GateTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new InteractionOperandTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new InteractionTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new InteractionUseTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new LifelineTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new LinkEndTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new LinkTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new MessageTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // State machine diagram tools
            if (categoryFlags.stateMachineDiagramEnabled()) {
                providers.add(new StateMachineDiagramGuideTool());
                providers.add(new StateMachineDiagramEditorTool(projectAccessor, transactionSupport, stateMachineDiagramEditor, astahProToolSupport, imageCaptureSupport, includeEditorTools));
                providers.add(new StateMachineDiagramTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new StateMachineTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new StateTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new TransitionTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new VertexTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new PseudostateTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // Use case diagram tools
            if (categoryFlags.useCaseDiagramEnabled()) {
                providers.add(new UseCaseDiagramGuideTool());
                providers.add(new UseCaseDiagramEditorTool(projectAccessor, transactionSupport, useCaseDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new UseCaseModelEditorTool(projectAccessor, transactionSupport, useCaseModelEditor, astahProToolSupport, includeEditorTools));
                providers.add(new UseCaseTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new IncludeTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new ExtendTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // Requirement diagram tools
            if (categoryFlags.requirementDiagramEnabled()) {
                providers.add(new RequirementDiagramGuideTool());
                providers.add(new RequirementDiagramEditorTool(projectAccessor, transactionSupport, requirementDiagramEditor, astahProToolSupport, includeEditorTools));
                providers.add(new RequirementTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new TestCaseTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // Communication diagram tools
            if (categoryFlags.communicationDiagramEnabled()) {
                providers.add(new CommunicationDiagramGuideTool());
                providers.add(new CommunicationDiagramTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
                providers.add(new LifelineLinkTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // Composite structure diagram tools
            if (categoryFlags.compositeStructureDiagramEnabled()) {
                providers.add(new CompositeStructureDiagramGuideTool());

                // Note: The editing APIs for the Composite Structure Diagram don't work as expected right now, so specify that no editing tools should be included.
                providers.add(new CompositeStructureModelEditorTool(basicModelEditor, projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new CompositeStructureDiagramEditorTool(projectAccessor, transactionSupport, compositeStructureDiagramEditor, astahProToolSupport, imageCaptureSupport, false));
                providers.add(new ConnectorTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new PortTool(projectAccessor, transactionSupport, astahProToolSupport, false));
            }

            // ER diagram tools
            if (categoryFlags.erDiagramEnabled()) {
                providers.add(new ERDiagramGuideTool());

                // Note: Only expose the query tools for ER diagrams, because there are too many editing tools and exposing them may affect existing tools.
                providers.add(new ERModelEditorTool(erModelEditor, projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERDiagramEditorTool(projectAccessor, transactionSupport, erDiagramEditor, astahProToolSupport, imageCaptureSupport, false));
                providers.add(new ERDiagramTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERDomainTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERModelTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERSchemaTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERPackageTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new EREntityTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERAttributeTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERDatatypeTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERIndexTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERRelationshipTool(projectAccessor, transactionSupport, astahProToolSupport, false));
                providers.add(new ERSubtypeRelationshipTool(projectAccessor, transactionSupport, astahProToolSupport, false));
            }

            // Mind map diagram tools
            if (categoryFlags.mindMapDiagramEnabled()) {
                providers.add(new MindMapGuideTool());
                providers.add(new MindmapEditorTool(projectAccessor, transactionSupport, mindmapEditor, astahProToolSupport, imageConvertSupport, imageCaptureSupport, includeEditorTools));
                providers.add(new MindMapDiagramTool(projectAccessor, transactionSupport, astahProToolSupport, includeEditorTools));
            }

            // Astah script tools
            providers.add(new AstahScriptGuideTool());
            providers.add(new AstahScriptTool(projectAccessor, includeEditorTools));

            // Wrap every provider so that each tool call holds the process-wide Astah API lock,
            // serializing Astah API access across concurrently connected AI agents.
            List<ToolProvider> exclusiveProviders = providers.stream()
                    .map(provider -> (ToolProvider) new ExclusiveToolProvider(provider))
                    .toList();
            
            return exclusiveProviders;

        } catch (ClassNotFoundException e) {
            log.error("Failed to get Astah Pro API classes: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Failed to create Astah Pro tools", e);
            return List.of();
        }
    }
}
