package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.FilePathHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementHyperlinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.UrlHyperlinkDTO;
import com.change_vision.jude.api.inf.model.IHyperlink;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NodePresentationDTOAssembler {
    public static NodePresentationDTO toDTO(@NonNull INodePresentation astahNodePresentation) throws Exception {

        List<String> linkIds = new ArrayList<>();
        for (ILinkPresentation linkPresentation : astahNodePresentation.getLinks()) {
            linkIds.add(linkPresentation.getID());
        }

        List<UrlHyperlinkDTO> urlHyperlinks = new ArrayList<>();
        List<FilePathHyperlinkDTO> filePathHyperlinks = new ArrayList<>();
        List<NamedElementHyperlinkDTO> namedElementHyperlinks = new ArrayList<>();
        for (IHyperlink hyperlink : astahNodePresentation.getHyperlinks()) {
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

        return new NodePresentationDTO(
            PresentationDTOAssembler.toDTO(astahNodePresentation),
            linkIds,
            RectangleDTOAssembler.toDTO(astahNodePresentation.getRectangle()),
            urlHyperlinks,
            filePathHyperlinks,
            namedElementHyperlinks);
    }
}
