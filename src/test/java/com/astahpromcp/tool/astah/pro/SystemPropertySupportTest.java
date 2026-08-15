package com.astahpromcp.tool.astah.pro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemPropertySupportTest {

    @Test
    void parseImageExportDpi_ok_readsTheConfiguredValue() {
        assertEquals(150.0, SystemPropertySupport.parseImageExportDpi("150"));
    }

    @Test
    void parseImageExportDpi_ok_readsTheDecimalFormAstahDeclares() {
        // JudeProp.properties declares basic.image_export_dpi.type=double, so the stored value may carry a fraction.
        assertEquals(96.0, SystemPropertySupport.parseImageExportDpi("96.0"));
        assertEquals(120.5, SystemPropertySupport.parseImageExportDpi(" 120.5 "));
    }

    @Test
    void parseImageExportDpi_ok_keepsBothBounds() {
        assertEquals(72.0, SystemPropertySupport.parseImageExportDpi("72"));
        assertEquals(720.0, SystemPropertySupport.parseImageExportDpi("720"));
    }

    @Test
    void parseImageExportDpi_ok_clampsOutOfRangeValuesLikeAstahDoes() {
        // Astah itself clamps to the bound and warns, so an out-of-range property must not be reported verbatim.
        assertEquals(72.0, SystemPropertySupport.parseImageExportDpi("1"));
        assertEquals(720.0, SystemPropertySupport.parseImageExportDpi("5000"));
        assertEquals(72.0, SystemPropertySupport.parseImageExportDpi("-96"));
    }

    @Test
    void parseImageExportDpi_ok_fallsBackToTheDefaultOnAnUnusableValue() {
        assertEquals(96.0, SystemPropertySupport.parseImageExportDpi("high"));
        assertEquals(96.0, SystemPropertySupport.parseImageExportDpi(""));
        assertEquals(96.0, SystemPropertySupport.parseImageExportDpi("NaN"));
    }

    @Test
    void imageExportDpi_ok_alwaysReturnsADpiTheExportCanUse() {
        // Whether or not the accessor can reach a running Astah, reading the dpi must yield a value the export can actually use - never an escaping exception, and never a value Astah would reject.
        double dpi = new SystemPropertySupport().imageExportDpi();

        assertTrue(dpi >= 72.0 && dpi <= 720.0, "The dpi must stay within Astah's 72-720 range, but was " + dpi);
    }
}
