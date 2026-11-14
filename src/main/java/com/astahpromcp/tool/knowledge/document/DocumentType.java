package com.astahpromcp.tool.knowledge.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// The lists that tell the MCP client which documents it can read are made from these constants, so adding a document here is all it takes to announce it.
public enum DocumentType {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys are defined in lowercase using @JsonProperty, as DiagramType does.
    @JsonProperty("astah_pro_user_guide")
    ASTAH_PRO_USER_GUIDE(
            "Astah Pro user guide",
            "Read it to learn how to use Astah.",
            new DocumentSource.WebPages(
                "astah-pro-and-uml-user-guide-url.txt",
                "astah_pro_and_uml_user_guide.md")),
    
    @JsonProperty("puml_guide")
    PUML_GUIDE(
            "PlantUML guide",
            "Read it to learn how to write PlantUML code.",
            new DocumentSource.WebPages(
                "plantuml-guide-url.txt",
                "plantuml_guide.md")),
    
    @JsonProperty("color_palette_guide")
    COLOR_PALETTE_GUIDE(
            "Color Palette guide",
            "Read it to learn the rules for refining the color scheme of diagrams.",
            new DocumentSource.WebPages(
                "color-palette-guide-url.txt",
                "color_palette_guide.md")),
    
    @JsonProperty("conceptual_model_conventions")
    CONCEPTUAL_MODEL_CONVENTIONS(
            "Conceptual Model Conventions",
            "Consult it when creating or editing a conceptual model.",
            new DocumentSource.WebPages(
                "conceptual-model-conventions-url.txt",
                "conceptual_model_conventions.md")),
    
    @JsonProperty("object_relational_impedance_mismatch")
    OBJECT_RELATIONAL_IMPEDANCE_MISMATCH(
            "Object-Relational Impedance Mismatch knowledge",
            "Consult it before you refer to or edit an ER diagram, when mapping a domain model to a relational (ER) model, or when designing object-to-RDB persistence.",
            new DocumentSource.WebPages(
                "or-impedance-mismatch-knowledge-url.txt",
                "or_impedance_mismatch_knowledge.md")),
    
    @JsonProperty("ddd_reference")
    DDD_REFERENCE(
            "Domain-Driven Design Reference",
            "Read it to learn the theory of Domain-Driven Design (DDD).",
            new DocumentSource.Pdf(
                "https://www.domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf",
                "ddd_reference_2015_03")),
    
    @JsonProperty("ocl_spec")
    OCL_SPEC(
            "OCL (Object Constraint Language) Specification",
            "Read it to learn the theory of OCL.",
            new DocumentSource.Pdf(
                "https://www.omg.org/spec/OCL/2.4/PDF",
                "ocl_specification_2_4")),
    
    @JsonProperty("marte_spec")
    MARTE_SPEC(
            "MARTE (UML Profile for Modeling and Analysis of Real-Time Embedded Systems) Specification",
            "Read it to learn the theory of MARTE.",
            new DocumentSource.Pdf(
                "https://www.omg.org/spec/MARTE/1.3/PDF",
                "marte_specification_1_3")),
    
    @JsonProperty("uafml_spec")
    UAFML_SPEC(
            "UAFML (Unified Architecture Framework Modeling Language) Specification",
            "Consult it when creating or editing a UAF model or diagram, to learn which UAF stereotype to apply to which UML element and which elements each UAF view shows.",
            new DocumentSource.Pdf(
                "https://www.omg.org/spec/UAF/1.3/UAFML/PDF",
                "uafml_specification_1_3")),
    
    @JsonProperty("bpmn_profile_spec")
    BPMN_PROFILE_SPEC(
            "UML Profile for BPMN Processes Specification",
            "Consult it when drawing a BPMN process or collaboration, to learn how to express it as a UML activity diagram: which UML element to use for each BPMN element and which stereotype to apply to it.",
            new DocumentSource.Pdf(
                "https://www.omg.org/spec/BPMNProfile/1.0/PDF",
                "bpmn_profile_specification_1_0")),
    
    @JsonProperty("odm_uml_owl")
    ODM_UML_OWL(
            "ODM (Ontology Definition Metamodel) Specification, excerpt: Clause 14 UML Profile for RDF and OWL, and Annex D Mapping UML to OWL (informative)",
            "Consult it when expressing an RDF or OWL ontology in a UML model, to learn which stereotype to apply to which UML element, or when mapping a UML model to OWL.",
            new DocumentSource.Pdf(
                "https://www.omg.org/spec/ODM/1.1/PDF",
                "odm_specification_1_1_uml_owl",
                // Physical pages: printed pp.131-212 (Clause 14) and pp.293-340 (Annex D), each printed number plus 14
                List.of(new DocumentSource.PageRange(145, 226), new DocumentSource.PageRange(307, 354)))),
    
    @JsonProperty("systems_engineering")
    SYSTEMS_ENGINEERING(
            "NASA Systems Engineering Handbook",
            "Read it to learn the theory of Systems Engineering.",
            new DocumentSource.Pdf(
                "https://www.nasa.gov/wp-content/uploads/2018/09/nasa_systems_engineering_handbook_0.pdf",
                "nasa_systems_engineering_handbook"));
    
    // The name of the document, as the texts that announce it write it
    private final String title;
    
    // When to read the document, as one sentence
    private final String usage;
    
    private final DocumentSource source;
    
    DocumentType(String title, String usage, DocumentSource source) {
        this.title = title;
        this.usage = usage;
        this.source = source;
    }
    
    public String title() {
        return title;
    }
    
    public String usage() {
        return usage;
    }
    
    public DocumentSource source() {
        return source;
    }
    
    // The value the MCP client names in 'documentType', read from @JsonProperty so that it is written in one place only
    public String value() {
        try {
            return DocumentType.class.getField(name()).getAnnotation(JsonProperty.class).value();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("No constant named " + name(), e);
        }
    }
}
