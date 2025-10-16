package com.astahpromcp.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;

public final class ExtraTabUiBuilder {

    private final Supplier<ExtraTabPanel> panelSupplier;

    public ExtraTabUiBuilder(Supplier<ExtraTabPanel> panelSupplier) {
        this.panelSupplier = Objects.requireNonNull(panelSupplier, "panelSupplier");
    }

    public void build(JPanel parent) {
        Objects.requireNonNull(parent, "parent");

        Runnable task = () -> {
            parent.setLayout(new BorderLayout());
            parent.add(panelSupplier.get(), BorderLayout.CENTER);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }
}
