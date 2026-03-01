package com.astahpromcp.tool.astah.pro.editor;

import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DiagramEditorSupport {
    
    public enum DiagramEditorType {
        ACTIVITY_DIAGRAM(IActivityDiagram.class, ActivityDiagramEditor.class),
        CLASS_DIAGRAM(IClassDiagram.class, ClassDiagramEditor.class),
        COMMUNICATION_DIAGRAM(ICommunicationDiagram.class, BehaviorDiagramEditor.class),
        COMPONENT_DIAGRAM(IComponentDiagram.class, StructureDiagramEditor.class),
        COMPOSITE_STRUCTURE_DIAGRAM(ICompositeStructureDiagram.class, CompositeStructureDiagramEditor.class),
        DATA_FLOW_DIAGRAM(IDataFlowDiagram.class, BehaviorDiagramEditor.class),
        DEPLOYMENT_DIAGRAM(IDeploymentDiagram.class, StructureDiagramEditor.class),
        ER_DIAGRAM(IERDiagram.class, ERDiagramEditor.class),
        MATRIX_DIAGRAM(IMatrixDiagram.class, DiagramEditor.class),
        MIND_MAP_DIAGRAM(IMindMapDiagram.class, MindmapEditor.class),
        REQUIREMENT_DIAGRAM(IRequirementDiagram.class, RequirementDiagramEditor.class),
        REQUIREMENT_TABLE(IRequirementTable.class, DiagramEditor.class),
        SEQUENCE_DIAGRAM(ISequenceDiagram.class, SequenceDiagramEditor.class),
        STATE_MACHINE_DIAGRAM(IStateMachineDiagram.class, StateMachineDiagramEditor.class),
        TIMING_DIAGRAM(ITimingDiagram.class, BehaviorDiagramEditor.class),
        TRACEABILITY_MAP(ITraceabilityMap.class, MindmapEditor.class),
        USE_CASE_DIAGRAM(IUseCaseDiagram.class, UseCaseDiagramEditor.class);

        public final Class<? extends IDiagram> diagramInterface;
        public final Class<? extends DiagramEditor> diagramEditor;
        
        private DiagramEditorType(Class<? extends IDiagram> diagramInterface, Class<? extends DiagramEditor> diagramEditor) {
            this.diagramInterface = diagramInterface;
            this.diagramEditor = diagramEditor;
        }
    }

    private final List<DiagramEditor> diagramEditors;

    public DiagramEditorSupport(ProjectAccessor projectAccessor) {
        try {
            this.diagramEditors = List.of(
                projectAccessor.getDiagramEditorFactory().getActivityDiagramEditor(),
                projectAccessor.getDiagramEditorFactory().getClassDiagramEditor(),
                projectAccessor.getDiagramEditorFactory().getCompositeStructureDiagramEditor(),
                projectAccessor.getDiagramEditorFactory().getERDiagramEditor(),
                projectAccessor.getDiagramEditorFactory().getRequirementDiagramEditor(),
                projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor(),
                projectAccessor.getDiagramEditorFactory().getStateMachineDiagramEditor(),
                projectAccessor.getDiagramEditorFactory().getUseCaseDiagramEditor());

        } catch (InvalidUsingException e) {
            throw new RuntimeException(e);
        }
    }
        
    public DiagramEditor getCorrespondingDiagramEditor(IDiagram diagram) {
        log.debug("Get corresponding diagram editor for diagram: {}", diagram.getClass().getName());
        
        // Determine the editor class suitable for the diagram type
        Class<? extends DiagramEditor> requiredEditorClass = null;
        
        // Walk through the supported diagram types in order
        for (DiagramEditorType editorType : DiagramEditorType.values()) {
            // Check whether this diagram is supported by the current editor type
            // Example: when diagram is UMLClassDiagram, verify editorType.diagramInterface is IUMLClassDiagram
            if (editorType.diagramInterface.isAssignableFrom(diagram.getClass())) {
                requiredEditorClass = editorType.diagramEditor;
                break;
            }
        }
        
        // When the diagram type is not supported
        if (requiredEditorClass == null) {
            String errorMessage = "Unsupported diagram type: " + diagram.getClass().getName();
            throw new RuntimeException(errorMessage);
        }
        
        // Look for an editor instance that matches
        for (DiagramEditor editor : diagramEditors) {
            // Check whether the editor is an instance of the required editor class
            if (requiredEditorClass.isAssignableFrom(editor.getClass())) {
                log.debug("Found corresponding diagram editor: {}", editor.getClass().getName());
                return editor;
            }
        }
        
        // If no matching editor is found
        String errorMessage = "Corresponding diagram editor not found: " + requiredEditorClass.getName();
        throw new RuntimeException(errorMessage);
    }
}
