package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.guide.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ERDiagramGuideTool implements ToolProvider {
    
    public ERDiagramGuideTool() {
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.definition(
                    "er_dgm_guide",
                    "MCP client (you) MUST call this tool function before referencing or editing a ER diagram to understand its usage and terminology definitions.",
                    this::getGuide,
                    NoInputDTO.class,
                    GuideDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create ER diagram guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get ER diagram guide: {}", param);

        String content = """
IMPORTANT POINTS to Keep in Mind:
* After editing a diagram, retrieve its drawing content and verify that the edits and layout are as intended.


Terminology Definitions (quoted from FIPS PUB 184, Integration Definition for Information Modeling (IDEF1X)):
* Attribute: A property or characteristic that is common to some or all of the instances of an entity. An attribute represents the use of a domain in the context of an entity.
* Constraint: A rule that specifies a valid condition of data.
* Constraint, Cardinality: A limit on the number of entity instances that can be associated with each other in a relationship.
* Data Type: A categorization of an abstract set of possible values, characteristics, and set of operations for an attribute. Integers, real numbers, character strings, and enumerations are examples of data types.
* Domain: A named set of data values (fixed, or possibly infinite in number) all of the same data type, upon which the actual value for an attribute instance is drawn. Every attribute must be defined on exactly one underlying domain. Multiple attributes may be based on the same underlying domain.
* Entity: The representation of a set of real or abstract things (people, objects, places, events, ideas, combination of things, etc.) that are recognized as the same type because they share the same characteristics and can participate in the same relationships.
* Entity Instance: One of a set of real or abstract things represented by an entity. The instance of an entity can be specifically identified by the value of the attribute(s) participating in its primary key.
* Key, Primary: The candidate key selected as the unique identifier of an entity.
* Key, Foreign: An attribute, or combination of attributes of a child or category entity instance whose values match those in the primary key of a related parent or generic entity instance. A foreign key results from the migration of the parent or generic entities primary key through a specific connection or categorization relationship.
* Relationship: An association between two entities or between instances of the same entity.
* Relationship Cardinality: The number of entity instances that can be associated with each other in a relationship.
* Conceptual Schema: A schema of the ANSI/SPARC Three Schema Architecture, in which the structure of data is represented in a form independent of any physical storage or external presentation format.
* External Schema: A schema of the ANSI/SPARC Three Schema Architecture, in which views of information are represented in a form convenient for the users of information; a description of the structure of data as seen by the user of a system.
* Internal Schema: A schema of the ANSI/SPARC Three Schema Architecture, in which views of information are represented in a form specific to the data base management system used to store the information: a description of the physical structure of data.
* An entity represents a set of real or abstract things (people, objects, places, events, ideas, combinations of things, etc.) which have common attributes or characteristics.
* A "Domain" represents a named and defined set of values that one or more attributes draw their values from. In IDEF1X domains are defined separately from entities and views in order to permit their reuse and standardization throughout the enterprise.
* An IDEF1X "view" is a collection of entities and assigned domains (attributes) assembled for some purpose. A view may cover the entire area being modeled, or a part of that area.
* In an IDEF1X view, an "attribute" represents a type of characteristic or property associated with a set of real or abstract things (people, objects, places, events, ideas, combinations of things, etc.).
* An entity must have an attribute or combination of attributes whose values uniquely identify every instance of the entity. These attributes form the "primary-key" of the entity.
* Primary and alternate keys represent uniqueness constraints over the values of entity attributes.
* If more than one candidate key exists, then one candidate key is designated as the "primary key" and the other candidate keys are designated as "alternate keys." If only one candidate key exists, then it is, of course, the primary key.
* Attributes which define the primary key are placed at the top of the attribute list within an entity box and separated from the other attributes by a horizontal line.
* Foreign keys are attributes in entities which designate instances of related entities.


Inheritance Structure of ER Element Types:
The following PlantUML code illustrates the inheritance structure of ER element types in Astah. Child types inherit all the characteristics of their parent types. In Astah, the definitions of ER elements are established as extensions of the UML element definitions, and this is an Astah-specific extension.

```plantuml
@startuml
ERAttribute --|> NamedElement
ERDatatype --|> NamedElement
ERDiagram --|> Diagram
ERDomain --|> NamedElement
EREntity --|> NamedElement
ERIndex --|> NamedElement
ERModel --|> Model
ERPackage --|> Package
ERSchema --|> ERPackage
ERRelationship --|> NamedElement
ERSubtypeRelationship --|> NamedElement
@enduml
```


Relationships between ER Element Types:
The following PlantUML code illustrates the relationships between the ER element types in Astah.

```plantuml
@startuml
ERAttribute ---> "datatype" ERDatatype
ERAttribute ---> "domain" ERDomain
ERAttribute ---> "indices" ERIndex
ERAttribute ---> "referenced primary key" ERAttribute
ERAttribute ---> "referenced foreign keys" ERAttribute
ERAttribute ---> "referenced relationship" ERRelationship
ERAttribute ---> "referenced subtype relationships" ERSubtypeRelationship
ERAttribute ---> "associated subtype relationship as discriminator attribute" ERSubtypeRelationship
ERDomain ---> "children" ERDomain
EREntity ---> "children relationships" ERRelationship
EREntity ---> "children subtype relationships" ERSubtypeRelationship
EREntity ---> "indices" ERIndex
EREntity ---> "primary keys" ERAttribute
EREntity ---> "foreign keys" ERAttribute
EREntity ---> "non primary keys" ERAttribute
EREntity ---> "parent relationships" ERRelationship
EREntity ---> "parent subtype relationships" ERSubtypeRelationship
ERIndex ---> "attributes" ERAttribute
ERIndex ---> "relationships" ERRelationship
ERIndex ---> "parent entity" EREntity
ERModel ---> "schemata" ERSchema
ERPackage ---> "entities" EREntity
ERSchema ---> "datatypes" ERDatatype
ERSchema ---> "domains" ERDomain
ERRelationship ---> "parent entity" EREntity
ERRelationship ---> "child entity" EREntity
ERRelationship ---> "index" ERIndex
ERRelationship ---> "foreign keys" ERAttribute
ERSubtypeRelationship ---> "parent entity" EREntity
ERSubtypeRelationship ---> "child entity" EREntity
ERSubtypeRelationship ---> "discriminator attribute" ERAttribute
ERSubtypeRelationship ---> "foreign keys" ERAttribute
@enduml
```
        """;

        return new GuideDTO(content);
    }
}
