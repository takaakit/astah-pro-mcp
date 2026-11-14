package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.VisibilityKind;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NamedElementDTO(
    @JsonPropertyDescription("Element info")
    ElementDTO element,
    
    @JsonPropertyDescription("Element type")
    String type,
    
    @JsonPropertyDescription("Element name")
    String name,
    
    @JsonPropertyDescription("Full namespace")
    String nameSpace,
    
    @JsonPropertyDescription("Visibility")
    VisibilityKind visibility,
    
    @JsonPropertyDescription("Alias 1")
    String alias1,
    
    @JsonPropertyDescription("Alias 2")
    String alias2,
    
    @JsonPropertyDescription("Definition")
    String definition,
    
    @JsonPropertyDescription("Client dependencies: Dependencies in which this element depends on the supplier. Usages and realizations are not included.")
    List<DependencyDTO> clientDependencies,
    
    @JsonPropertyDescription("Supplier dependencies: Dependencies in which the client depends on this element. Usages and realizations are not included.")
    List<DependencyDTO> supplierDependencies,
    
    @JsonPropertyDescription("Client realizations: Realizations in which this element realizes the supplier.")
    List<RealizationDTO> clientRealizations,
    
    @JsonPropertyDescription("Supplier realizations: Realizations in which the client realizes this element.")
    List<RealizationDTO> supplierRealizations,
    
    @JsonPropertyDescription("Client usages: Usages in which this element uses the supplier.")
    List<UsageDTO> clientUsages,
    
    @JsonPropertyDescription("Supplier usages: Usages in which the client uses this element.")
    List<UsageDTO> supplierUsages,
    
    @JsonPropertyDescription("Owned diagrams: Diagrams that this element owns directly. Diagrams owned by the elements nested in it are not included.")
    List<NameIdTypeDTO> ownedDiagrams,
    
    @JsonPropertyDescription("Rendered-in diagrams: Diagrams on which this element itself is drawn, each listed once. Diagrams on which it appears only as the base of a lifeline, an instance specification or an object node are not included; get_dgms_of_element returns those too.")
    List<NameIdTypeDTO> renderedInDiagrams,
    
    @JsonPropertyDescription("Constraints")
    List<NameIdTypeDTO> constraints,
    
    @JsonPropertyDescription("Hyperlinks to URL")
    List<UrlHyperlinkDTO> urlHyperlinks,
    
    @JsonPropertyDescription("Hyperlinks to file path")
    List<FilePathHyperlinkDTO> filePathHyperlinks,
    
    @JsonPropertyDescription("Hyperlinks to named element")
    List<NamedElementHyperlinkDTO> namedElementHyperlinks
) {
}
