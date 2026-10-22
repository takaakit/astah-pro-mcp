package com.astahpromcp.tool.projectdiff.engine;

import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.ValueEncoding;
import com.astahpromcp.tool.projectdiff.outputdto.assembler.ChangeDTOAssembler;
import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// The comparison of two XMIs, on small XMIs laid out the way Astah writes them
public class ProjectDiffEngineTest {

    private static final Set<String> VERIFIED = Set.of("1.2.0.44/44");

    @TempDir
    Path dir;

    // A project with two classes, an association whose end has a multiplicity, a package, and a class diagram.
    // The multiplicity is also referred to by an association end role, the way Sample.asta has it.
    private static final String BASE = """
            <UML:Model xmi.id="root" name="project" isRoot="false">
              <UML:Namespace.ownedElement>
                <UML:Class xmi.id="c1" name="Order">
                  <UML:ModelElement.namespace><UML:Namespace xmi.idref="root"/></UML:ModelElement.namespace>
                </UML:Class>
                <UML:Class xmi.id="c2" name="Customer"/>
                <UML:Class xmi.id="c3" name="Spare">
                  <UML:ModelElement.definition xmi.value="kept+as+is"/>
                </UML:Class>
                <UML:Package xmi.id="pk1" name="sub">
                  <UML:Namespace.ownedElement>
                  </UML:Namespace.ownedElement>
                </UML:Package>
                <UML:Association xmi.id="a1" name="">
                  <UML:Association.connection>
                    <UML:AssociationEnd xmi.id="e1" name="" aggregation="none">
                      <UML:StructuralFeature.multiplicity>
                        <UML:Multiplicity xmi.id="m1">
                          <UML:Multiplicity.range>
                            <UML:MultiplicityRange xmi.id="r1" lowerValue="1" upperValue="*"/>
                          </UML:Multiplicity.range>
                        </UML:Multiplicity>
                      </UML:StructuralFeature.multiplicity>
                      <UML:AssociationEnd.participant><UML:Classifier xmi.idref="c2"/></UML:AssociationEnd.participant>
                    </UML:AssociationEnd>
                  </UML:Association.connection>
                </UML:Association>
                <UML:AssociationEndRole xmi.id="er1" name="role">
                  <UML:AssociationEndRole.multiplicity><UML:Multiplicity xmi.idref="m1"/></UML:AssociationEndRole.multiplicity>
                </UML:AssociationEndRole>
              </UML:Namespace.ownedElement>
            </UML:Model>
            <JUDE:Diagram xmi.id="d1" name="Main" typeInfo="Class Diagram">
              <UML:ModelElement.taggedValue>
                <UML:TaggedValue xmi.id="tv1" tag="jude.diagram.updated_version" value="12.0.0"/>
              </UML:ModelElement.taggedValue>
              <JUDE:Diagram.presentations>
                <JUDE:ClassifierPresentation xmi.id="p1" label="Order" width="80.0" depth="3">
                  <JUDE:UPresentation.semanticModel><UML:Class xmi.idref="c1"/></JUDE:UPresentation.semanticModel>
                  <JUDE:UPresentation.diagram><JUDE:Diagram xmi.idref="d1"/></JUDE:UPresentation.diagram>
                  <JUDE:UPresentation.customStyleMap>
                    <JUDE:UPresentation.styleProperty key="fill.color" value="#CCCCFF"/>
                  </JUDE:UPresentation.customStyleMap>
                  <JUDE:JomtPresentation.location><XMI.field>40.0</XMI.field><XMI.field>20.0</XMI.field></JUDE:JomtPresentation.location>
                </JUDE:ClassifierPresentation>
                <JUDE:MessagePresentation xmi.id="mp1" depth="-4" label="go"/>
                <JUDE:MessageCLPresentation xmi.id="mcl1" depth="1">
                  <JUDE:MessageCLPresentation.namePresentation>
                    <JUDE:LabelPresentation xmi.id="ml1" label="1%3A+go%28%29" width="85.0">
                      <JUDE:JomtPresentation.location><XMI.field>259.03</XMI.field><XMI.field>100.0</XMI.field></JUDE:JomtPresentation.location>
                    </JUDE:LabelPresentation>
                  </JUDE:MessageCLPresentation.namePresentation>
                </JUDE:MessageCLPresentation>
                <JUDE:ImagePresentation xmi.id="img1" depth="2" width="16.0">
                  <JUDE:ImagePresentation.imageData>rO0ABXNyAAA</JUDE:ImagePresentation.imageData>
                </JUDE:ImagePresentation>
              </JUDE:Diagram.presentations>
            </JUDE:Diagram>
            """;

