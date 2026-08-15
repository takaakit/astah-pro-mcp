package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.AstahAPI;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

// Reads the Astah system properties
@Slf4j
public class SystemPropertySupport {

    static final String IMAGE_EXPORT_DPI_KEY = "basic.image_export_dpi";
    static final double IMAGE_EXPORT_DPI_DEFAULT = 96.0;
    static final double IMAGE_EXPORT_DPI_MINIMUM = 72.0;
    static final double IMAGE_EXPORT_DPI_MAXIMUM = 720.0;

    // The dpi that diagram images are exported at.
    public double imageExportDpi() {
        String value = imageExportDpiProperty();
        if (value == null || value.isBlank()) {
            return IMAGE_EXPORT_DPI_DEFAULT;
        }

        return parseImageExportDpi(value);
    }

    private String imageExportDpiProperty() {
        try {
            Properties properties = AstahAPI.getAstahAPI().getSystemPropertyAccessor().getSystemProperties();
            return properties == null ? null : properties.getProperty(IMAGE_EXPORT_DPI_KEY);

        } catch (Exception e) {
            // The accessor is unavailable outside the Astah plugin environment.
            log.debug("Failed to read the Astah system properties, falling back to {} dpi: {}",
                    IMAGE_EXPORT_DPI_DEFAULT, e.getMessage());
            return null;
        }
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
}
