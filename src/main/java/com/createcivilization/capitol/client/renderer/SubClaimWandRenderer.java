package com.createcivilization.capitol.client.renderer;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.item.SubClaimWandClientHandler;
import com.createcivilization.capitol.common.item.SubClaimWand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import org.joml.Matrix4f;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.CLIENT)
public class SubClaimWandRenderer {

	private static final float OUTLINE_R = 1.0f;
	private static final float OUTLINE_G = 1.0f;
	private static final float OUTLINE_B = 0.0f;
	private static final float OUTLINE_A = 1.0f;

	@SubscribeEvent
	public static void onHighlightBlock(RenderHighlightEvent.Block event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof SubClaimWand)) return;

		event.setCanceled(true);

		BlockPos hovered = event.getTarget().getBlockPos();
		AABB box = new AABB(
			hovered.getX(), hovered.getY(), hovered.getZ(),
			hovered.getX() + 1, hovered.getY() + 1, hovered.getZ() + 1
		).inflate(0.002);

		// The event's PoseStack has camera at origin, so translate by -camera to reach world space
		Vec3 camera = event.getCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		drawAABBOutline(poseStack, box, OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A);
		poseStack.popPose();
	}

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof SubClaimWand)) return;

		int phase = SubClaimWand.getPhase(stack);
		if (phase == 0) return;

		BlockPos cursorPos = null;
		if (mc.hitResult instanceof BlockHitResult blockHit) {
			cursorPos = blockHit.getBlockPos();
		}

		AABB box = SubClaimWandClientHandler.getEffectiveBox(stack, cursorPos);
		if (box == null) return;

		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		Vec3 camera = event.getCamera().getPosition();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		drawAABBOutline(poseStack, box, OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A);
		poseStack.popPose();
	}

	private static void drawAABBOutline(PoseStack poseStack, AABB box, float r, float g, float b, float a) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		RenderSystem.lineWidth(2.0f);

		// RenderType.lines() manages the line shader internally — no manual setShader() needed.
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer buf = bufferSource.getBuffer(RenderType.lines());

		// In 1.21.1, setNormal() takes PoseStack.Pose, not Matrix3f
		PoseStack.Pose pose = poseStack.last();
		Matrix4f mat = pose.pose();

		float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
		float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

		// Bottom face
		addLine(buf, pose, mat, x0, y0, z0, x1, y0, z0, r, g, b, a);
		addLine(buf, pose, mat, x1, y0, z0, x1, y0, z1, r, g, b, a);
		addLine(buf, pose, mat, x1, y0, z1, x0, y0, z1, r, g, b, a);
		addLine(buf, pose, mat, x0, y0, z1, x0, y0, z0, r, g, b, a);
		// Top face
		addLine(buf, pose, mat, x0, y1, z0, x1, y1, z0, r, g, b, a);
		addLine(buf, pose, mat, x1, y1, z0, x1, y1, z1, r, g, b, a);
		addLine(buf, pose, mat, x1, y1, z1, x0, y1, z1, r, g, b, a);
		addLine(buf, pose, mat, x0, y1, z1, x0, y1, z0, r, g, b, a);
		// Vertical edges
		addLine(buf, pose, mat, x0, y0, z0, x0, y1, z0, r, g, b, a);
		addLine(buf, pose, mat, x1, y0, z0, x1, y1, z0, r, g, b, a);
		addLine(buf, pose, mat, x1, y0, z1, x1, y1, z1, r, g, b, a);
		addLine(buf, pose, mat, x0, y0, z1, x0, y1, z1, r, g, b, a);

		bufferSource.endBatch(RenderType.lines());

		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.lineWidth(1.0f);
	}

	private static void addLine(VertexConsumer buf, PoseStack.Pose pose, Matrix4f mat,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float r, float g, float b, float a) {
		float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len == 0.0f) len = 1.0f;
		float nx = dx / len, ny = dy / len, nz = dz / len;

		buf.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
		buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
	}
}