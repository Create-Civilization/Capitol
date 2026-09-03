package com.createcivilization.capitol.client.renderer;

import com.createcivilization.capitol.client.networking.ClientSubClaimCache;
import com.createcivilization.capitol.client.networking.SubClaimRenderData;
import com.createcivilization.capitol.common.item.SubClaimWand;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;

// renders sub-claims as a yellow box while the wand is held (registered in Capitol.java)
public class SubClaimBorderRenderer {

	private static final float OUTLINE_R = 1.0f;
	private static final float OUTLINE_G = 1.0f;
	private static final float OUTLINE_B = 0.0f;
	private static final float OUTLINE_A = 1.0f;

	private static final float REVEAL_RADIUS = 24.0f;
	private static final float INNER_RADIUS = 2.0f;
	private static final float MAX_ALPHA = 0.15f;

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;

		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof SubClaimWand)) return;

		if (ClientSubClaimCache.subclaims.isEmpty()) return;

		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f matrix = poseStack.last().pose();

		String dimension = mc.level.dimension().location().toString();
		float playerX = (float) camera.x;
		float playerY = (float) camera.y;
		float playerZ = (float) camera.z;

		renderWalls(matrix, dimension, playerX, playerY, playerZ);
		renderOutlines(poseStack, dimension);

		poseStack.popPose();
	}

	private static void renderWalls(Matrix4f matrix, String dimension, float playerX, float playerY, float playerZ) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		for (var entry : new ArrayList<>(ClientSubClaimCache.subclaims.entrySet())) {
			SubClaimRenderData data = entry.getValue();
			if (!data.dimension().equals(dimension)) continue;
			drawFaces(buffer, matrix, data, playerX, playerY, playerZ);
		}

		MeshData mesh = buffer.build();
		if (mesh != null) BufferUploader.drawWithShader(mesh);

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void drawFaces(BufferBuilder buffer, Matrix4f matrix, SubClaimRenderData data,
			float px, float py, float pz) {
		float minX = data.minX(), minY = data.minY(), minZ = data.minZ();
		float maxX = data.maxX(), maxY = data.maxY(), maxZ = data.maxZ();

		// top & bottom
		if (Math.abs(py - maxY) <= REVEAL_RADIUS) {
			drawFace(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, px, py, pz);
		}
		if (Math.abs(py - minY) <= REVEAL_RADIUS) {
			drawFace(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, px, py, pz);
		}
		// north & south
		if (Math.abs(pz - minZ) <= REVEAL_RADIUS) {
			drawFace(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, px, py, pz);
		}
		if (Math.abs(pz - maxZ) <= REVEAL_RADIUS) {
			drawFace(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, px, py, pz);
		}
		// west & east
		if (Math.abs(px - minX) <= REVEAL_RADIUS) {
			drawFace(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, px, py, pz);
		}
		if (Math.abs(px - maxX) <= REVEAL_RADIUS) {
			drawFace(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, px, py, pz);
		}
	}

	private static void drawFace(BufferBuilder buffer, Matrix4f matrix,
			float x0, float y0, float z0,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float px, float py, float pz) {
		float a0 = alpha(x0, y0, z0, px, py, pz);
		float a1 = alpha(x1, y1, z1, px, py, pz);
		float a2 = alpha(x2, y2, z2, px, py, pz);
		float a3 = alpha(x3, y3, z3, px, py, pz);
		if (a0 <= 0 && a1 <= 0 && a2 <= 0 && a3 <= 0) return;

		buffer.addVertex(matrix, x0, y0, z0).setColor(OUTLINE_R, OUTLINE_G, OUTLINE_B, a0);
		buffer.addVertex(matrix, x1, y1, z1).setColor(OUTLINE_R, OUTLINE_G, OUTLINE_B, a1);
		buffer.addVertex(matrix, x2, y2, z2).setColor(OUTLINE_R, OUTLINE_G, OUTLINE_B, a2);
		buffer.addVertex(matrix, x3, y3, z3).setColor(OUTLINE_R, OUTLINE_G, OUTLINE_B, a3);
	}

	private static float alpha(float vertexX, float vertexY, float vertexZ, float playerX, float playerY, float playerZ) {
		float dx = vertexX - playerX;
		float dy = vertexY - playerY;
		float dz = vertexZ - playerZ;
		float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist >= REVEAL_RADIUS) return 0f;
		float alpha = MAX_ALPHA * (1f - dist / REVEAL_RADIUS);
		if (dist < INNER_RADIUS) alpha *= dist / INNER_RADIUS;
		return alpha;
	}

	private static void renderOutlines(PoseStack poseStack, String dimension) {
		for (var entry : new ArrayList<>(ClientSubClaimCache.subclaims.entrySet())) {
			SubClaimRenderData data = entry.getValue();
			if (!data.dimension().equals(dimension)) continue;
			drawAABBOutline(poseStack, new AABB(data.minX(), data.minY(), data.minZ(), data.maxX(), data.maxY(), data.maxZ()),
				OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A);
		}
	}

	private static void drawAABBOutline(PoseStack poseStack, AABB box, float r, float g, float b, float a) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		RenderSystem.lineWidth(32.0f);

		// RenderType.lines() handles its own shader
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer buf = bufferSource.getBuffer(RenderType.lines());

		// 1.21.1: setNormal() wants PoseStack.Pose, not Matrix3f
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

	private static void addLine(
    	VertexConsumer buf,
    	PoseStack.Pose pose,
    	Matrix4f mat,
    	float x0, float y0, float z0,
    	float x1, float y1, float z1,
    	float r, float g, float b, float a
	) {
		float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len <= 0.0f) len = 1.0f;
		float nx = dx / len, ny = dy / len, nz = dz / len;

		buf.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
		buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
	}
}