    private static String xmi(String content, String exporterVersion) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <XMI xmi.version="1.1" xmlns:UML="org.omg.xmi.namespace.UML" xmlns:JUDE="http://objectclub.esm.co.jp/Jude/namespace/">
                <XMI.header><XMI.documentation><XMI.exporter>Jomt XMI writer</XMI.exporter><XMI.exporterVersion>%s</XMI.exporterVersion><XMI.currentModelVersion>44</XMI.currentModelVersion></XMI.documentation></XMI.header>
                <XMI.content>
                %s
                </XMI.content>
                </XMI>
                """.formatted(exporterVersion, content);
    }

    private record Run(ProjectDiffEngine.Outcome outcome, List<ChangeDTO> changes) {
    }

    private Run compare(String base, String target) throws Exception {
        return compare(xmi(base, "1.2.0.44"), xmi(target, "1.2.0.44"), VERIFIED);
    }

    private Run compare(String baseXmi, String targetXmi, Set<String> verified) throws Exception {
        Path basePath = write("base.xmi", baseXmi);
        Path targetPath = write("target.xmi", targetXmi);
        List<ChangeDTO> changes = new ArrayList<>();
        ProjectDiffEngine.Outcome outcome = ProjectDiffEngine.compare(
                new ProjectDiffEngine.Input(basePath, XmiIndex.read(basePath, Long.MAX_VALUE), "Base"),
                new ProjectDiffEngine.Input(targetPath, XmiIndex.read(targetPath, Long.MAX_VALUE), "Target"),
                verified, changes::add);
        return new Run(outcome, changes);
    }

    private Path write(String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private static int count(Run run, String rule) {
        return run.outcome().normalization().counts().stream().filter(c -> c.rule().equals(rule)).findFirst().orElseThrow().count();
    }

    private static PropertyChangeDTO property(ChangeDTO change, String path) {
        return change.properties().stream().filter(p -> p.path().equals(path)).findFirst()
                .orElseThrow(() -> new AssertionError("no change of " + path + " in " + change.properties()));
    }

    private static ChangeDTO only(Run run) {
        assertEquals(1, run.changes().size(), () -> "expected one change: " + run.changes());
        return run.changes().get(0);
    }

    @Test
    void compare_ok_findsNothingBetweenTheSameXmi() throws Exception {
        Run run = compare(BASE, BASE);

        assertEquals(0, run.outcome().summary().total());
        assertTrue(run.changes().isEmpty());
        assertTrue(run.outcome().normalization().applied());
        assertTrue(run.outcome().warnings().isEmpty());
    }

    @Test
    void compare_ok_doesNotCompareTheNameOfTheRootModel() throws Exception {
        Run run = compare(BASE, BASE.replace("name=\"project\"", "name=\"Sample\""));

        assertTrue(run.changes().isEmpty(), () -> run.changes().toString());
    }

    @Test
    void compare_ok_matchesAMultiplicityAndItsRangeRecreatedWithNewIds() throws Exception {
        // Both IDs change, and the association end role still refers to the multiplicity by its old ID's counterpart
        String target = BASE.replace("\"m1\"", "\"m9\"").replace("\"r1\"", "\"r9\"");

        Run run = compare(BASE, target);

        assertTrue(run.changes().isEmpty(), () -> run.changes().toString());
        assertEquals(1, count(run, "associationEndMultiplicity"));
        assertEquals(1, count(run, "associationEndMultiplicityRange"));
    }

    @Test
    void compare_ok_matchesTheUpdatedVersionTaggedValueOfADiagram() throws Exception {
        Run run = compare(BASE, BASE.replace("\"tv1\"", "\"tv9\""));

        assertTrue(run.changes().isEmpty(), () -> run.changes().toString());
        assertEquals(1, count(run, "diagramUpdatedVersion"));
    }

    @Test
    void compare_ok_reportsARecreatedMultiplicityWithAnotherValueAsModified() throws Exception {
        String target = BASE.replace("\"m1\"", "\"m9\"").replace("\"r1\"", "\"r9\"").replace("upperValue=\"*\"", "upperValue=\"1\"");

        ChangeDTO change = only(compare(BASE, target));

        assertEquals(ChangeKind.MODIFIED, change.kind());
        assertEquals("UML:MultiplicityRange", change.type());
        assertEquals("r9", change.xmiId());
        assertEquals("r1", change.baseXmiId());
        PropertyChangeDTO upper = property(change, "@upperValue");
        assertEquals("*", upper.before());
        assertEquals("1", upper.after());

        // An anonymous part is placed by the association end above it, and the class at that end
        assertEquals("e1", change.xmlContextOwner().xmiId());
        assertEquals("UML:AssociationEnd", change.xmlContextOwner().type());
        assertEquals("Customer", change.xmlContextOwner().participant());
        assertEquals("UML:StructuralFeature.multiplicity/UML:Multiplicity/UML:Multiplicity.range", change.xmlContextOwner().ownerPath());
    }

    @Test
    void compare_ok_keepsAClassDeletedAndRecreatedWithTheSameContents() throws Exception {
        Run run = compare(BASE, BASE.replace("\"c3\"", "\"c8\""));

        assertEquals(2, run.changes().size(), () -> run.changes().toString());
        assertEquals(ChangeKind.ADDED, run.changes().get(0).kind());
        assertEquals("c8", run.changes().get(0).xmiId());
        assertEquals(ChangeKind.REMOVED, run.changes().get(1).kind());
        assertEquals("c3", run.changes().get(1).xmiId());
        assertEquals("Spare", run.changes().get(1).name());
    }

    @Test
    void compare_ok_doesNotMatchWhenMoreThanOneCandidateRemains() throws Exception {
        String twoTags = BASE.replace("<UML:TaggedValue xmi.id=\"tv1\" tag=\"jude.diagram.updated_version\" value=\"12.0.0\"/>",
                "<UML:TaggedValue xmi.id=\"tv1\" tag=\"jude.diagram.updated_version\" value=\"12.0.0\"/>"
                        + "<UML:TaggedValue xmi.id=\"tv2\" tag=\"jude.diagram.updated_version\" value=\"12.0.0\"/>");
        String target = twoTags.replace("\"tv1\"", "\"tv8\"").replace("\"tv2\"", "\"tv9\"");

        Run run = compare(twoTags, target);

        assertEquals(0, count(run, "diagramUpdatedVersion"));
        assertEquals(2, run.outcome().summary().added());
        assertEquals(2, run.outcome().summary().removed());
    }

    @Test
    void compare_ok_matchesByIdBeforeAnyRule() throws Exception {
        // tv1 keeps its ID; a second tagged value of the same tag is new, and must not be taken for tv1
        String target = BASE.replace("<UML:TaggedValue xmi.id=\"tv1\" tag=\"jude.diagram.updated_version\" value=\"12.0.0\"/>",
                "<UML:TaggedValue xmi.id=\"tv1\" tag=\"jude.diagram.updated_version\" value=\"12.0.0\"/>"
                        + "<UML:TaggedValue xmi.id=\"tv5\" tag=\"jude.diagram.updated_version\" value=\"12.0.0\"/>");

        ChangeDTO change = only(compare(BASE, target));

        assertEquals(ChangeKind.ADDED, change.kind());
        assertEquals("tv5", change.xmiId());
    }

    @Test
    void compare_ok_reportsAnElementThatMovedWithBothContexts() throws Exception {
        String classElement = """
                    <UML:Class xmi.id="c1" name="Order">
                      <UML:ModelElement.namespace><UML:Namespace xmi.idref="root"/></UML:ModelElement.namespace>
                    </UML:Class>
                """;
        String moved = BASE.replace(classElement, "")
                .replace("<UML:Package xmi.id=\"pk1\" name=\"sub\">\n      <UML:Namespace.ownedElement>",
                        "<UML:Package xmi.id=\"pk1\" name=\"sub\">\n      <UML:Namespace.ownedElement>" + classElement);
        assertNotEquals(BASE, moved);

        ChangeDTO change = only(compare(BASE, moved));

        assertEquals("c1", change.xmiId());
        PropertyChangeDTO parent = property(change, ChangeDTOAssembler.PARENT_PATH);
        assertEquals("root", parent.before());
        assertEquals("pk1", parent.after());
        assertEquals("root", change.beforeContext().xmiId());
        assertEquals("Base", change.beforeContext().name());
        assertEquals("pk1", change.afterContext().xmiId());
        assertEquals("sub", change.afterContext().name());
    }

    @Test
    void compare_ok_tellsStylesApartByTheirKey() throws Exception {
        String target = BASE.replace("<JUDE:UPresentation.styleProperty key=\"fill.color\" value=\"#CCCCFF\"/>",
                "<JUDE:UPresentation.styleProperty key=\"line.color\" value=\"#000000\"/><JUDE:UPresentation.styleProperty key=\"fill.color\" value=\"#CCCCFF\"/>");

        ChangeDTO change = only(compare(BASE, target));

        assertEquals(1, change.properties().size(), () -> change.properties().toString());
        PropertyChangeDTO added = change.properties().get(0);
        assertEquals("JUDE:UPresentation.customStyleMap/JUDE:UPresentation.styleProperty[key=line.color]/@value", added.path());
        assertEquals(PropertyChangeKind.ADDED, added.change());
        assertEquals("#000000", added.after());
    }

    // A package that lists association ends by reference, the way Sample.asta does, next to the classes it refers to
    private static String packageListing(String... ids) {
        StringBuilder references = new StringBuilder();
        for (String id : ids) {
            references.append("<UML:AssociationEnd xmi.idref=\"").append(id).append("\"/>");
        }
        return """
                <UML:Model xmi.id="root" name="project" isRoot="false">
                  <UML:Namespace.ownedElement>
                    <UML:Class xmi.id="e1" name="One"/>
                    <UML:Class xmi.id="e2" name="Two"/>
                    <UML:Class xmi.id="e3" name="Three"/>
                    <UML:Class xmi.id="e4" name="Four"/>
                    <UML:Package xmi.id="pk" name="Class">
                      <UML:ModelElement.namespace><UML:Namespace xmi.idref="root"/></UML:ModelElement.namespace>
                      <UML:Namespace.ownedElement>%s</UML:Namespace.ownedElement>
                    </UML:Package>
                  </UML:Namespace.ownedElement>
                </UML:Model>
                """.formatted(references);
    }

    private static final String LIST_PATH = "UML:Namespace.ownedElement/UML:AssociationEnd";

    @Test
    void compare_ok_reportsAReferenceRemovedFromTheMiddleOfAListAsRemovedOnly() throws Exception {
        ChangeDTO change = only(compare(packageListing("e1", "e2", "e3", "e4"), packageListing("e1", "e3", "e4")));

        assertEquals("pk", change.xmiId());
        assertEquals(1, change.properties().size(), () -> change.properties().toString());
        PropertyChangeDTO removed = change.properties().get(0);
        assertEquals(LIST_PATH, removed.path());
        assertEquals(PropertyChangeKind.REMOVED, removed.change());
        assertEquals("e2", removed.before());
        assertEquals("Two", removed.beforeRefName());
    }

    @Test
    void compare_ok_reportsAReferenceAddedToTheMiddleOfAListAsAddedOnly() throws Exception {
        ChangeDTO change = only(compare(packageListing("e1", "e2", "e4"), packageListing("e1", "e2", "e3", "e4")));

        assertEquals(1, change.properties().size(), () -> change.properties().toString());
        PropertyChangeDTO added = change.properties().get(0);
        assertEquals(LIST_PATH, added.path());
        assertEquals(PropertyChangeKind.ADDED, added.change());
        assertEquals("e3", added.after());
        assertEquals("Three", added.afterRefName());
    }

    @Test
    void compare_ok_reportsTheSameReferencesInAnotherOrderAsOneReordering() throws Exception {
        ChangeDTO change = only(compare(packageListing("e1", "e2", "e3"), packageListing("e3", "e1", "e2")));

        assertEquals(1, change.properties().size(), () -> change.properties().toString());
        PropertyChangeDTO reordered = change.properties().get(0);
        assertEquals(LIST_PATH, reordered.path());
        assertEquals(PropertyChangeKind.REORDERED, reordered.change());
        assertEquals("e1, e2, e3", reordered.before());
        assertEquals("e3, e1, e2", reordered.after());
        assertEquals("One, Two, Three", reordered.beforeRefName());
        assertEquals("Three, One, Two", reordered.afterRefName());
    }

    @Test
    void compare_ok_matchesAListThatRefersToTheSameElementTwice() throws Exception {
        ChangeDTO change = only(compare(packageListing("e1", "e2", "e1", "e3"), packageListing("e1", "e1", "e3")));

        assertEquals(1, change.properties().size(), () -> change.properties().toString());
        assertEquals(PropertyChangeKind.REMOVED, change.properties().get(0).change());
        assertEquals("e2", change.properties().get(0).before());
    }

    @Test
    void compare_ok_readsAListThatRefersToPartsMatchedByTheRulesAsUnchanged() throws Exception {
        String single = "<UML:AssociationEndRole.multiplicity><UML:Multiplicity xmi.idref=\"m1\"/></UML:AssociationEndRole.multiplicity>";
        String list = "<UML:AssociationEndRole.multiplicity><UML:Multiplicity xmi.idref=\"m1\"/><UML:Multiplicity xmi.idref=\"m1\"/></UML:AssociationEndRole.multiplicity>";
        String base = BASE.replace(single, list);
        assertNotEquals(BASE, base);
        // The multiplicity and its range recreated with new IDs, which the rules match, and the role renamed, so that
        // the role is compared value by value, its list included
        String target = base.replace("\"m1\"", "\"m9\"").replace("\"r1\"", "\"r9\"").replace("name=\"role\"", "name=\"role2\"");

        ChangeDTO change = only(compare(base, target));

        assertEquals("er1", change.xmiId());
        assertEquals(List.of("@name"), change.properties().stream().map(PropertyChangeDTO::path).toList());
    }

    @Test
    void compare_ok_keepsASingleReferenceThatChangedAsModified() throws Exception {
        ChangeDTO change = only(compare(packageListing("e1"), packageListing("e2")));

        assertEquals(1, change.properties().size(), () -> change.properties().toString());
        PropertyChangeDTO modified = change.properties().get(0);
        assertEquals(LIST_PATH + "/@xmi.idref", modified.path());
        assertEquals(PropertyChangeKind.MODIFIED, modified.change());
        assertEquals("One", modified.beforeRefName());
        assertEquals("Two", modified.afterRefName());
    }

    @Test
    void compare_ok_givesAPresentationItsDiagramAndModel() throws Exception {
        ChangeDTO change = only(compare(BASE, BASE.replace("<XMI.field>40.0</XMI.field>", "<XMI.field>80.0</XMI.field>")));

        assertEquals("p1", change.xmiId());
        assertEquals("JUDE:ClassifierPresentation", change.type());
        assertEquals("Order", change.name());
        assertEquals("d1", change.diagram().xmiId());
        assertEquals("Main", change.diagram().name());
        assertEquals("c1", change.model().xmiId());
        assertEquals("Order", change.model().name());
        assertEquals("d1", change.xmlContextOwner().xmiId());
        assertEquals("JUDE:Diagram.presentations", change.xmlContextOwner().ownerPath());
        PropertyChangeDTO x = property(change, "JUDE:JomtPresentation.location/XMI.field[0]");
        assertEquals("40.0", x.before());
        assertEquals("80.0", x.after());
    }

    @Test
    void compare_ok_ignoresTheDepthOfMessagesAndTheDataOfImages() throws Exception {
        String target = BASE.replace("depth=\"-4\"", "depth=\"-2\"").replace("rO0ABXNyAAA", "rO0ABXNyBBB");

        Run run = compare(BASE, target);

        assertTrue(run.changes().isEmpty(), () -> run.changes().toString());
    }

    // Showing a communication diagram makes Astah resize its message labels (confirmed with Sample.asta)
    @Test
    void compare_ok_leavesOutTheResizingOfACommunicationMessageLabel() throws Exception {
        String target = BASE.replace("width=\"85.0\"", "width=\"84.697265625\"").replace("<XMI.field>259.03</XMI.field>", "<XMI.field>259.18</XMI.field>");

        Run run = compare(BASE, target);

        assertTrue(run.changes().isEmpty(), () -> run.changes().toString());
        assertEquals(1, count(run, "communicationMessageLabelSize"));
    }

    @Test
    void compare_ok_reportsAMessageLabelResizedBeyondTheTolerance() throws Exception {
        ChangeDTO change = only(compare(BASE, BASE.replace("width=\"85.0\"", "width=\"80.0\"")));

        assertEquals("ml1", change.xmiId());
        assertEquals("80.0", property(change, "@width").after());
    }

    @Test
    void compare_ok_reportsEveryDifferenceOfAMessageLabelThatAlsoChangedOtherwise() throws Exception {
        String target = BASE.replace("width=\"85.0\"", "width=\"84.697265625\"").replace("label=\"1%3A+go%28%29\"", "label=\"1%3A+stop%28%29\"");

        ChangeDTO change = only(compare(BASE, target));

        assertEquals("1: stop()", property(change, "@label").after());
        assertEquals("84.697265625", property(change, "@width").after());
    }

    @Test
    void compare_ok_stillComparesTheDepthOfOtherPresentations() throws Exception {
        ChangeDTO change = only(compare(BASE, BASE.replace("depth=\"3\"", "depth=\"1\"")));

        assertEquals("1", property(change, "@depth").after());
    }

    @Test
    void compare_ok_showsDecodedNamesAndComparesThemAsWritten() throws Exception {
        ChangeDTO change = only(compare(BASE, BASE.replace("name=\"Order\"", "name=\"New+Order\"")));

        PropertyChangeDTO name = property(change, "@name");
        assertEquals("Order", name.before());
        assertEquals("New Order", name.after());
        assertEquals(ValueEncoding.DECODED, name.encoding());
        assertEquals("New Order", change.name());
    }

    @Test
    void compare_ok_namesWhatAReferenceRefersTo() throws Exception {
        ChangeDTO change = only(compare(BASE, BASE.replace("<UML:Classifier xmi.idref=\"c2\"/>", "<UML:Classifier xmi.idref=\"c1\"/>")));

        PropertyChangeDTO participant = property(change, "UML:AssociationEnd.participant/UML:Classifier/@xmi.idref");
        assertEquals("c2", participant.before());
        assertEquals("Customer", participant.beforeRefName());
        assertEquals("c1", participant.after());
        assertEquals("Order", participant.afterRefName());
    }

    @Test
    void compare_ok_doesNotMatchRecreatedPartsOfAnXmiVersionNotChecked() throws Exception {
        String target = BASE.replace("\"m1\"", "\"m9\"").replace("\"r1\"", "\"r9\"");

        Run run = compare(xmi(BASE, "1.2.0.45"), xmi(target, "1.2.0.45"), VERIFIED);

        assertFalse(run.outcome().normalization().applied());
        // Both sides call for the same note, which is given once, for both
        assertEquals(1, run.outcome().warnings().size(), () -> run.outcome().warnings().toString());
        assertTrue(run.outcome().warnings().get(0).contains("1.2.0.45/44"), run.outcome().warnings().get(0));
        assertTrue(run.outcome().warnings().get(0).contains("both the base, 'Base', and the target, 'Target'"), run.outcome().warnings().get(0));
        // The multiplicity, its range, and the role that refers to the multiplicity
        assertEquals(2, run.outcome().summary().added());
        assertEquals(2, run.outcome().summary().removed());
        assertEquals(1, run.outcome().summary().modified());
    }

    @Test
    void compare_ok_ordersAddedAndModifiedByTheTargetThenRemovedByTheBase() throws Exception {
        String target = BASE.replace("\"c3\"", "\"c8\"").replace("name=\"Customer\"", "name=\"Client\"");

        Run run = compare(BASE, target);

        assertEquals(List.of("c2", "c8", "c3"), run.changes().stream().map(ChangeDTO::xmiId).toList());
        assertEquals(List.of(ChangeKind.MODIFIED, ChangeKind.ADDED, ChangeKind.REMOVED), run.changes().stream().map(ChangeDTO::kind).toList());
    }

    @Test
    void read_ng_refusesTwoElementsWithTheSameId() throws Exception {
        Path path = write("dup.xmi", xmi(BASE.replace("\"c3\"", "\"c2\""), "1.2.0.44"));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> XmiIndex.read(path, Long.MAX_VALUE));
        assertTrue(e.getMessage().contains("c2"), e.getMessage());
    }

    @Test
    void read_ng_refusesAnXmiLargerThanTheLimit() throws Exception {
        Path path = write("big.xmi", xmi(BASE, "1.2.0.44"));

        assertThrows(IllegalStateException.class, () -> XmiIndex.read(path, 100));
    }

    @Test
    void read_ng_doesNotResolveExternalEntities() throws Exception {
        Path secret = write("secret.txt", "SECRET");
        String withEntity = "<?xml version=\"1.0\"?>\n<!DOCTYPE XMI [<!ENTITY x SYSTEM \"" + secret.toUri() + "\">]>\n"
                + xmi(BASE.replace("kept+as+is", "&x;"), "1.2.0.44").replaceFirst("<\\?xml[^>]*>", "");
        Path path = write("entity.xmi", withEntity);

        // The parser refuses the DTD rather than read the file it names
        assertThrows(IllegalStateException.class, () -> XmiIndex.read(path, Long.MAX_VALUE));
    }
}
