package com.astahpromcp.tool.astah.pro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AstahElementTypeTest {
    
    @Test
    void matches_ok_returnsTrueForValueEqualTypeName() {
        String equalButDistinctTypeName = new String(AstahElementType.ELEMENT.typeName);
        
        assertTrue(AstahElementType.ELEMENT.matches(equalButDistinctTypeName));
    }
    
    @Test
    void matches_ng_null() {
        assertFalse(AstahElementType.ELEMENT.matches(null));
    }
    
    @Test
    void getCorrespondingType_ok_returnsTypeForValueEqualTypeName() {
        String equalButDistinctTypeName = new String(AstahElementType.ELEMENT.typeName);
        
        assertEquals(AstahElementType.ELEMENT, AstahElementType.getCorrespondingType(equalButDistinctTypeName));
    }
    
    @Test
    void getCorrespondingType_ng_null() {
        // A null literal binds to the String overload, so both overloads must tolerate null.
        assertEquals(AstahElementType.UNKNOWN, AstahElementType.getCorrespondingType(null));
        assertEquals(AstahElementType.UNKNOWN, AstahElementType.getCorrespondingType((Object) null));
    }
}
