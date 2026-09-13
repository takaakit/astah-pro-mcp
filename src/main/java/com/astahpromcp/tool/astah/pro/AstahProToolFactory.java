package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.common.ImageConvertSupport;
import com.astahpromcp.tool.astah.pro.editor.*;
import com.astahpromcp.tool.astah.pro.guide.*;
import com.astahpromcp.tool.astah.pro.image.*;
import com.astahpromcp.tool.astah.pro.preliminary.*;
import com.astahpromcp.tool.astah.pro.review.*;
import com.astahpromcp.tool.astah.pro.astahapiscript.*;
import com.astahpromcp.tool.astah.pro.model.*;
import com.astahpromcp.tool.astah.pro.presentation.*;
import com.astahpromcp.tool.astah.pro.project.*;
import com.astahpromcp.tool.astah.pro.view.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
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
        this(McpServerConfig.WORKSPACE_DIR.resolve("images"));
    }

    public AstahProToolFactory(Path imageOutputDir) {
        this.imageOutputDir = imageOutputDir;
    }

    // Create the tool providers, each wrapped so that its handlers hold the process-wide Astah API lock.
    public List<ToolProvider> createToolProviders() {
        return createRawToolProviders().stream()
                .map(provider -> (ToolProvider) new ExclusiveToolProvider(provider))
                .toList();
    }

    // Create the tool providers without the Astah API lock.
    public List<ToolProvider> createRawToolProviders() {
        return createRawToolProviders(DiagramThumbnails.INCLUDE);
    }

    // Create the tool providers without the Astah API lock, choosing whether the editing tools capture the diagram they change.
    public List<ToolProvider> createRawToolProviders(DiagramThumbnails diagramThumbnails) {
        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            ProjectAccessor projectAccessor = api.getProjectAccessor();
            IModelEditorFactory modelEditorFactory = projectAccessor.getModelEditorFactory();
            BasicModelEditor basicModelEditor = modelEditorFactory.getBasicModelEditor();
            IDiagramEditorFactory diagramEditorFactory = projectAccessor.getDiagramEditorFactory();
            ClassDiagramEditor classDiagramEditor = diagramEditorFactory.getClassDiagramEditor();
            SequenceDiagramEditor sequenceDiagramEditor = diagramEditorFactory.getSequenceDiagramEditor();
            ActivityDiagramEditor activityDiagramEditor = diagramEditorFactory.getActivityDiagramEditor();
            StateMachineDiagramEditor stateMachineDiagramEditor = diagramEditorFactory.getStateMachineDiagramEditor();
            RequirementDiagramEditor requirementDiagramEditor = diagramEditorFactory.getRequirementDiagramEditor();
            UseCaseDiagramEditor useCaseDiagramEditor = diagramEditorFactory.getUseCaseDiagramEditor();
            CompositeStructureDiagramEditor compositeStructureDiagramEditor = diagramEditorFactory.getCompositeStructureDiagramEditor();
            CompositeStructureModelEditor compositeStructureModelEditor = modelEditorFactory.getCompositeStructureModelEditor();
            UseCaseModelEditor useCaseModelEditor = modelEditorFactory.getUseCaseModelEditor();
            ERModelEditor erModelEditor = modelEditorFactory.getERModelEditor();
            ERDiagramEditor erDiagramEditor = diagramEditorFactory.getERDiagramEditor();
            MindmapEditor mindmapEditor = diagramEditorFactory.getMindmapEditor();
            AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
            DiagramEditorSupport diagramEditorSupport = new DiagramEditorSupport(projectAccessor);
            ImageConvertSupport imageConvertSupport = new ImageConvertSupport();
            SystemPropertySupport systemPropertySupport = new SystemPropertySupport();
            ImageCaptureSupport imageCaptureSupport = diagramThumbnails == DiagramThumbnails.OMIT
                    ? new NoThumbnailImageCaptureSupport(astahProToolSupport, systemPropertySupport, imageOutputDir)
                    : new ImageCaptureSupport(astahProToolSupport, systemPropertySupport, imageOutputDir);
            SvgOverlaySupport svgOverlaySupport = new SvgOverlaySupport(imageCaptureSupport, imageConvertSupport, systemPropertySupport);
            TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());

            List<ToolProvider> providers = new ArrayList<>();
            
            // Common tools
            providers.add(new AstahProMcpGuideTool(projectAccessor));
            providers.add(new DiagramLayoutGuideTool());
            providers.add(new BasicDiagramEditorTool(projectAccessor, transactionSupport, astahProToolSupport, diagramEditorSupport, imageCaptureSupport));
            providers.add(new BasicModelEditorTool(basicModelEditor, projectAccessor, transactionSupport, astahProToolSupport, systemPropertySupport));
            providers.add(new DiagramEditorTool(projectAccessor, transactionSupport, astahProToolSupport, systemPropertySupport, diagramEditorSupport, imageConvertSupport, imageCaptureSupport));
            providers.add(new StructureDiagramEditorTool(projectAccessor, transactionSupport, astahProToolSupport, diagramEditorSupport, imageCaptureSupport));
            providers.add(new CommentTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new DiagramTool(projectAccessor, transactionSupport, astahProToolSupport, systemPropertySupport, imageOutputDir));
            providers.add(new ElementTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ConstraintTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new LinkPresentationTool(projectAccessor, transactionSupport, astahProToolSupport, imageCaptureSupport));
            providers.add(new NodePresentationTool(projectAccessor, transactionSupport, astahProToolSupport, imageCaptureSupport));
            providers.add(new PresentationTool(projectAccessor, transactionSupport, astahProToolSupport, imageCaptureSupport));
            providers.add(new ProjectAccessorTool(projectAccessor, astahProToolSupport));
            providers.add(new ProjectInfoTool(projectAccessor, astahProToolSupport));
            providers.add(new ImageCaptureTool(projectAccessor, imageCaptureSupport));
            providers.add(new HyperlinkOwnerTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new DiagramLayoutLintTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new TerminologyConsistencyTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new DiagramConsistencyTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new PreliminaryLayoutTool());
            providers.add(new SvgOverlayTool(svgOverlaySupport));
            
            // The view managers are available only inside the running Astah GUI (plugin environment).
            try {
                IViewManager viewManager = projectAccessor.getViewManager();
                IDiagramViewManager diagramViewManager = viewManager.getDiagramViewManager();
                IProjectViewManager projectViewManager = viewManager.getProjectViewManager();
                providers.add(new DiagramViewManagerTool(projectAccessor, diagramViewManager, transactionSupport, astahProToolSupport));
                providers.add(new ProjectViewManagerTool(projectAccessor, projectViewManager, transactionSupport, astahProToolSupport));
            } catch (InvalidUsingException e) {
                log.warn("View manager tools are unavailable (not running inside Astah): {}", e.getMessage());
            }

            // Activity diagram tools
            providers.add(new ActivityDiagramGuideTool());
            providers.add(new ActivityDiagramEditorTool(projectAccessor, transactionSupport, activityDiagramEditor, astahProToolSupport, imageCaptureSupport));
            providers.add(new ActionTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ActivityDiagramTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ActivityNodeTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ActivityTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ControlNodeTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new FlowTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ObjectNodeTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new PartitionTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Class diagram tools
            providers.add(new ClassDiagramGuideTool());
            providers.add(new ClassDiagramEditorTool(projectAccessor, transactionSupport, classDiagramEditor, astahProToolSupport, imageCaptureSupport));
            providers.add(new AssociationClassTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new AssociationTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new AttributeTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ClassTool(basicModelEditor, projectAccessor, transactionSupport, astahProToolSupport, systemPropertySupport));
            providers.add(new DependencyTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new EnumerationLiteralTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new EnumerationTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new GeneralizationTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new InstanceSpecificationTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new NamedElementTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new OperationTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new PackageTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ParameterTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new RealizationTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new SlotTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new UsageTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Sequence diagram tools
            providers.add(new SequenceDiagramGuideTool());
            providers.add(new SequenceDiagramEditorTool(projectAccessor, transactionSupport, sequenceDiagramEditor, astahProToolSupport, imageCaptureSupport));
            providers.add(new SequenceDiagramTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new CombinedFragmentTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new GateTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new InteractionOperandTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new InteractionTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new InteractionUseTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new LifelineTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new LinkEndTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new LinkTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new MessageTool(projectAccessor, transactionSupport, astahProToolSupport));

            // State machine diagram tools
            providers.add(new StateMachineDiagramGuideTool());
            providers.add(new StateMachineDiagramEditorTool(projectAccessor, transactionSupport, stateMachineDiagramEditor, astahProToolSupport, imageCaptureSupport));
            providers.add(new StateMachineDiagramTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new StateMachineTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new StateTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new TransitionTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new VertexTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new PseudostateTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Use case diagram tools
            providers.add(new UseCaseDiagramGuideTool());
            providers.add(new UseCaseDiagramEditorTool(projectAccessor, transactionSupport, useCaseDiagramEditor, astahProToolSupport));
            providers.add(new UseCaseModelEditorTool(projectAccessor, transactionSupport, useCaseModelEditor, astahProToolSupport));
            providers.add(new UseCaseTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new IncludeTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ExtendTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Requirement diagram tools
            providers.add(new RequirementDiagramGuideTool());
            providers.add(new RequirementDiagramEditorTool(projectAccessor, transactionSupport, requirementDiagramEditor, astahProToolSupport));
            providers.add(new RequirementTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new TestCaseTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Communication diagram tools
            providers.add(new CommunicationDiagramGuideTool());
            providers.add(new CommunicationDiagramTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new LifelineLinkTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Composite structure diagram tools
            providers.add(new CompositeStructureDiagramGuideTool());
            providers.add(new CompositeStructureDiagramEditorTool(projectAccessor, transactionSupport, compositeStructureDiagramEditor, astahProToolSupport, imageCaptureSupport));
            providers.add(new CompositeStructureModelEditorTool(compositeStructureModelEditor, projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ConnectorTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new PortTool(projectAccessor, transactionSupport, astahProToolSupport));

            // ER diagram tools
            providers.add(new ERDiagramGuideTool());
            providers.add(new ERModelEditorTool(erModelEditor, projectAccessor, transactionSupport, astahProToolSupport, systemPropertySupport));
            providers.add(new ERDiagramEditorTool(projectAccessor, transactionSupport, erDiagramEditor, astahProToolSupport, imageCaptureSupport));
            providers.add(new ERDiagramTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERDomainTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERModelTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERSchemaTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERPackageTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new EREntityTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERAttributeTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERDatatypeTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERIndexTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERRelationshipTool(projectAccessor, transactionSupport, astahProToolSupport));
            providers.add(new ERSubtypeRelationshipTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Mind map diagram tools
            providers.add(new MindMapGuideTool());
            providers.add(new MindmapEditorTool(projectAccessor, transactionSupport, mindmapEditor, astahProToolSupport, imageConvertSupport, imageCaptureSupport));
            providers.add(new MindMapDiagramTool(projectAccessor, transactionSupport, astahProToolSupport));

            // Astah API script tools
            providers.add(new AstahApiScriptGuideTool());
            providers.add(new AstahApiScriptTool(projectAccessor));

            return List.copyOf(providers);

        } catch (ClassNotFoundException e) {
            log.error("Failed to get Astah Pro API classes: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Failed to create Astah Pro tools", e);
            return List.of();
        }
    }
}
