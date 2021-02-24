package com.simibubi.create.content.contraptions.base;

public class RotatingFormat extends KineticFormat {

    @Override
    public int bufferCount() {
        return 1;
    }

    @Override
    protected void setup(FormatBuilder builder) {
        super.setup(builder);
        builder.addAttributes(RotatingVertexAttributes.class);
    }
}
