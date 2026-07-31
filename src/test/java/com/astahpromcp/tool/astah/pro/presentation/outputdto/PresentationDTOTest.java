package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationDTOTest {

    @Test
    void matches_ok_returnsTrueForValueEqualTypeName() {
        String equalButDistinctTypeName = new String(PresentationDTO.Type.LIFELINE.typeName);

        assertTrue(PresentationDTO.Type.LIFELINE.matches(equalButDistinctTypeName));
    }

    @Test
    void matches_ng_null() {
        assertFalse(PresentationDTO.Type.LIFELINE.matches(null));
    }

    @Test
    void getCorrespondingType_ok_returnsTypeForValueEqualTypeName() {
        String equalButDistinctTypeName = new String(PresentationDTO.Type.LIFELINE.typeName);

        assertEquals(PresentationDTO.Type.LIFELINE, PresentationDTO.Type.getCorrespondingType(equalButDistinctTypeName));
    }

    @Test
    void getCorrespondingType_ng_null() {
        assertEquals(PresentationDTO.Type.UNKNOWN, PresentationDTO.Type.getCorrespondingType(null));
    }
}
