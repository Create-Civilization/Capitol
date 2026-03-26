package com.createcivilization.capitol.client.renderer;

import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;
import java.util.Map;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class BorderRenderer {

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
		if (ClientClaimCache.get().isEmpty()) return;

		PoseStack poseStack = event.getPoseStack();

		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 camPos = camera.getPosition();

		MultiBufferSource.BufferSource bufferSource =
			Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

		for (Map.Entry<ChunkPos, ?> entry : ClientClaimCache.get().entrySet()) {
			ChunkPos pos = entry.getKey();

			poseStack.pushPose();
			poseStack.translate(
				pos.getMinBlockX() - camPos.x,
				-camPos.y,
				pos.getMinBlockZ() - camPos.z
			);

			LevelRenderer.renderLineBox(
				poseStack, lines,
				0, -64, 0,
				16, 320, 16,
				1.0F, 0.0F, 0.0F, 1.0F
			);

			poseStack.popPose();
		}

		bufferSource.endBatch(RenderType.lines());
	}

}
