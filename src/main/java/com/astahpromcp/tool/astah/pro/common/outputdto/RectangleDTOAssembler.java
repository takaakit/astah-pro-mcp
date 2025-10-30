package com.astahpromcp.tool.astah.pro.common.outputdto;

import lombok.NonNull;
import java.awt.geom.Rectangle2D;

public class RectangleDTOAssembler {
    public static RectangleDTO toDTO(@NonNull Rectangle2D astahRectangle) throws Exception {

        return new RectangleDTO(
            astahRectangle.getX(),
            astahRectangle.getY(),
            astahRectangle.getWidth(),
            astahRectangle.getHeight());
    }
}
