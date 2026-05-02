package com.createcivilization.capitol.client.renderer;

import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.data.Team;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;

public class BorderRenderer {

	public static float LINE_THICKNESS = 0.25f;
	private static final float Z_FIGHT_OFFSET = 0.002f;
	private static final float BORDER_ALPHA = 0.85f;
	private static final int DISTANCE = 512;

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
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
		for (var entry : new ArrayList<>(claims.entrySet())) {
			ChunkPos chunkPos = entry.getKey();
			if (!mc.level.getChunkSource().hasChunk(chunkPos.x, chunkPos.z)) continue;
			Team team = entry.getValue();
			Color color = team.getColor();
			if (isBorder(team, chunkPos.x, chunkPos.z - 1)) drawSide(buffer, matrix, mc.level, chunkPos, Direction.NORTH, color);
			if (isBorder(team, chunkPos.x, chunkPos.z + 1)) drawSide(buffer, matrix, mc.level, chunkPos, Direction.SOUTH, color);
			if (isBorder(team, chunkPos.x - 1, chunkPos.z)) drawSide(buffer, matrix, mc.level, chunkPos, Direction.WEST, color);
			if (isBorder(team, chunkPos.x + 1, chunkPos.z)) drawSide(buffer, matrix, mc.level, chunkPos, Direction.EAST, color);
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

	private static boolean occluding(Level level, int x, int y, int z) {
		return level.getBlockState(new BlockPos(x, y, z)).canOcclude();
	}

	private static int surfaceY(Level level, int x, int z) {
		return level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
	}

	private static void horizontalQuad(BufferBuilder buffer, Matrix4f matrix, boolean alongX, int blockX, int blockZ, float y, float boundary, float inner, float r, float g, float b) {
		if (alongX) {
			buffer.addVertex(matrix, blockX, y, boundary).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, blockX + 1, y, boundary).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, blockX + 1, y, inner).setColor(r, g, b, 0f);
			buffer.addVertex(matrix, blockX, y, inner).setColor(r, g, b, 0f);
		} else {
			buffer.addVertex(matrix, boundary, y, blockZ).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, boundary, y, blockZ + 1).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, inner, y, blockZ + 1).setColor(r, g, b, 0f);
			buffer.addVertex(matrix, inner, y, blockZ).setColor(r, g, b, 0f);
		}
	}

	private static void outwardQuad(BufferBuilder buffer, Matrix4f matrix, boolean alongX,
			int blockX, int blockZ, int y, float outwardBoundary,
			float r, float g, float b) {
		if (alongX) {
			buffer.addVertex(matrix, blockX, y + 1f, outwardBoundary).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, blockX + 1, y + 1f, outwardBoundary).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, blockX + 1, y, outwardBoundary).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, blockX, y, outwardBoundary).setColor(r, g, b, BORDER_ALPHA);
		} else {
			buffer.addVertex(matrix, outwardBoundary, y + 1f, blockZ).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, outwardBoundary, y + 1f, blockZ + 1).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, outwardBoundary, y, blockZ + 1).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, outwardBoundary, y, blockZ).setColor(r, g, b, BORDER_ALPHA);
		}
	}

	private static void lateralQuad(BufferBuilder buffer, Matrix4f matrix, boolean alongX,
			int y, float faceCoord, float boundary, float inner,
			float r, float g, float b) {
		if (alongX) {
			buffer.addVertex(matrix, faceCoord, y + 1f, boundary).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, faceCoord, y, boundary).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, faceCoord, y, inner).setColor(r, g, b, 0f);
			buffer.addVertex(matrix, faceCoord, y + 1f, inner).setColor(r, g, b, 0f);
		} else {
			buffer.addVertex(matrix, boundary, y + 1f, faceCoord).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, boundary, y, faceCoord).setColor(r, g, b, BORDER_ALPHA);
			buffer.addVertex(matrix, inner, y, faceCoord).setColor(r, g, b, 0f);
			buffer.addVertex(matrix, inner, y + 1f, faceCoord).setColor(r, g, b, 0f);
		}
	}

	private static void drawSide(BufferBuilder buffer, Matrix4f matrix, Level level, ChunkPos chunk, Direction side, Color color) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		int minX = chunk.getMinBlockX();
		int minZ = chunk.getMinBlockZ();
		int minBuildHeight = level.getMinBuildHeight();
		int maxBuildHeight = level.getMaxBuildHeight();

		float boundary, inner, inwardNudge;
		boolean alongX;
		switch (side) {
			case NORTH -> {
				boundary = minZ;
				inner = minZ + LINE_THICKNESS;
				inwardNudge = Z_FIGHT_OFFSET;
				alongX = true;
			}
			case SOUTH -> {
				boundary = minZ + 16f;
				inner = minZ + 16f - LINE_THICKNESS;
				inwardNudge = -Z_FIGHT_OFFSET;
				alongX = true;
			}
			case WEST -> {
				boundary = minX;
				inner = minX + LINE_THICKNESS;
				inwardNudge = Z_FIGHT_OFFSET;
				alongX = false;
			}
			default -> {
				boundary = minX + 16f;
				inner = minX + 16f - LINE_THICKNESS;
				inwardNudge = -Z_FIGHT_OFFSET;
				alongX = false;
			}
		}

		for (int i = 0; i < 16; i++) {
			int blockX, blockZ, neighborX, neighborZ;
			switch (side) {
				case NORTH -> {
					blockX = minX + i;
					blockZ = minZ;
					neighborX = blockX;
					neighborZ = blockZ - 1;
				}
				case SOUTH -> {
					blockX = minX + i;
					blockZ = minZ + 15;
					neighborX = blockX;
					neighborZ = blockZ + 1;
				}
				case WEST -> {
					blockX = minX;
					blockZ = minZ + i;
					neighborX = blockX - 1;
					neighborZ = blockZ;
				}
				default -> {
					blockX = minX + 15;
					blockZ = minZ + i;
					neighborX = blockX + 1;
					neighborZ = blockZ;
				}
			}

			int scanTop = Math.clamp(Math.min(surfaceY(level, blockX, blockZ), maxBuildHeight), Math.min(surfaceY(level, neighborX, neighborZ), maxBuildHeight), maxBuildHeight);
			float outwardBoundary = boundary + inwardNudge;

			for (int y = minBuildHeight; y < scanTop; y++) {
				if (!occluding(level, blockX, y, blockZ)) continue;

				if (!occluding(level, blockX, y + 1, blockZ))
					horizontalQuad(buffer, matrix, alongX, blockX, blockZ, y + 1f + Z_FIGHT_OFFSET, boundary, inner, r, g, b);

				if (!occluding(level, blockX, y - 1, blockZ))
					horizontalQuad(buffer, matrix, alongX, blockX, blockZ, y - Z_FIGHT_OFFSET, boundary, inner, r, g, b);

				if (!occluding(level, neighborX, y, neighborZ))
					outwardQuad(buffer, matrix, alongX, blockX, blockZ, y, outwardBoundary, r, g, b);
			}

			if (i < 15) {
				int nextBlockX = alongX ? blockX + 1 : blockX;
				int nextBlockZ = alongX ? blockZ : blockZ + 1;
				int lateralTop = Math.clamp(scanTop, Math.min(surfaceY(level, nextBlockX, nextBlockZ), maxBuildHeight), maxBuildHeight);

				for (int y = minBuildHeight; y < lateralTop; y++) {
					boolean currentSolid = occluding(level, blockX, y, blockZ);
					boolean nextSolid = occluding(level, nextBlockX, y, nextBlockZ);
					if (currentSolid == nextSolid) continue;

					float faceCoord = (alongX ? blockX + 1f : blockZ + 1f) + (currentSolid ? Z_FIGHT_OFFSET : -Z_FIGHT_OFFSET);
					lateralQuad(buffer, matrix, alongX, y, faceCoord, boundary, inner, r, g, b);
				}
			}
		}
	}
}