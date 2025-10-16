package com.astahpromcp;

import com.astahpromcp.ui.ExtraTabPanel;
import com.astahpromcp.ui.ExtraTabUiBuilder;
import com.astahpromcp.ui.TextPaneAppender;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class ExtraTabView extends JPanel implements IPluginExtraTabView, ProjectEventListener {

    private final ExtraTabPanel extraTabPanel;
    private final ExtraTabUiBuilder extraTabUiBuilder;
    private ProjectAccessor projectAccessor;
    private boolean listenerRegistered;

    public ExtraTabView() {
        extraTabPanel = new ExtraTabPanel();
        extraTabUiBuilder = new ExtraTabUiBuilder(() -> extraTabPanel);

        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            projectAccessor = api.getProjectAccessor();
            registerProjectListener(projectAccessor);

        } catch (Exception e) {
            String message = "An error has occurred, so the astah-pro-mcp plugin will be disabled.";
            JOptionPane.showMessageDialog(this,
                message,
                "astah-pro-mcp",
                JOptionPane.ERROR_MESSAGE);
            log.error(message, e);
        }

        TextPaneAppender.setLogMessageConsumer(this::appendLogMessage);
        extraTabUiBuilder.build(this);
    }

    // Append a log message to the JTextArea
    public void appendLogMessage(String message) {
        Runnable uiUpdate = () -> {
            if (extraTabPanel != null) {
                extraTabPanel.appendLogMessage(message);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            uiUpdate.run();
        } else {
            SwingUtilities.invokeLater(uiUpdate);
        }
    }

    @Override
    public void projectChanged(ProjectEvent e) {
    }

    @Override
    public void projectClosed(ProjectEvent e) {
    }

    @Override
    public void projectOpened(ProjectEvent e) {
    }

    @Override
    public void addSelectionListener(ISelectionListener listener) {
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public String getDescription() {
        return "This is an MCP server application for retrieving information from the modeling tool Astah Pro and for editing models and diagrams.";
    }

    @Override
    public String getTitle() {
        return "mcp";
    }

    @Override
    public void activated() {
        registerProjectListener(projectAccessor);
    }

    @Override
    public void deactivated() {
        unregisterProjectListener();
    }

    private void registerProjectListener(ProjectAccessor accessor) {
        if (accessor == null || listenerRegistered) {
            return;
        }
        accessor.addProjectEventListener(this);
        listenerRegistered = true;
    }

    private void unregisterProjectListener() {
        if (projectAccessor == null || !listenerRegistered) {
            return;
        }
        projectAccessor.removeProjectEventListener(this);
        listenerRegistered = false;
    }
}
