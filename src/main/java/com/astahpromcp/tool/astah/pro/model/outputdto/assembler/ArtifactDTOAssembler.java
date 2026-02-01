package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.model.outputdto.ArtifactDTO;
import com.change_vision.jude.api.inf.model.IArtifact;
import lombok.NonNull;

public class ArtifactDTOAssembler {
    public static ArtifactDTO toDTO(@NonNull IArtifact astahArtifact) throws Exception {
        return new ArtifactDTO(ClassDTOAssembler.toDTO(astahArtifact));
    }
}
