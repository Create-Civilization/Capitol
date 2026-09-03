package com.createcivilization.capitol.client.renderer;

import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.data.Team;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BorderWallRenderer {

	public static boolean showBorders = true;
	public static float REVEAL_RADIUS = 8.0f;
	public static float INNER_RADIUS = 1.5f;
	public static float MAX_ALPHA = 0.35f;
	private static final float Z_FIGHT_OFFSET = -0.002f;

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (!showBorders) return;
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		var claims = ClientClaimCache.get();
		if (claims.isEmpty()) return;

		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f matrix = poseStack.last().pose();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		float playerX = (float) camera.x;
		float playerY = (float) camera.y;
		float playerZ = (float) camera.z;

		for (var entry : cachedEntries) {
			ChunkPos chunkPos = entry.getKey();
			Team team = entry.getValue();
			Color color = team.getColor();
			if (isBorder(team, chunkPos.x, chunkPos.z - 1)) drawWall(buffer, matrix, chunkPos, true, chunkPos.getMinBlockZ(), true, color, playerX, playerY, playerZ);
			if (isBorder(team, chunkPos.x, chunkPos.z + 1)) drawWall(buffer, matrix, chunkPos, true, chunkPos.getMinBlockZ() + 16, false, color, playerX, playerY, playerZ);
			if (isBorder(team, chunkPos.x - 1, chunkPos.z)) drawWall(buffer, matrix, chunkPos, false, chunkPos.getMinBlockX(), true, color, playerX, playerY, playerZ);
			if (isBorder(team, chunkPos.x + 1, chunkPos.z)) drawWall(buffer, matrix, chunkPos, false, chunkPos.getMinBlockX() + 16, false, color, playerX, playerY, playerZ);
		}

		MeshData mesh = buffer.build();
		if (mesh != null) BufferUploader.drawWithShader(mesh);

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		poseStack.popPose();
	}

	private static boolean isBorder(Team team, int chunkX, int chunkZ) {
		Team neighbor = ClientClaimCache.getClaim(new ChunkPos(chunkX, chunkZ));
		return neighbor == null || !neighbor.getId().equals(team.getId());
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

	private static void drawWall(BufferBuilder buffer, Matrix4f matrix, ChunkPos chunk, boolean onZ, float coord, boolean inward, Color color, float playerX, float playerY, float playerZ) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		coord += inward ? Z_FIGHT_OFFSET : -Z_FIGHT_OFFSET;

		float planeDist = onZ ? Math.abs(coord - playerZ) : Math.abs(coord - playerX);
		if (planeDist > REVEAL_RADIUS) return;
		float planeFade = Math.clamp(planeDist / INNER_RADIUS, 0f, 1f);

		int rangeStart = onZ ? chunk.getMinBlockX() : chunk.getMinBlockZ();
		int yBottom = (int) Math.floor(playerY - REVEAL_RADIUS);
		int yTop = (int) Math.ceil(playerY + REVEAL_RADIUS);

		for (int i = 0; i < 16; i++) {
			float pos = rangeStart + i;
			float mid = pos + 0.5f;
			float axisDist = onZ ? Math.abs(mid - playerX) : Math.abs(mid - playerZ);
			if (axisDist > REVEAL_RADIUS + 1) continue;

			for (int y = yBottom; y < yTop; y++) {
				float x0, z0, x1, z1;
				if (onZ) {
					x0 = pos;
					x1 = pos + 1;
					z0 = coord;
					z1 = coord;
				} else {
					x0 = coord;
					x1 = coord;
					z0 = pos;
					z1 = pos + 1;
				}

				float alpha00 = alpha(x0, y, z0, playerX, playerY, playerZ) * planeFade;
				float alpha10 = alpha(x1, y, z1, playerX, playerY, playerZ) * planeFade;
				float alpha01 = alpha(x0, y + 1, z0, playerX, playerY, playerZ) * planeFade;
				float alpha11 = alpha(x1, y + 1, z1, playerX, playerY, playerZ) * planeFade;
				if (alpha00 <= 0 && alpha10 <= 0 && alpha01 <= 0 && alpha11 <= 0) continue;

				buffer.addVertex(matrix, x0, y + 1f, z0).setColor(r, g, b, alpha01);
				buffer.addVertex(matrix, x1, y + 1f, z1).setColor(r, g, b, alpha11);
				buffer.addVertex(matrix, x1, y, z1).setColor(r, g, b, alpha10);
				buffer.addVertex(matrix, x0, y, z0).setColor(r, g, b, alpha00);
			}
		}
	}
}