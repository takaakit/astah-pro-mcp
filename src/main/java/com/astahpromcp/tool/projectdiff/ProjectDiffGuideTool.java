package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProjectDiffGuideTool implements ToolProvider {

    public ProjectDiffGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "proj_diff_guide",
                    "MCP client (you) MUST call this tool function before finding out what has changed in a project, to understand which projects to compare, how to get the file of a git revision, how to start the comparison and get its result, and how to read it.",
                    this::getGuide,
                    NoInputDTO.class,
                    GuideDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create project diff guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(NoInputDTO param) throws Exception {
        String contents = """
Finding what has changed between two versions of a project takes two tool functions: one starts the comparison and returns a 'diffId' at once, and the other returns the result for that 'diffId', page by page.


Which projects to pass when you start a comparison:
* The open project, unsaved changes included, against its state before the edits: leave 'targetProjectPath' empty, and set 'baseProjectPath' to the path of the open project's file, which the project path tool returns, to compare with the last save, or, for a project in git, to the file of the 'HEAD' revision (see below) to compare with the last commit. The two differ when the project was saved after the last commit.
* The open project against a project file elsewhere, such as a copy or an earlier revision of it: set 'baseProjectPath' to that file, and leave 'targetProjectPath' empty.
* Two project files: set 'baseProjectPath' to one and 'targetProjectPath' to the other. Do not open them in Astah for this; the comparison reads them itself, and opening one would replace the project the user has open.
* Two git revisions of a project file: get the file of each revision (see below), and set them as 'baseProjectPath' and 'targetProjectPath'.
* In every case, the two must be versions of the same project, because elements are matched by their IDs: projects made separately show up as almost everything removed and added.


Getting the file of a git revision:
* In bash or cmd, run the following, where <path> is the project file's path from the repository root and <file> is in a temporary folder outside the repository. Keep the quotes, because the paths may contain spaces.
    git show "<revision>:<path>" > "<file>"
* From PowerShell, run it through cmd as follows. Do not redirect with the '>' of PowerShell itself: Windows PowerShell 5.1 rewrites the bytes, and Astah cannot read the file.
    cmd /c 'git show "<revision>:<path>" > "<file>"'
* The files are copied before the call that starts the comparison returns, so they can be deleted once it has returned a 'diffId'.


Starting a comparison and getting its result:
* Set 'requestId' to a new unique value, such as a UUID, for each comparison. If the call that starts it gets no answer (a timeout, for instance), call again with the same 'requestId' and the same paths: you get the 'diffId' of the comparison already started instead of starting another. This holds for a comparison that failed as well: the same 'requestId' gets it back, failed, while it is kept, so start a failed comparison again with a new 'requestId'.
* Each project file is read by a separate Astah process, which takes from a few seconds for a small project to about half a minute for a project with thousands of classes.
* Getting the result waits up to about {RESULT_WAIT_SECONDS} seconds for the comparison to finish. While 'status' is 'running', ask for it again with 'startIndex' and 'startPropertyIndex' 0.


The result lists the elements that differ between the XMI that Astah writes for each project (XMI 1.1 with UML 1.4 elements, 'UML:*', and Astah's own, 'JUDE:*').


Changes:
* Each change is one XMI element with an xmi.id. 'type' is its XMI element name: UML:Class, UML:Attribute, UML:Operation, UML:AssociationEnd, UML:Dependency, JUDE:Diagram and the like for the model, and JUDE:*Presentation for what diagrams show (JUDE:ClassifierPresentation is a class box, for instance).
* 'kind' is 'added' (only in the target), 'removed' (only in the base), or 'modified'. For 'added' and 'removed', 'properties' lists every value the element holds; for 'modified', only the values that changed.
* The changes are in XMI order: added and modified elements in the target's order, then removed ones in the base's order.
* An edit often changes related elements as well, such as a frame that resizes, a label that moves, or an object node named after a renamed class. These are real changes and are listed.


Values ('properties'):
* 'path' says where a value sits in the XMI, relative to the element: '@name' is an attribute of the element itself, and a longer path leads to a child element; 'JUDE:JomtPresentation.location/XMI.field[0]' is the text of a child element; '[n]' counts the children of the same tag from 0; a segment such as 'JUDE:UPresentation.styleProperty[key=fill.color]' is told apart by its key. '(parent)' is the element the change moved this one to or from in the XMI.
* For a reference, 'before' and 'after' are xmi.ids, and 'beforeRefName' and 'afterRefName' name the elements they refer to.
* A list of references, the children of the same tag under the same path that hold nothing but an xmi.idref (such as 'UML:Namespace.ownedElement/UML:AssociationEnd' of a package, or 'JUDE:UPresentation.clients/JUDE:AssociationPresentation' of a presentation), is compared as the list of the elements it refers to. A reference removed from it or added to it shows as one value 'removed' or 'added', whose 'path' is the path of the list without '[n]'; the references after it do not show. The same references in another order show as one value 'reordered', whose 'before' and 'after' list the xmi.ids in order, separated by ', ', and 'beforeRefName' and 'afterRefName' their names.
* A long value is cut short in the answer, with its whole length noted: a value longer than {MAX_RESPONSE_VALUE_CHARS} characters is cut to about that length, and when not even one property of a change fits in a page so (see 'Pages'), a value of that part of the change longer than {LAST_RESORT_VALUE_CHARS} characters is cut to about that length. A value that the note would leave no shorter is kept whole, and 'path' is never cut. A property with a value cut short has 'cut' true; 'summary.valuesCut' counts the values cut in the answer. A modified value is cut so that the part that changed shows: what is left out at its start is the same in 'before' and 'after'. The whole value is in the file at 'fullDiffPath'.
* Values are shown decoded where the XMI is known to URL-encode them ('encoding' is 'decoded'); 'raw' marks a value shown as written that is not encoded; 'unknown' marks a value shown as written that may still be URL-encoded.


Presentations:
* A presentation's 'diagram' and 'model' name its diagram and the model element it shows.
* Its position is 'JUDE:JomtPresentation.location/XMI.field[0]' (x) and '[1]' (y); '@width' and '@height' are its size; '@depth' is its z-order, a smaller value being in front; 'JUDE:PathPresentation.points' holds the bends of a line; styles such as 'fill.color', 'line.color' and 'font.color' are 'JUDE:UPresentation.styleProperty[key=...]/@value'.


Tagged values and stereotypes:
* Some properties are Astah tagged values, UML:TaggedValue elements with '@tag' and '@value': jude.multi_language.alias1 and alias2 (alias 1 and 2), jude.type_modifier (type modifier), jude.hyperlink (a hyperlink, as type=url,name=...,path=...,comment=...), jude.enumeration_literal (an attribute that is an enumeration literal), jude.require.id and jude.require.text (a requirement's ID and text), alias3 (the physical name of an ER entity or attribute), jude.erentity.kind, jude.modifier (length and precision of an ER attribute), jude.domain.default, jude.datatype.length and jude.datatype.defaultLength, object_aggregation_kind and object_navigable_type (link ends of instance specifications).
* Requirements and test cases are UML:Class elements with stereotypes, ER domains and datatypes are UML:Class too, and a Realization is a UML:Usage with the stereotype 'realize'.


Context:
* 'xmlContextOwner' is the nearest ancestor in the XMI that gives an element its context: for a model element, the nearest one with a name attribute; for a presentation, its diagram or parent presentation. For the range of a multiplicity, it is the association end, and 'ownerPath' leads from it down to the element. 'participant' names the class at the end of an association end without a name.
* This is the XMI containment, not the owner the Astah API reports.
* When an element moved, 'beforeContext' and 'afterContext' say from where to where.


IDs:
* 'xmiId' is the element's ID in the XMI: the target's for 'added' and 'modified', the base's for 'removed'. For two project files, and for removed elements, it names nothing in the open project.
* Even for the open project, the other tools do not accept every such ID (multiplicities, template parameters, the top state of a state machine, elements inside data flow diagrams) and answer with an error.
* The open project is read once, while the call that starts the comparison runs, and the result compares that snapshot. It may have changed since, so read its current state with the other tools before you edit based on the result.


Parts recreated with a new xmi.id:
* Some parts are recreated with a new xmi.id whenever their value changes: multiplicities, constraint bodies, the parameter that holds a return type, transition events, slot values. The multiplicity of an association end is matched across and shown as a modified value, with the base's ID in 'baseXmiId'; the others show as one removed and one added element under the same 'xmlContextOwner'.
* Showing a communication diagram makes Astah resize the labels of its messages without marking the project modified; differences of less than 2 pixels in their width and position are left out.
* 'normalization' says how many parts each of these rules matched or left out.


Pages:
* The result comes page by page. Pass 'startIndex' 0 and 'startPropertyIndex' 0 for the first page; while 'nextIndex' is not -1, pass it as 'startIndex' and 'nextPropertyIndex' as 'startPropertyIndex' to get the next page. 'startIndex' of the answer is where its 'changes' start among the changes of the whole result, and 'truncated' is true while more pages follow.
* A change too large for a page is split across pages by its properties. Each part has the same 'xmiId'; 'propertyCount' is the number of properties of the whole change, and 'propertyStart' is where the 'properties' of the part start among them. A page that ends inside a change has 'nextPropertyIndex' above 0, and the next page goes on with that change: join the 'properties' of its parts in order. Every other change comes whole, with 'propertyStart' 0 and as many 'properties' as 'propertyCount'.
* 'summary' gives the numbers of the whole result, except 'returned' and 'valuesCut', which are about the page: the changes, or parts of changes, in it and the values cut in it.
* 'shortened' is true when fields other than 'changes' were shortened to fit. When 'startIndex' is 0, 'truncated' and 'shortened' are false and 'summary.valuesCut' is 0, the answer holds the whole result, as the file does.
* The whole result is also a JSON file at 'fullDiffPath', for a client that can read the local file system.
* A result not yet fetched is kept until 'expiresAt'. Once fetched, it may be deleted to make room for other comparisons when {READ_GRACE_MINUTES} minutes have passed since it was last fetched, so read all its pages without long pauses.


What else to keep in mind:
* 'notCompared' lists data that is never compared, or left out under a condition: zero changes does not mean the projects are identical in these.
* 'warnings' lists what else affects how to read the result, each saying whether it is about the base, the target, or both: a file saved by an older Astah that converted it on loading (see 'projectModelVersion'), which can show differences even against itself and from one comparison to the next, or projects whose elements come from referenced projects.
* When 'status' is 'failed', 'failure' says at which stage and why, and 'retryable' whether starting the same comparison again, with a new 'requestId', may succeed.
""".replace("{READ_GRACE_MINUTES}", String.valueOf(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES))
            .replace("{RESULT_WAIT_SECONDS}", String.valueOf(McpServerConfig.PROJECT_DIFF_RESULT_WAIT_SECONDS))
            .replace("{MAX_RESPONSE_VALUE_CHARS}", String.valueOf(McpServerConfig.PROJECT_DIFF_MAX_RESPONSE_VALUE_CHARS))
            .replace("{LAST_RESORT_VALUE_CHARS}", String.valueOf(McpServerConfig.PROJECT_DIFF_LAST_RESORT_VALUE_CHARS));

        return new GuideDTO(contents);
    }
}
