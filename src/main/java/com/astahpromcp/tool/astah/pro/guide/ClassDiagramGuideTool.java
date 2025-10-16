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
public class ClassDiagramGuideTool implements ToolProvider {

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "cls_dgm_guide",
                            "MCP client (you) MUST call this tool function before referencing or editing a class diagram (also serving as an object diagram and package diagram) to understand its usage and terminology definitions.",
                            this::getGuide,
                            NoInputDTO.class,
                            GuideDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get class diagram guide: {}", param);
        
        String content = """
IMPORTANT POINTS to Keep in Mind:
* After editing a diagram, retrieve its drawing content and verify that the edits and layout are as intended.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* An Element is a constituent of a model. Descendants of Element provide semantics appropriate to the concept they represent.
* Every kind of Element may own Comments. The ownedComments for an Element add no semantics but may represent information useful to the reader of the model.
* A Relationship is an Element that specifies some kind of relationship between other Elements. Descendants of Relationship provide semantics appropriate to the concept they represent. A DirectedRelationship represents a Relationship between a collection of source model elements and a collection of target model elements. A DirectedRelationship is said to be directed from the source elements to the target elements.
* Templates are model Elements that are parameterized by other model Elements.
* A Namespace is an Element in a model that contains a set of NamedElements that can be identified by name. Packages are Namespaces whose specific purpose is to contain other NamedElements in order to organize a model, but many other kinds of model Elements are also Namespaces, including Classifiers, which contain named Features and nested Classifiers, and BehavioralFeatures, which contain named Parameters.
* A NamedElement is an Element in a model that may have a name. The name may be used for identification of the NamedElement within Namespaces where its name is accessible.
* Types and multiplicity are used in the declaration of Elements that contain values, in order to constrain the kind and number of values that may be contained.
* The multiplicity bounds may be shown in the format: <lower-bound> .. <upper-bound>
* A Dependency implies that the semantics of the clients are not complete without the suppliers. The presence of Dependency relationships in a model does not have any runtime semantic implications. The semantics are all given in terms of the NamedElements that participate in the relationship, not in terms of their instances.
* A Usage is a Dependency in which one NamedElement requires another NamedElement (or set of NamedElements) for its full implementation or operation. The Usage does not specify how the client uses the supplier other than the fact that the supplier is used by the definition or implementation of the client.
* Realization is a specialized Abstraction dependency between two sets of NamedElements, one representing a specification (the supplier) and the other representing an implementation of that specification (the client).
* A Comment is a textual annotation that can be attached to a set of Elements.
* A Dependency is a Relationship that signifies that a single model Element or a set of model Elements requires other model Elements for their specification or implementation. This means that the complete semantics of the client Element(s) are either semantically or structurally dependent on the definition of the supplier Element(s).
* A DirectedRelationship represents a relationship between a collection of source model Elements and a collection of target model Elements.
* An Element is a constituent of a model. As such, it has the capability of owning other Elements.
* A multiplicity is a definition of an inclusive interval of non-negative integers beginning with a lower bound and ending with a (possibly infinite) upper bound. A MultiplicityElement embeds this information to specify the allowable cardinalities for an instantiation of the Element.
* A NamedElement is an Element in a model that may have a name. The name may be given directly and/or via the use of a StringExpression.
* A Namespace is an Element in a model that owns and/or imports a set of NamedElements that can be identified by name.
* Realization is a specialized Abstraction relationship between two sets of model Elements, one representing a specification (the supplier) and the other represents an implementation of the latter (the client).
* A TemplateParameter exposes a ParameterableElement as a formal parameter of a template.
* A Usage is a Dependency in which the client Element requires the supplier Element (or set of Elements) for its full implementation or operation.
* A Classifier represents a classification of instances according to their Features. Classifiers are organized in hierarchies by Generalizations. RedefinableElements may be redefined in the context of Generalization hierarchies.
* A Classifier has a set of Features, some of which are Properties called the attributes of the Classifier. Each of the Features is a member of the Classifier.
* Generalizations define generalization/specialization relationships between Classifiers. Each Generalization relates a specific Classifier to a more general Classifier. Given a Classifier, the transitive closure of its general Classifiers is often called its generalizations, and the transitive closure of its specific Classifiers is called its specializations.
* Features represent structural and behavioral characteristics of Classifiers.
* A StructuralFeature is a typed Feature of a Classifier that specifies the structure of instances of the Classifier.
* A non-static BehavioralFeature specifies that an instance of its featuringClassifier will react to an invocation of the BehavioralFeature by carrying out a specific behavioral response. Subclasses of BehavioralFeature model different behavioral aspects of a Classifier.
* InstanceSpecifications represent instances of Classifiers in a modeled system. They are often used to model example configurations of instances. They may be partial or complete representations of the instances that they correspond to.
* AggregationKind is an Enumeration for specifying the kind of aggregation of a Property.
* A BehavioralFeature is a feature of a Classifier that specifies an aspect of the behavior of its instances. A BehavioralFeature is implemented (realized) by a Behavior. A BehavioralFeature specifies that a Classifier will respond to a designated request by invoking its implementing method.
* A Feature declares a behavioral or structural characteristic of Classifiers.
* A Generalization is a taxonomic relationship between a more general Classifier and a more specific Classifier. Each instance of the specific Classifier is also an instance of the general Classifier. The specific Classifier inherits the features of the more general Classifier. A Generalization is owned by the specific Classifier.
* An InstanceSpecification is a model element that represents an instance in a modeled system. An InstanceSpecification can act as a DeploymentTarget in a Deployment relationship, in the case that it represents an instance of a Node. It can also act as a DeployedArtifact, if it represents an instance of an Artifact.
* An Operation is a BehavioralFeature of a Classifier that specifies the name, type, parameters, and constraints for invoking an associated Behavior. An Operation may invoke both the execution of method behaviors as well as other behavioral responses.
* A Parameter is a specification of an argument used to pass information into or out of an invocation of a BehavioralFeature. Parameters can be treated as ConnectableElements within Collaborations.
* A Slot designates that an entity modeled by an InstanceSpecification has a value or values for a specific StructuralFeature.
* Interfaces declare coherent services that are implemented by BehavioredClassifiers that implement the Interfaces via InterfaceRealizations.
* An Enumeration is a DataType whose values are enumerated in the model as EnumerationLiterals.
* An EnumerationLiteral is a user-defined data value for an Enumeration.
* A PrimitiveType defines a predefined DataType, without any substructure. A PrimitiveType may have an algebra and operations defined outside of UML, for example, mathematically.
* Class is the concrete realization of EncapsulatedClassifier and BehavioredClassifier. The purpose of a Class is to specify a classification of objects and to specify the Features that characterize the structure and behavior of those objects.
* Class is a kind of EncapsulatedClassifier whose Features are Properties, Operations, Receptions, Ports and Connectors. Attributes of a Class are Properties that are owned by the Class. Some of these attributes may represent the ends of binary Associations.
* An Association classifies a set of tuples representing links between typed instances. An AssociationClass is both an Association and a Class.
* An AssociationClass is a declaration of an Association that has a set of Features of its own. An AssociationClass is both an Association and a Class, and preserves the static and dynamic semantics of both.
* A Class classifies a set of objects and specifies the features that characterize the structure and behavior of those objects. A Class may have an internal structure and Ports.
* A Package is a namespace for its members, which comprise those elements associated via packagedElement (which are said to be owned or contained), and those imported.
        """;

        return new GuideDTO(content);
    }
}
