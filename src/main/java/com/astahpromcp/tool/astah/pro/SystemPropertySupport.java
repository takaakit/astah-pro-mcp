package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.AstahAPI;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class SystemPropertySupport {

    @FunctionalInterface
    public interface Deletion {
        void execute() throws Exception;
    }

    static final String IMAGE_EXPORT_DPI_KEY = "basic.image_export_dpi";
    static final double IMAGE_EXPORT_DPI_DEFAULT = 96.0;
    static final double IMAGE_EXPORT_DPI_MINIMUM = 72.0;
    static final double IMAGE_EXPORT_DPI_MAXIMUM = 720.0;

    static final String CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY = "basic.confirm_modifying_elements_on_diagram";

    // The dpi that diagram images are exported at.
    public double imageExportDpi() {
        String value = imageExportDpiProperty();
        if (value == null || value.isBlank()) {
            return IMAGE_EXPORT_DPI_DEFAULT;
        }

        return parseImageExportDpi(value);
    }

    private String imageExportDpiProperty() {
        Properties properties = systemProperties();
        if (properties == null) {
            log.debug("Failed to read the Astah system properties, falling back to {} dpi", IMAGE_EXPORT_DPI_DEFAULT);
            return null;
        }

        return properties.getProperty(IMAGE_EXPORT_DPI_KEY);
    }

    // Astah clamps an out-of-range dpi to the bound and warns, so mirror that rather than reporting a dpi that the export would not actually use.
    static double parseImageExportDpi(String value) {
        double dpi;
        try {
            dpi = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.debug("Astah system property {} is not a number ({}), falling back to {} dpi",
                    IMAGE_EXPORT_DPI_KEY, value, IMAGE_EXPORT_DPI_DEFAULT);
            return IMAGE_EXPORT_DPI_DEFAULT;
        }

        if (Double.isNaN(dpi)) {
            return IMAGE_EXPORT_DPI_DEFAULT;
        }

        return Math.max(IMAGE_EXPORT_DPI_MINIMUM, Math.min(IMAGE_EXPORT_DPI_MAXIMUM, dpi));
    }

    public void deleteWithoutConfirmationDialog(Deletion deletion) throws Exception {
        runDeleteWithoutConfirmationDialog(systemProperties(), deletion);
    }

    static void runDeleteWithoutConfirmationDialog(Properties properties, Deletion deletion) throws Exception {
        if (properties == null) {
            deletion.execute();
            return;
        }

        String previous = properties.getProperty(CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY);
        if (previous != null && !Boolean.parseBoolean(previous.trim())) {
            // Already off. Leave it alone so that this delete does not switch it back on.
            deletion.execute();
            return;
        }

        properties.setProperty(CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY, "false");
        try {
            deletion.execute();

        } finally {
            if (previous == null) {
                properties.remove(CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY);
            } else {
                properties.setProperty(CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY, previous);
            }
        }
    }

    private Properties systemProperties() {
        try {
            return AstahAPI.getAstahAPI().getSystemPropertyAccessor().getSystemProperties();

        } catch (Exception e) {
            log.debug("The Astah system properties are unavailable: {}", e.getMessage());
            return null;
        }
    }
}
