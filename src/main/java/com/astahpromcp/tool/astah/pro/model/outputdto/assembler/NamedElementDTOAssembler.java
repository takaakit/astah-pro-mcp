package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.VisibilityKind;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import com.astahpromcp.tool.astah.pro.model.outputdto.DependencyDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.FilePathHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.UrlHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.RealizationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.UsageDTO;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NamedElementDTOAssembler {
    public static NamedElementDTO toDTO(@NonNull INamedElement astahNamedElement) throws Exception {
        
        List<DependencyDTO> clientDependencies = new ArrayList<>();
        for (IDependency dependency : astahNamedElement.getClientDependencies()) {
            clientDependencies.add(DependencyDTOAssembler.toDTO(dependency));
        }

        List<DependencyDTO> supplierDependencies = new ArrayList<>();
        for (IDependency dependency : astahNamedElement.getSupplierDependencies()) {
            supplierDependencies.add(DependencyDTOAssembler.toDTO(dependency));
        }

        List<RealizationDTO> clientRealizations = new ArrayList<>();
        for (IRealization realization : astahNamedElement.getClientRealizations()) {
            clientRealizations.add(RealizationDTOAssembler.toDTO(realization));
        }

        List<RealizationDTO> supplierRealizations = new ArrayList<>();
        for (IRealization realization : astahNamedElement.getSupplierRealizations()) {
            supplierRealizations.add(RealizationDTOAssembler.toDTO(realization));
        }

        List<UsageDTO> clientUsages = new ArrayList<>();
        for (IUsage usage : astahNamedElement.getClientUsages()) {
            clientUsages.add(UsageDTOAssembler.toDTO(usage));
        }

        List<UsageDTO> supplierUsages = new ArrayList<>();
        for (IUsage usage : astahNamedElement.getSupplierUsages()) {
            supplierUsages.add(UsageDTOAssembler.toDTO(usage));
        }

        List<NameIdTypeDTO> renderedInDiagrams = new ArrayList<>();
        // Workaround: Some diagrams caused an exception when trying to cast them.
        try {
            for (IDiagram diagram : astahNamedElement.getDiagrams()) {
                renderedInDiagrams.add(NameIdTypeDTOAssembler.toDTO(diagram));
            }
        } catch (Exception e) {
            log.warn("Failed to get diagrams: {}", e.getMessage());
        }

        List<NameIdTypeDTO> constraints = new ArrayList<>();
        for (IConstraint constraint : astahNamedElement.getConstraints()) {
            constraints.add(NameIdTypeDTOAssembler.toDTO(constraint));
        }

        VisibilityKind visibility;
        if (astahNamedElement.isPublicVisibility()) {
            visibility = VisibilityKind.PUBLIC;
        } else if (astahNamedElement.isProtectedVisibility()) {
            visibility = VisibilityKind.PROTECTED;
        } else if (astahNamedElement.isPrivateVisibility()) {
            visibility = VisibilityKind.PRIVATE;
        } else if (astahNamedElement.isPackageVisibility()) {
            visibility = VisibilityKind.PACKAGE;
        } else {
            visibility = VisibilityKind.PUBLIC;
        }

        List<UrlHyperlinkDTO> urlHyperlinks = new ArrayList<>();
        List<FilePathHyperlinkDTO> filePathHyperlinks = new ArrayList<>();
        List<NamedElementHyperlinkDTO> namedElementHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : astahNamedElement.getHyperlinks()) {
            if (hyperlink.isURL()) {
                urlHyperlinks.add(
                    new UrlHyperlinkDTO(
                        hyperlink.getName(),
                        hyperlink.getComment()));
            
            } else if (hyperlink.isFile()) {
                filePathHyperlinks.add(
                    new FilePathHyperlinkDTO(
                        Path.of(hyperlink.getPath(), hyperlink.getName()).toString(),
                        hyperlink.getComment()));
            
            } else if (hyperlink.isModel()) {
                namedElementHyperlinks.add(
                    new NamedElementHyperlinkDTO(
                        hyperlink.getName(),
                        hyperlink.getComment()));
            }
        }   

        return new NamedElementDTO(
            ElementDTOAssembler.toDTO(astahNamedElement),
            NamedElementDTO.Type.getCorrespondingType(astahNamedElement).typeName,
            astahNamedElement.getName(),
            astahNamedElement.getFullNamespace("."),
            visibility,
            astahNamedElement.getAlias1(),
            astahNamedElement.getAlias2(),
            astahNamedElement.getDefinition(),
            clientDependencies,
            supplierDependencies,
            clientRealizations,
            supplierRealizations,
            clientUsages,
            supplierUsages,
            renderedInDiagrams,
            constraints,
            urlHyperlinks,
            filePathHyperlinks,
            namedElementHyperlinks);
    }
}
