package com.astahpromcp.tool.projectdiff.xmi;

import com.astahpromcp.tool.projectdiff.outputdto.ValueEncoding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class XmiValueCodecTest {

    private static XmiValueCodec.Decoded decode(String ownerTag, boolean diagram, String path, String raw) {
        return XmiValueCodec.decode(ownerTag, diagram, path, raw);
    }

    @Test
    void decode_ok_undoesTheEncodingOfAModelElementName() {
        assertEquals(new XmiValueCodec.Decoded("Take order", ValueEncoding.DECODED), decode("UML:Class", false, "@name", "Take+order"));
        assertEquals("t..t+3", decode("UML:TimeConstraint", false, "@name", "t..t%2B3").value());
        assertEquals("ERデータタイプモデル", decode("UML:Model", false, "@name", "ER%E3%83%87%E3%83%BC%E3%82%BF%E3%82%BF%E3%82%A4%E3%83%97%E3%83%A2%E3%83%87%E3%83%AB").value());
    }

    @Test
    void decode_ok_undoesTheEncodingWhereItWasConfirmed() {
        assertEquals("Changed definition", decode("UML:Class", false, "UML:ModelElement.definition/@xmi.value", "Changed+definition").value());
        assertEquals("a body", decode("UML:Comment", false, "@body", "a+body").value());
        assertEquals("v 1", decode("UML:TaggedValue", false, "@value", "v+1").value());
        assertEquals("Card, Password", decode("JUDE:NotePresentation", false, "@label", "Card%2C+Password").value());
        assertEquals("http://", decode("JUDE:ClassifierPresentation", false, "JUDE:UPresentation.hyperlinks/JUDE:Hyperlink/@path", "http%3A%2F%2F").value());
    }

    @Test
    void decode_ok_showsTheNameOfADiagramAsWritten() {
        // A diagram's name is not encoded: decoding would turn '+' into a space
        assertEquals(new XmiValueCodec.Decoded("Process & Artifact", ValueEncoding.RAW), decode("JUDE:Diagram", true, "@name", "Process & Artifact"));
        assertEquals(new XmiValueCodec.Decoded("a+b", ValueEncoding.RAW), decode("JUDE:ActivityDiagram", true, "@name", "a+b"));
        assertEquals("ER Diagram(IE)", decode("JUDE:ERDiagram", true, "@typeInfo", "ER Diagram(IE)").value());
    }

    @Test
    void decode_ok_showsTheBodyOfAnExpressionAsWritten() {
        assertEquals(new XmiValueCodec.Decoded("t..t+3", ValueEncoding.RAW),
                decode("UML:TimeConstraint", false, "UML:Constraint.body/UML:BooleanExpression/UML:Expression.body", "t..t+3"));
    }

    @Test
    void decode_ok_marksAValueAtAnUnknownPositionThatMayBeEncoded() {
        assertEquals(new XmiValueCodec.Decoded("%2B", ValueEncoding.UNKNOWN), decode("UML:Class", false, "@someAttribute", "%2B"));
        assertEquals(new XmiValueCodec.Decoded("a+b", ValueEncoding.UNKNOWN), decode("UML:Class", false, "@someAttribute", "a+b"));
    }

    @Test
    void decode_ok_showsAValueThatCannotBeEncodedAsRaw() {
        // A space, a '&' or a character beyond ASCII never appears in the output of a URL encoder
        assertEquals(ValueEncoding.RAW, decode("UML:AssociationEnd", false, "@navigableType", "unspecified navigable").encoding());
        assertEquals(ValueEncoding.RAW, decode("UML:Class", false, "@someAttribute", "50%").encoding());
        assertEquals(ValueEncoding.RAW, decode("UML:Class", false, "@someAttribute", "true").encoding());
        assertEquals(ValueEncoding.RAW, decode("JUDE:ClassifierPresentation", false, "JUDE:JomtPresentation.location/XMI.field[0]", "40.0").encoding());
    }

    @Test
    void decode_ok_keepsAMalformedEscapeAsWritten() {
        assertEquals(new XmiValueCodec.Decoded("50%", ValueEncoding.UNKNOWN), decode("UML:Class", false, "@name", "50%"));
    }

    @Test
    void decode_ok_handlesAnEmptyValue() {
        assertEquals(new XmiValueCodec.Decoded("", ValueEncoding.RAW), decode("UML:Class", false, "@name", ""));
    }

    @Test
    void couldBeUrlEncoded_ok_needsAPlusOrAPercentAmongSafeCharacters() {
        assertTrue(XmiValueCodec.couldBeUrlEncoded("a+b"));
        assertTrue(XmiValueCodec.couldBeUrlEncoded("%E3%83%87"));
        assertFalse(XmiValueCodec.couldBeUrlEncoded("abc"));
        assertFalse(XmiValueCodec.couldBeUrlEncoded("a b+c"));
        assertFalse(XmiValueCodec.couldBeUrlEncoded("日本+語"));
    }
}
