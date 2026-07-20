package com.astahpromcp.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExtraTabPanelTest {

    @Test
    void overflowChars_ok_zeroWhenWithinCap() {
        assertEquals(0, ExtraTabPanel.overflowChars(500, 1000));
        assertEquals(0, ExtraTabPanel.overflowChars(1000, 1000), "Exactly at the cap is not an overflow");
        assertEquals(0, ExtraTabPanel.overflowChars(0, 1000));
    }

    @Test
    void overflowChars_ok_positiveWhenExceedingCap() {
        assertEquals(500, ExtraTabPanel.overflowChars(1500, 1000));
        assertEquals(1, ExtraTabPanel.overflowChars(1001, 1000));
    }
}
