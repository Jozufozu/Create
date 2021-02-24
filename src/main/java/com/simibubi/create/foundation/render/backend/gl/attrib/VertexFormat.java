package com.simibubi.create.foundation.render.backend.gl.attrib;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class VertexFormat {

    protected final ArrayList<BufferFormat> buffers;

    protected final int shaderAttributeCount;

    public VertexFormat() {
        FormatBuilder formatBuilder = new FormatBuilder(bufferCount());
        setup(formatBuilder);

        buffers = new ArrayList<>(formatBuilder.buffers.size());
        for (ArrayList<IVertexAttrib> buffer : formatBuilder.buffers) {
            buffers.add(new BufferFormat(buffer));
        }

        shaderAttributeCount = buffers.stream().reduce(0, (integer, bufferFormat) -> bufferFormat.shaderAttributeCount, Integer::sum);
    }

    public abstract int bufferCount();

    protected abstract void setup(FormatBuilder formatBuilder);

    public int getShaderAttributeCount() {
        return shaderAttributeCount;
    }

    public static class FormatBuilder {
        protected final ArrayList<ArrayList<IVertexAttrib>> buffers = new ArrayList<>();

        public FormatBuilder(int numBuffers) {
            for (int i = 0; i < numBuffers; i++) {
                buffers.add(new ArrayList<>());
            }
        }

        public <A extends Enum<A> & IVertexAttrib> void addAttributes(Class<A> attribEnum) {
            addAttributes(attribEnum, 0);
        }

        public <A extends Enum<A> & IVertexAttrib> void addAttributes(Class<A> attribEnum, int bufferIndex) {
            buffers.get(bufferIndex).addAll(Arrays.asList(attribEnum.getEnumConstants()));
        }
    }

}
