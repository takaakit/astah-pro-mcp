package com.astahpromcp.tool.astah.pro.review;

import java.util.ArrayList;
import java.util.List;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.StepsDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

@Slf4j
public class TerminologyConsistencyTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public TerminologyConsistencyTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
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
            log.error("Failed to create terminology consistency tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "terminology_consistency_maintenance_steps",
                "Return the steps that MCP client (you) MUST follow to maintain terminology consistency. When you need to eliminate terminology inconsistencies and ensure consistent terminology across model elements and diagrams, call this tool and then perform the work in strict accordance with the returned steps.",
                this::getSteps,
                NoInputDTO.class,
                StepsDTO.class));
    }

    private StepsDTO getSteps(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get terminology consistency maintenance steps: {}", param);

        String contents = """
If you have sub-agent capability, you MUST assign all of the following steps to a sub-agent. If the sub-agent makes any corrections, it MUST report the correction details back to the main agent.

Follow these steps to maintain terminology consistency across model elements and diagrams:

1. Load any existing terminology resource that defines the agreed vocabulary for the target domain, if one is available — for example, a glossary, terminology dictionary, ontology, data dictionary, or naming convention document, whether it is a prose document or structured data. Treat it as the preferred terminology in the subsequent steps. If no such resource exists, skip this step.

2. Retrieve the names, labels, and definitions of all elements using the following tools.
   - get_info_of_all_named_elements
   - get_chunk_of_all_named_elements
   - get_info_of_all_prsts
   - get_chunk_of_all_prsts
   - get_info_of_all_definitions
   - get_chunk_of_all_definitions

3. Build an understanding of the domain and context that the model represents. Read the diagrams, packages, stereotypes, relationships, and definitions to grasp what domain the model describes and which context each element belongs to. This understanding is what lets you judge whether two terms mean the same thing: terminology consistency is judged within a context, so the same word used in two different contexts may legitimately denote different concepts.

4. Against that understanding, identify three kinds of terminology inconsistency across names, labels, and definitions.
   4-1. Spelling/notation variants: one and the same term written in different ways (e.g., "e-mail" vs "email"; in camelCase an uppercase letter marks a word boundary, so "orderId" and "orderID" are the same term).
   4-2. Synonyms: different terms that denote the same domain concept within one context (e.g., "Customer" and "User" when the model treats them as the same role). Do NOT judge from the name string alone; infer each element's concept from its definition, type, stereotype, and relationships.
   4-3. Homonyms: one and the same term used for different domain concepts within one context (e.g., "Account" meaning a login account in one place and a billing account in another).

5. If any inconsistency is found, resolve it.
   5-1. Following the domain's vocabulary, decide the single preferred term for each concept, and confirm that genuinely distinct concepts keep distinct terms.
   5-2. A spelling/notation variant (4-1) may be unified directly. Unifying synonyms (4-2) or disambiguating homonyms (4-3) rests on a domain judgment; when you cannot make it with confidence, present the candidate set to the user and get confirmation before changing anything.
   5-3. Apply the corrections.
        """;

        return new StepsDTO(contents);
    }
}
