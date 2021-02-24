package com.simibubi.create.foundation.render.backend.gl.attrib;

import java.util.ArrayList;
import java.util.Arrays;

public class BufferFormat {
    protected ArrayList<IVertexAttrib> attributes;

    protected final int shaderAttributeCount;
    protected final int stride;

    public BufferFormat(ArrayList<IVertexAttrib> attributes) {
        this.attributes = attributes;

        int numAttributes = 0, stride = 0;
        for (IVertexAttrib attrib : attributes) {
            VertexAttribSpec spec = attrib.attribSpec();
            numAttributes += spec.getShaderAttributeCount();
            stride += spec.getSize();
        }
        this.shaderAttributeCount = numAttributes;
        this.stride = stride;
    }

    public int getShaderAttributeCount() {
        return shaderAttributeCount;
    }

    public int getStride() {
        return stride;
    }

    public void vertexAttribPointers(int index) {
        int offset = 0;
        for (IVertexAttrib attrib : this.attributes) {
            VertexAttribSpec spec = attrib.attribSpec();
            spec.vertexAttribPointer(stride, index, offset);
            index += spec.getShaderAttributeCount();
            offset += spec.getSize();
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private final ArrayList<IVertexAttrib> allAttributes;

        public Builder() {
            allAttributes = new ArrayList<>();
        }

        public <A extends Enum<A> & IVertexAttrib> Builder addAttributes(Class<A> attribEnum) {
            allAttributes.addAll(Arrays.asList(attribEnum.getEnumConstants()));
            return this;
        }

        public BufferFormat build() {
            return new BufferFormat(allAttributes);
        }
    }
}
