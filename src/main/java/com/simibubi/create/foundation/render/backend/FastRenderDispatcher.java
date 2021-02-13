package com.simibubi.create.foundation.render.backend;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.WorldAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class FastRenderDispatcher {

    public static WorldAttached<ConcurrentHashMap.KeySetView<TileEntity, Boolean>> queuedUpdates = new WorldAttached<>(ConcurrentHashMap::newKeySet);
    public static WorldAttached<ConcurrentHashMap<TileEntity, Integer>> addedLastTick = new WorldAttached<>(ConcurrentHashMap::new);

    private static Matrix4f projectionMatrixThisFrame = null;

    public static void endFrame() {
        projectionMatrixThisFrame = null;
    }

    public static void enqueueUpdate(TileEntity te) {
        queuedUpdates.get(te.getWorld()).add(te);
    }

    public static void tick() {
        ClientWorld world = Minecraft.getInstance().world;

        // Clean up twice a second. This doesn't have to happen every tick,
        // but this does need to be run to ensure we don't miss anything.
        int ticks = AnimationTickHolder.getTicks();

        ConcurrentHashMap<TileEntity, Integer> map = addedLastTick.get(world);
        map
                .entrySet()
                .stream()
                .filter(it -> ticks - it.getValue() > 10)
                .map(Map.Entry::getKey)
                .forEach(te -> {
                    map.remove(te);

                    CreateClient.kineticRenderer.onLightUpdate(te);
                });


        if (ticks % 10 == 0) {
            CreateClient.kineticRenderer.clean();
        }

        runQueue(queuedUpdates.get(world), CreateClient.kineticRenderer::update);
    }

    public static boolean available() {
        return Backend.canUseInstancing();
    }

    public static boolean available(World world) {
        return Backend.canUseInstancing() && !(world instanceof SchematicWorld);
    }

    public static int getDebugMode() {
        return KineticDebugger.isActive() ? 1 : 0;
    }

    public static void refresh() {
        RenderWork.enqueue(Minecraft.getInstance().worldRenderer::loadRenderers);
    }

    private static <T> void runQueue(@Nullable ConcurrentHashMap.KeySetView<T, Boolean> changed, Consumer<T> action) {
        if (changed == null) return;

        if (available()) {
            // because of potential concurrency issues, we make a copy of what's in the set at the time we get here
            ArrayList<T> tiles = new ArrayList<>(changed);

            tiles.forEach(action);
            changed.removeAll(tiles);
        } else {
            changed.clear();
        }
    }

    public static void renderLayer(RenderType layer, Matrix4f viewProjection, float cameraX, float cameraY, float cameraZ) {
        if (!Backend.canUseInstancing()) return;

        layer.startDrawing();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        GL11.glCullFace(GL11.GL_BACK);
        CreateClient.kineticRenderer.render(layer, viewProjection, cameraX, cameraY, cameraZ);
        RenderSystem.disableCull();
        //RenderSystem.disableDepthTest();

        layer.endDrawing();
    }

    // copied from GameRenderer.renderWorld
    public static Matrix4f getProjectionMatrix() {
        if (projectionMatrixThisFrame != null) return projectionMatrixThisFrame;

        float partialTicks = AnimationTickHolder.getPartialTicks();
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        ClientPlayerEntity player = mc.player;

        MatrixStack matrixstack = new MatrixStack();
        matrixstack.peek().getModel().multiply(gameRenderer.func_228382_a_(gameRenderer.getActiveRenderInfo(), partialTicks, true));
        gameRenderer.bobViewWhenHurt(matrixstack, partialTicks);
        if (mc.gameSettings.viewBobbing) {
            gameRenderer.bobView(matrixstack, partialTicks);
        }

        float portalTime = MathHelper.lerp(partialTicks, player.prevTimeInPortal, player.timeInPortal);
        if (portalTime > 0.0F) {
            int i = 20;
            if (player.isPotionActive(Effects.NAUSEA)) {
                i = 7;
            }

            float f1 = 5.0F / (portalTime * portalTime + 5.0F) - portalTime * 0.04F;
            f1 = f1 * f1;
            Vector3f vector3f = new Vector3f(0.0F, MathHelper.SQRT_2 / 2.0F, MathHelper.SQRT_2 / 2.0F);
            matrixstack.multiply(vector3f.getDegreesQuaternion(((float)gameRenderer.rendererUpdateCount + partialTicks) * (float)i));
            matrixstack.scale(1.0F / f1, 1.0F, 1.0F);
            float f2 = -((float)gameRenderer.rendererUpdateCount + partialTicks) * (float)i;
            matrixstack.multiply(vector3f.getDegreesQuaternion(f2));
        }

        Matrix4f matrix4f = matrixstack.peek().getModel();
        gameRenderer.func_228379_a_(matrix4f);

        projectionMatrixThisFrame = matrix4f;
        return projectionMatrixThisFrame;
    }
}