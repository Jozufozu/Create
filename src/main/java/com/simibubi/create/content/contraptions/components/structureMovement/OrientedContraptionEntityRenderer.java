package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class OrientedContraptionEntityRenderer extends ContraptionEntityRenderer<OrientedContraptionEntity> {

	public OrientedContraptionEntityRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public boolean shouldRender(OrientedContraptionEntity entity, ClippingHelper p_225626_2_, double p_225626_3_,
		double p_225626_5_, double p_225626_7_) {
		if (!super.shouldRender(entity, p_225626_2_, p_225626_3_, p_225626_5_, p_225626_7_))
			return false;

		return entity.getContraption().getType() != AllContraptionTypes.MOUNTED || entity.getRidingEntity() != null;
	}
}
