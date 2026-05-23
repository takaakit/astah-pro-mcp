package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ModelFinder;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Assertions;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class TestSupport {
    private static TestSupport instance;
    private final ProjectAccessor projectAccessor;

    private TestSupport() {
        AstahAPI astahApi = null;
        try {
            astahApi = AstahAPI.getAstahAPI();
        } catch (ClassNotFoundException e) {
            Assertions.fail("Failed to AstahAPI.getAstahAPI()");
        }
        projectAccessor = astahApi.getProjectAccessor();
    }
    
    public static TestSupport instance() {
        if (instance == null) {
            instance = new TestSupport();
        }
        return instance;
    }

    public INamedElement getNamedElementByClassAndName(Class<? extends INamedElement> clazz, String name) {
        try {
            INamedElement[] namedElements = projectAccessor.findElements(clazz, name);

            if (namedElements.length != 1) {
                return null;
            }
            return namedElements[0];

        } catch (Exception e) {
            return null;
        }
    }

    public INamedElement getNamedElementById(String id) {
        try {
            INamedElement[] namedElements = projectAccessor.findElements(new ModelFinder() {
                @Override
                public boolean isTarget(INamedElement element) {
                    return element.getId().equals(id);
                }
            });

            if (namedElements.length != 1) {
                return null;
            }
            return namedElements[0];

        } catch (Exception e) {
            return null;
        }
    }

    public List<INamedElement> getNamedElementsByClass(Class<? extends INamedElement> clazz) {
        try {
            return List.of(projectAccessor.findElements(clazz));
        } catch (Exception e) {
            return List.of();
        }
    }

    public IPresentation getPresentationByTypeAndLabel(String presentationType, String label) {
        try {
            INamedElement[] namedElements = projectAccessor.findElements(IDiagram.class);

            List<IPresentation> presentations = new ArrayList<>();
            for (INamedElement namedElement : namedElements) {
                IDiagram diagram = (IDiagram)namedElement;
                for (IPresentation presentation : diagram.getPresentations()) {
                    if (presentation.getType().equals(presentationType)
                        && presentation.getLabel().equals(label)) {
                        presentations.add(presentation);
                    }
                }
            }

            if (presentations.size() != 1) {
                return null;
            }
            return presentations.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    public IPresentation getPresentationById(String id) {
        try {
            INamedElement[] namedElements = projectAccessor.findElements(IDiagram.class);

            List<IPresentation> presentations = new ArrayList<>();
            for (INamedElement namedElement : namedElements) {
                IDiagram diagram = (IDiagram)namedElement;
                for (IPresentation presentation : diagram.getPresentations()) {
                    if (presentation.getID().equals(id)) {
                        presentations.add(presentation);
                    }
                }
            }

            if (presentations.size() != 1) {
                return null;
            }
            return presentations.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    public List<IPresentation> getPresentationsByType(String type) {
        try {
            INamedElement[] namedElements = projectAccessor.findElements(IDiagram.class);

            List<IPresentation> presentations = new ArrayList<>();
            for (INamedElement namedElement : namedElements) {
                IDiagram diagram = (IDiagram)namedElement;
                for (IPresentation presentation : diagram.getPresentations()) {
                    if (presentation.getType().equals(type)) {
                        presentations.add(presentation);
                    }
                }
            }
            
            return presentations;

        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeToolMethodReturningDto(Method method, Object tool, Object inputDTO, Class<T> resultType) throws Exception {
        return (T) method.invoke(
            tool,
            mock(McpSyncServerExchange.class),
            inputDTO);
    }

    @SuppressWarnings("unchecked")
    public List<McpSchema.Content> invokeToolMethodReturningContents(Method method, Object tool, Object inputDTO) throws Exception {
        return (List<McpSchema.Content>) method.invoke(
            tool,
            mock(McpSyncServerExchange.class),
            inputDTO);
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeToolMethodReturningDtoAndContents(Method method, Object tool, Object inputDTO, Class<T> resultType) throws Exception {
        Pair<T, ?> pair = (Pair<T, ?>) method.invoke(
            tool,
            mock(McpSyncServerExchange.class),
            inputDTO);
        return pair.getLeft();
    }

    public static Method getAccessibleMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) 
            throws NoSuchMethodException {
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    public static Field getAccessibleField(Class<?> clazz, String fieldName) 
            throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}