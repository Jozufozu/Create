package com.simibubi.create.foundation.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.antlr.v4.runtime.misc.IntegerList;

public class PonderProgressBar extends AbstractSimiWidget {

	LerpedFloat progress;
	LerpedFloat flash;

	PonderUI ponder;

	public PonderProgressBar(PonderUI ponder, int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);

		this.ponder = ponder;
		progress = LerpedFloat.linear()
			.startWithValue(0);
		flash = LerpedFloat.linear()
			.startWithValue(0);
	}

	public void tick() {
		progress.chase(ponder.getActiveScene()
			.getSceneProgress(), .5f, LerpedFloat.Chaser.EXP);
		progress.tickChaser();

		if (isHovered)
			flash();
	}

	public void flash() {
		float value = flash.getValue();
		flash.setValue(value + (1 - value) * .2f);
	}

	public void dim() {
		float value = flash.getValue();
		flash.setValue(value * .5f);
	}

	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return this.active && this.visible && !ponder.getActiveScene().keyframeTimes.isEmpty()
			&& mouseX >= (double) this.x && mouseX < (double) (this.x + this.width + 4) && mouseY >= (double) this.y - 3
			&& mouseY < (double) (this.y + this.height + 20);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
        PonderScene activeScene = ponder.getActiveScene();
        IntegerList keyframeTimes = activeScene.keyframeTimes;

		int keyframeIndex = getHoveredKeyframeIndex(activeScene, mouseX);

		if (keyframeIndex == -1)
			ponder.seekToTime(0);
		else if (keyframeIndex == keyframeTimes.size())
			ponder.seekToTime(activeScene.totalTime);
		else
			ponder.seekToTime(keyframeTimes.get(keyframeIndex));
	}

	public int getHoveredKeyframeIndex(PonderScene activeScene, double mouseX) {
		IntegerList keyframeTimes = activeScene.keyframeTimes;

		int totalTime = activeScene.totalTime;
		int clickedAtTime = (int) ((mouseX - x) / ((double) width + 4) * totalTime);

		{
			int lastKeyframeTime = keyframeTimes.get(keyframeTimes.size() - 1);

			int diffToEnd = totalTime - clickedAtTime;
			int diffToLast = clickedAtTime - lastKeyframeTime;

			if (diffToEnd > 0 && diffToEnd < diffToLast / 2) {
				return keyframeTimes.size();
			}
		}

		int index = -1;

		for (int i = 0; i < keyframeTimes.size(); i++) {
			int keyframeTime = keyframeTimes.get(i);

			if (keyframeTime > clickedAtTime)
				break;

			index = i;
		}

		return index;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {

		isHovered = clicked(mouseX, mouseY);

		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 400);
		PonderUI.renderBox(x, y, width, height, false);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		RenderSystem.translated(x - 2, y - 2, 0);

		RenderSystem.pushMatrix();
		RenderSystem.scaled((width + 4) * progress.getValue(partialTicks), 1, 1);
		GuiUtils.drawGradientRect(500, 0, 3, 1, 4, 0x80ffeedd, 0x80ffeedd);
		GuiUtils.drawGradientRect(500, 0, 4, 1, 5, 0x50ffeedd, 0x50ffeedd);
		RenderSystem.popMatrix();

		renderKeyframes(mouseX, partialTicks);

		RenderSystem.popMatrix();
	}

	private void renderKeyframes(int mouseX, float partialTicks) {
		PonderScene activeScene = ponder.getActiveScene();

		int hoverStartColor;
		int hoverEndColor;
		int hoverIndex;

		if (isHovered) {
			hoverIndex = getHoveredKeyframeIndex(activeScene, mouseX);
			float flashValue = flash.getValue(partialTicks) * 3
				+ (float) Math.sin((AnimationTickHolder.getTicks() + partialTicks) / 6);

			hoverEndColor = ColorHelper.applyAlpha(0x70ffffff, flashValue);
			hoverStartColor = ColorHelper.applyAlpha(0x30ffffff, flashValue);
		} else {
			hoverIndex = -2;
			hoverEndColor = 0;
			hoverStartColor = 0;
		}
		IntegerList keyframeTimes = activeScene.keyframeTimes;

		if (hoverIndex == -1)
			drawKeyframe(activeScene, true, 0, 0, hoverStartColor, hoverEndColor, 8);
		else if (hoverIndex == keyframeTimes.size())
			drawKeyframe(activeScene, true, activeScene.totalTime, width + 4, hoverStartColor, hoverEndColor, 8);

		for (int i = 0; i < keyframeTimes.size(); i++) {
			int keyframeTime = keyframeTimes.get(i);
			int keyframePos = (int) (((float) keyframeTime) / ((float) activeScene.totalTime) * (width + 4));

			boolean selected = i == hoverIndex;
			int startColor = selected ? hoverStartColor : 0x30ffeedd;
			int endColor = selected ? hoverEndColor : 0x60ffeedd;
			int height = selected ? 8 : 4;

			drawKeyframe(activeScene, selected, keyframeTime, keyframePos, startColor, endColor, height);

		}
	}

	private void drawKeyframe(PonderScene activeScene, boolean selected, int keyframeTime, int keyframePos, int startColor, int endColor, int height) {
		if (selected) {
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			GuiUtils.drawGradientRect(500, keyframePos, 10, keyframePos + 1, 10 + height, endColor, startColor);
			RenderSystem.pushMatrix();
			RenderSystem.translated(0, 0, 400);
			String text;
			int offset;
			if (activeScene.currentTime < keyframeTime) {
				text = ">";
				offset = -1 - font.getStringWidth(text);
			}
			else {
				text = "<";
				offset = 3;
			}
			font.drawString(text, keyframePos + offset, 10, endColor);
			RenderSystem.popMatrix();
		}

		GuiUtils.drawGradientRect(500, keyframePos, -1, keyframePos + 1, 2 + height, startColor, endColor);
	}

	@Override
	public void playDownSound(SoundHandler handler) {

	}
}
