package com.astahpromcp.tool.astah.pro.preliminary;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.spec.McpSchema;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
public class PreliminaryLayoutTool implements ToolProvider {

    private static final String EXAMPLE_RESOURCE_DIR = "/preliminary-layout/";
    private static final String EXAMPLE_SVG_SUFFIX = "-preliminary-layout-example.svg";
    private static final String EXAMPLE_PNG_SUFFIX = "-preliminary-layout-example.png";

    private static final List<DiagramTypeExample> EXAMPLES = List.of(
        new DiagramTypeExample("use-case-diagram", "Use Case Diagram"),
        new DiagramTypeExample("class-diagram", "Class Diagram"),
        new DiagramTypeExample("sequence-diagram", "Sequence Diagram"),
        new DiagramTypeExample("activity-diagram", "Activity Diagram"),
        new DiagramTypeExample("state-machine-diagram", "State Machine Diagram"),
        new DiagramTypeExample("requirement-diagram", "Requirement Diagram"));

    private record DiagramTypeExample(String resourceBaseName, String displayName) {
    }

    private final boolean includeEditTools;

    public PreliminaryLayoutTool(boolean includeEditTools) {
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create preliminary layout tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningContents(
                "preliminary_layout_steps",
                "Return the steps that the MCP client (you) MUST follow to create and refine a preliminary layout of a diagram, together with a preliminary layout SVG code example and its rendered PNG image for each diagram type. Call this tool at one point only in the course of creating a new diagram: after you have decided what that diagram is to show - the elements to appear on it and the relationships between them - and immediately before you begin working out where on the diagram its presentations are to be placed. Do not call it while what the diagram is to show is still undecided, and do not call it once you have begun deciding where a presentation goes. Whether the diagram itself has been created by then makes no difference. Having called it, perform the work in strict accordance with the returned steps.",
                this::getSteps,
                NoInputDTO.class));
    }

    private List<McpSchema.Content> getSteps(NoInputDTO param) throws Exception {
        log.debug("Get preliminary layout steps: {}", param);

        String contents = """
# Introspection Loop for Creating a Preliminary Layout Before Laying Out a New Diagram
Follow these steps to create and refine a preliminary layout of a diagram before you work out where on that diagram its presentations are to be placed, so that the initial layout of the diagram is of good quality. Run this introspective loop once per diagram to be created. Do not skip, reorder, or merge steps.

Run this loop at one point only in the course of creating a new diagram: after what that diagram is to show has been decided, and immediately before you begin working out where on the diagram its presentations are to be placed. Its two boundaries are these.
- After what the diagram is to show has been decided. That means the elements to appear on the diagram and the relationships between them are settled, so that step 1 can enumerate them without deciding anything further. While any of that is still open, settle it first and do not start this loop: a layout refined against a set of elements that then changes has been refined against the wrong requirement, and the rounds spent on it are wasted.
- Immediately before the placement is worked out. That means no decision has yet been made about where any presentation goes. This loop is how that placement is worked out: let nothing about it be settled beforehand, and let nothing decided outside this loop displace what it produces.
Both boundaries are about what has been decided, not about what has been edited in the Astah project. Whether the diagram itself has been created when this loop runs makes no difference, and neither does when the presentations are actually placed on it. This loop is therefore neither a planning exercise to be run ahead of time, nor a means of reworking a placement already decided - once that placement has been decided, adjust it directly instead of running this loop.

This procedure keeps its working artefacts - the element list, the SVG code and the evaluation result - in this conversation rather than in files, and every step refers back to what you yourself produced in an earlier step. Label each of those outputs with the name of the diagram being laid out, so that loops run for different diagrams do not get mixed up, and label the SVG code and the evaluation result with the round number as well, so that the rounds run for one diagram do not get mixed up either. Only the finished layout is written to a file, in step 6.

1. Enumerate the rectangles and lines to be placed on the diagram to be laid out.
   Referring to the "Rectangles and Lines to Draw in a Preliminary Layout" section below, output the enumerated result in the format shown below.
   Treat this output as the requirement for the whole introspective loop - the definition of WHAT is to be drawn, not the definition of the correct layout - and refer back to it in every round of step 3 and step 5. Never revise it once it has been written; it is the fixed reference that every round is checked against.

   > ## Requirement
   > Describe in one to three lines what this diagram is meant to express.
   >
   > ## Rectangles
   > - Class3 (class)
   > - Class4 (class)
   >
   > ## Lines
   > - holds (association): Class3 -> Class4
   > - (generalization): Class1 -> Class0

2. Understand the coding rules for the preliminary layout SVG code, and obtain the criteria this loop is judged against.
   - Read the "SVG Coding Rules" section below to understand how to write the SVG code.
   - Read the preliminary layout SVG code example of each diagram type, and the PNG image rendered from that code, both of which are included in the return value of this tool, to understand how to write the SVG code.
   - Call the `dgm_layout_guide` tool and the `dgm_layout_anti_patterns` tool and read what they return. These are the criteria this loop is judged against: step 3 writes the layout so as to follow the guide and to avoid the anti-patterns, and step 5 evaluates the layout against those same criteria. Obtain them here, before the first SVG code is written, rather than when step 5 needs them - a first draft written against the criteria it will be judged by reaches an acceptable layout in fewer rounds.

3. Write the SVG code that represents the preliminary layout.
   The rounds are numbered from zero: round 0 writes the initial draft, and each round that step 5 sends back here is one higher than the last.
   Take the following as input.
   - Round 0
     - The "Requirement", "Rectangles" and "Lines" you output in step 1 (the definition of what is to be drawn)
   - Round 1 and later (when step 5 has sent you back here)
     - The "Requirement", "Rectangles" and "Lines" you output in step 1 (the definition of what is to be drawn)
     - The SVG code you wrote in the previous round (the preliminary layout to be improved)
     - The critique and the revision suggestions produced in step 5 of the previous round
   If the step 1 output is no longer visible to you, redo step 1 before continuing.

   Write the layout so that it follows the diagram layout guide and avoids the layout anti-patterns you obtained in step 2. Those are the criteria step 5 will judge it by, so write against them from round 0 rather than waiting to be told in a critique.

   From round 1 on, revise the places pointed out in the revision suggestions and leave the rest of the previous SVG code as it is. You may nevertheless rewrite the whole of it when you judge that the layout has to be restructured as a whole.
   After writing it, use the data-name and data-type attributes to confirm that every rectangle and line listed in step 1 is drawn, and that nothing absent from that list is drawn. Fix any excess or omission.

4. Render the SVG code you wrote in step 3 into a PNG image.
   Call the overlay_svg_img_on_dgm_img tool to convert the SVG code into a PNG image. Specify an empty string as that tool's target diagram ID, so that the image shows the preliminary layout by itself rather than overlaid on a diagram. Specify the empty string even when the diagram this layout is for already exists; the layout is to be judged on its own.
   The PNG image returned by that tool is what step 5 evaluates. Do not save it to a file.

5. Evaluate the preliminary layout, referring to the PNG image and to the SVG code.
   Carry out this step as a reviewer of the layout, not as the author of steps 3 and 4. Specifically:
   - Keep to this order. First look only at the PNG image returned by the tool in step 4, and enumerate the places that have a problem. Only then read the SVG code, and identify which element each of those places belongs to from the data-name and data-type attributes. Reading the SVG code first drags your judgement towards the intent behind the drawing and makes you overlook problems in how it actually looks.
   - Use the SVG code only to identify elements and to check coordinates and sizes, never as the grounds for judging whether the layout is good. Never use the intent behind the drawing - "this was meant to be placed like that" - as a reason for concluding that there is no problem. Judge by how the image actually looks.
   - Choose the primary evidence according to what you are evaluating.
     - Judge from the PNG image whatever concerns visual impression: overall readability, whether an element is hidden behind another, whether a name runs outside the rectangle it belongs to, how line crossings look, and uneven density of elements.
     - Check against the coordinates in the SVG code whatever can be decided exactly: overlap between rectangles, the number of vertices in a line path, and the direction of arrows.
   - Take the stance of reviewing a layout produced by somebody else. Do not start from the conclusion that no revision is needed; look first for the points that ought to be raised.
   - Refer back to the step 1 output and confirm that nothing is missing or superfluous with respect to the "Requirement", and that every element listed under "Rectangles" and "Lines" is drawn.
   - Produce the following four sections as the evaluation result, and write them in this order.
     - Visual findings: what looks wrong in the PNG image, written from the image alone. Name the elements by the names drawn in the image, and say whereabouts on the image each place is. Do not write a single coordinate value in this section: coordinates exist only in the SVG code, so a coordinate appearing here is evidence that you read the code before you looked at the image. If one does appear, rewrite the section using only what the image shows.
     - Element identification: for each visual finding, the element it belongs to, taken from the data-name and data-type attributes of the SVG code. An element whose data-name is empty is identified here by its data-type and its position.
     - Critique: point out what is wrong with the layout, naming the elements concerned. Where the guide or the anti-patterns you obtained in step 2 have an item for it, cite that item; where they have none, say so and raise the point anyway. They are the floor this layout has to clear, not the definition of a finished one.
     - Revision suggestions: for each point raised in the critique, describe concretely which element is to be moved, in which direction, and how, giving coordinates.
   - When a point should not be acted on, record it as set aside with the reason instead of a revision suggestion, and do not raise it again in a later round.
   - Go on to step 6 when this round's evaluation produced no revision suggestion at all, or when the round you have just evaluated is round 3. This loop therefore writes at most four SVG codes: round 0 and rounds 1 to 3.
   - Otherwise, carry the critique and the revision suggestions into the next round, and go back to step 3 as the next round.

6. Save the finished preliminary layout.
   Write the SVG code of the last round you wrote - the round step 5 sent you here from - to the file {diagram-name}.svg in the directory below, creating the directory if it does not exist. Overwrite the file if one already exists.
   This is the only file this procedure produces. It is the record of the preliminary layout, and the reference for placing the presentations of the diagram it was made for.
   %1$s


# Rectangles and Lines to Draw in a Preliminary Layout
The rectangles and lines to be drawn in the preliminary layout are listed below per diagram type. You may draw elements other than the ones listed here whenever they are needed.

Use Case Diagram:
- Rectangles
  - Use case
  - Actor
  - System boundary
  - Note
- Lines
  - Association
  - Include
  - Extend
  - Note anchor (only for notes on elements)

Class Diagram:
- Rectangles
  - Class
  - Interface
  - Enumeration
  - Package
  - Note
- Lines
  - Association (including aggregation and composition)
  - Generalization
  - Realization
  - Dependency
  - Note anchor (only for notes on elements)

Sequence Diagram:
- Rectangles
  - Lifeline head
  - Activation (execution specification)
  - Combined fragment
  - Interaction use
  - Note
- Lines
  - Lifeline vertical line
  - Message (including self message)
  - Note anchor (only for notes on elements)

Activity Diagram:
- Rectangles
  - Action
  - Object node
  - Initial node / final node
  - Decision node / merge node
  - Fork node / join node
  - Input pin / output pin
  - Partition (swimlane)
  - Note
- Lines
  - Control flow
  - Object flow
  - Note anchor (only for notes on elements)

State Machine Diagram:
- Rectangles
  - State
  - Region within a state
  - Submachine state
  - Initial pseudostate
  - Final state
  - Note
- Lines
  - Transition
  - Note anchor (only for notes on elements)

Requirement Diagram:
- Rectangles
  - Requirement
  - Test case
  - Actor
  - Note
- Lines
  - Nesting
  - DeriveReqt
  - Copy
  - Satisfy
  - Verify
  - Refine
  - Trace
  - Dependency
  - Note anchor (only for notes on elements)


# SVG Coding Rules
- Use rectangles, lines and text only. The purpose is solely to refine the layout, so the detailed appearance the elements have on a diagram need not be reproduced in the SVG code. Text is drawn only to name the elements, for the reason given in the text rules below.
- Always specify the coordinates and the size of a rectangle as integer values, never as decimal values.
- Estimate the size of a rectangle in the diagram coordinates Astah uses. For a class the following approximation holds:
    width  = 20 + 6.7 x (number of halfwidth characters) + 12.4 x (number of fullwidth characters), counted on the widest line of text the element displays
    height = 42 + 15 x (number of attributes and operations displayed)
  The widest line is not always the element name: an attribute or an operation signature is often wider, and it widens the rectangle even though attributes and operations are not drawn in the SVG code. Halfwidth characters are set in a proportional font, so a name in capitals runs wider than this approximation and a name in lower case runs narrower. Size the rectangles of the other diagram types in the same way, from the widest line of text they display and from the number of sub elements they contain (the sub states contained in a state, and so on). The purpose is solely to refine the layout, so the attributes and operations of a class need not be drawn in the SVG code.
- Always specify the coordinates of a line path as integer values, never as decimal values.
- Use absolute coordinates, never relative coordinates.
- Use solid lines of 1px width only.
- Use black lines only.
- Use solid rectangle borders of 1px width only.
- Use black rectangle borders only.
- Fill rectangles with white only.
- Set the data-name attribute of a rectangle or a line to the name of the element it corresponds to (class name, lifeline name, and so on). Set an empty string when the corresponding element has no name.
- Set the data-type attribute of a rectangle or a line to the type of the element it corresponds to (class, association, and so on). Always set one of the presentation types; a tool is available for retrieving the list of presentation types.
- Draw the name of an element as text, so that the rendered PNG image shows which rectangle is which. Without the names every rectangle looks the same in the image, and step 5 cannot judge anything that depends on which element is where.
- Draw a name only where Astah itself displays it on the diagram: on the first line inside the rectangle for a rectangle that displays its name. Where Astah displays the name outside the rectangle, as it does under an actor symbol, draw it there instead. Draw no text where Astah displays no name - the activations of a lifeline, the vertical line of a lifeline, an initial or final node, a decision or merge node, a fork or join node - and none for an element whose data-name is empty. A name drawn where the real diagram shows none fills the image with repetitions and moves it away from what the diagram will look like.
- Break a name that Astah displays on several lines into one text element per line, spaced 15 apart, so that it occupies the same number of lines as it will on the diagram.
- For a rectangle whose width you estimated from the name it displays, the rendered image is the check on that estimate: a name that runs outside its rectangle means the width was estimated too small. Widen the rectangle.
- Use one text style only: font-family="sans-serif", font-size="12", fill="#000". Do not set any other font property. The width estimate above works out to a font of about this size, so changing it would weaken the check.
- Do not set a data-name or a data-type attribute on a text element. A text element is the label of the rectangle or the line it names, not an element of its own, and step 3 does not count it when it confirms that nothing is missing or superfluous.
- Put the text elements in their own group, after the rectangles and after the lines, so that no name is hidden behind a rectangle or crossed out by a line.
- Make the width, height and viewBox of the svg element cover the text as well, so that no name is cut off at the edge of the image.
- You may use curves and lines with several vertices. For instance, when a line would overlap a rectangle, you may give the line several vertices so that it goes around the rectangle.
- You may put a symbol such as a hollow arrowhead, a filled arrowhead, a hollow diamond or a filled diamond on the end of a line.
""".formatted(McpServerConfig.WORKSPACE_DIR.resolve("preliminary-layout"));

        List<McpSchema.Content> results = new ArrayList<>();
        results.add(McpSchema.TextContent.builder(contents).build());

        // Pair each example's SVG source with the image it renders to, so that the correspondence is explicit.
        for (DiagramTypeExample example : EXAMPLES) {
            results.add(loadSvgTextContent(example));
            results.add(loadPngImageContent(example));
        }

        return results;
    }

    private McpSchema.TextContent loadSvgTextContent(DiagramTypeExample example) throws Exception {
        String resourcePath = EXAMPLE_RESOURCE_DIR + example.resourceBaseName() + EXAMPLE_SVG_SUFFIX;
        String svgCode = loadResource(resourcePath, StandardCharsets.UTF_8);

        String text = "%s - preliminary layout SVG code example. The PNG image that follows is what this SVG code renders to.%n%n%s"
                .formatted(example.displayName(), svgCode);

        return McpSchema.TextContent.builder(text).build();
    }

    private McpSchema.ImageContent loadPngImageContent(DiagramTypeExample example) throws Exception {
        String resourcePath = EXAMPLE_RESOURCE_DIR + example.resourceBaseName() + EXAMPLE_PNG_SUFFIX;
        byte[] bytes = loadResource(resourcePath);

        String encoded = Base64.getEncoder().encodeToString(bytes);
        return McpSchema.ImageContent.builder(encoded, "image/png").build();
    }

    private String loadResource(String resourcePath, java.nio.charset.Charset charset) throws Exception {
        return new String(loadResource(resourcePath), charset);
    }

    private byte[] loadResource(String resourcePath) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new Exception("Resource not found on classpath: " + resourcePath);
            }
            return stream.readAllBytes();
        }
    }
}
