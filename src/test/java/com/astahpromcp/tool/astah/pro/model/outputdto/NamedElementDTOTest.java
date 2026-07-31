package com.astahpromcp.tool.astah.pro.model.outputdto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedElementDTOTest {

    @Test
    void matches_ok_returnsTrueForValueEqualTypeName() {
        String equalButDistinctTypeName = new String(NamedElementDTO.Type.ELEMENT.typeName);

        assertTrue(NamedElementDTO.Type.ELEMENT.matches(equalButDistinctTypeName));
    }

    @Test
    void matches_ng_null() {
        assertFalse(NamedElementDTO.Type.ELEMENT.matches(null));
    }

    @Test
    void getCorrespondingType_ok_returnsTypeForValueEqualTypeName() {
        String equalButDistinctTypeName = new String(NamedElementDTO.Type.ELEMENT.typeName);

        assertEquals(NamedElementDTO.Type.ELEMENT, NamedElementDTO.Type.getCorrespondingType(equalButDistinctTypeName));
    }

    @Test
    void getCorrespondingType_ng_null() {
        // A null literal binds to the String overload, so both overloads must tolerate null.
        assertEquals(NamedElementDTO.Type.UNKNOWN, NamedElementDTO.Type.getCorrespondingType(null));
        assertEquals(NamedElementDTO.Type.UNKNOWN, NamedElementDTO.Type.getCorrespondingType((Object) null));
    }
}
