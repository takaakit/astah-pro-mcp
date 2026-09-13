package com.astahpromcp.tool.astah.pro;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SystemPropertySupportTest {

    static final String ASTAH_SYSTEM_PROPERTY_DECLARATIONS = "JP/co/esm/caddies/jomt/resource/JudeProp.properties";

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

    @Test
    void runDeleteWithoutConfirmationDialog_ok_turnsTheConfirmationOffForTheDeleteAndPutsItBack() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY, "true");

        AtomicReference<String> seenDuringDelete = new AtomicReference<>();
        SystemPropertySupport.runDeleteWithoutConfirmationDialog(properties, () ->
                seenDuringDelete.set(properties.getProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY)));

        assertEquals("false", seenDuringDelete.get(), "The delete must run with the confirmation dialog switched off");
        assertEquals("true", properties.getProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY),
                "The setting must be put back as it was");
    }

    @Test
    void runDeleteWithoutConfirmationDialog_ok_leavesTheConfirmationOffWhenTheUserAlreadyHadItOff() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY, "false");

        SystemPropertySupport.runDeleteWithoutConfirmationDialog(properties, () -> {});

        assertEquals("false", properties.getProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY),
                "A setting the user already has off must not be switched on");
    }

    @Test
    void runDeleteWithoutConfirmationDialog_ok_leavesAnAbsentKeyAbsent() throws Exception {
        Properties properties = new Properties();

        AtomicReference<String> seenDuringDelete = new AtomicReference<>();
        SystemPropertySupport.runDeleteWithoutConfirmationDialog(properties, () ->
                seenDuringDelete.set(properties.getProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY)));

        assertEquals("false", seenDuringDelete.get(), "The delete must run with the confirmation dialog switched off");
        assertFalse(properties.containsKey(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY),
                "A key that was not there before the delete must not be left behind");
    }

    @Test
    void runDeleteWithoutConfirmationDialog_ng_restoresTheSettingWhenTheDeleteFails() {
        Properties properties = new Properties();
        properties.setProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY, "true");

        assertThrows(IllegalStateException.class, () ->
                SystemPropertySupport.runDeleteWithoutConfirmationDialog(properties, () -> {
                    throw new IllegalStateException("delete failed");
                }));

        assertEquals("true", properties.getProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY),
                "The setting must be put back even when the delete fails");
    }

    @Test
    void runDeleteWithoutConfirmationDialog_ok_stillRunsTheDeleteOutsideAstah() throws Exception {
        AtomicReference<Boolean> deleted = new AtomicReference<>(false);

        SystemPropertySupport.runDeleteWithoutConfirmationDialog(null, () -> deleted.set(true));

        assertTrue(deleted.get(), "The delete must still run when there are no system properties to switch");
    }

    @Test
    void confirmModifyingKey_ok_isStillDeclaredByTheInstalledAstah() throws Exception {
        Properties declared = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(ASTAH_SYSTEM_PROPERTY_DECLARATIONS)) {
            assertNotNull(in, "Astah no longer ships " + ASTAH_SYSTEM_PROPERTY_DECLARATIONS
                    + ", so this check can no longer see what it declares");
            declared.load(in);
        }

        assertTrue(declared.containsKey(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY),
                "Astah no longer declares " + SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY
                        + ", so deletes would raise the confirmation dialog again and block Astah");
        assertEquals("boolean", declared.getProperty(SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY + ".type"),
                SystemPropertySupport.CONFIRM_MODIFYING_ELEMENTS_ON_DIAGRAM_KEY
                        + " is no longer a boolean, so writing \"false\" may no longer switch the dialog off");
    }
}
