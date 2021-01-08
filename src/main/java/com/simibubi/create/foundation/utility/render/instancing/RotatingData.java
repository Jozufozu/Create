package com.simibubi.create.foundation.utility.render.instancing;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.utility.render.instancing.VertexAttribute.*;

public class RotatingData extends BasicData<RotatingData> {
    public static VertexFormat FORMAT = new VertexFormat(VEC3, VEC2, FLOAT, FLOAT, VEC3);

    private float rotationalSpeed;
    private float rotationOffset;
    private float rotationAxisX;
    private float rotationAxisY;
    private float rotationAxisZ;

    public RotatingData setRotationalSpeed(float rotationalSpeed) {
        this.rotationalSpeed = rotationalSpeed;
        return this;
    }

    public RotatingData setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        return this;
    }

    public RotatingData setRotationAxis(Vector3f axis) {
        this.rotationAxisX = axis.getX();
        this.rotationAxisY = axis.getY();
        this.rotationAxisZ = axis.getZ();
        return this;
    }

    public RotatingData setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
        this.rotationAxisX = rotationAxisX;
        this.rotationAxisY = rotationAxisY;
        this.rotationAxisZ = rotationAxisZ;
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        super.write(buf);
        putFloat(buf, rotationalSpeed);
        putFloat(buf, rotationOffset);

        putVec3(buf, rotationAxisX, rotationAxisY, rotationAxisZ);
    }
}