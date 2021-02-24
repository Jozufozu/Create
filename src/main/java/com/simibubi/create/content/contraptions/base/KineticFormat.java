package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;

public abstract class KineticFormat extends VertexFormat {

    @Override
    protected void setup(FormatBuilder builder) {
        builder.addAttributes(KineticVertexAttributes.class);
    }
}
