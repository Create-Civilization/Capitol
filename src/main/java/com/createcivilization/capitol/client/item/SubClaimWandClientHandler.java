package com.createcivilization.capitol.client.item;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.item.SubClaimWand;
import com.createcivilization.capitol.client.screen.SubClaimNamingScreen;
import com.createcivilization.capitol.common.item.SubClaimWand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import javax.annotation.Nullable;

// GAME is the default bus now; the old Bus.GAME got removed in newer NeoForge
@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.CLIENT)
public class SubClaimWandClientHandler {

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		if (!Screen.hasControlDown()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof SubClaimWand)) return;

		int phase = SubClaimWand.getPhase(stack);
		if (phase == 0) return;

		int delta = event.getScrollDeltaY() > 0 ? -1 : 1;

		Direction face = getLookedAtFace(stack);
		if (face == null) return;

		SubClaimWand.BoxCoords box = SubClaimWand.getBoxCoords(stack);

		// scroll up = push the looked-at face in (shrinks), scroll down = pull it out.
		// min faces move opposite to max faces, and every side is clamped to keep a
		// 1-block gap from its opposite, so pushing stops there instead of crossing
		// or dragging the whole box along.
		int minX = box.minX(), minY = box.minY(), minZ = box.minZ();
		int maxX = box.maxX(), maxY = box.maxY(), maxZ = box.maxZ();

		switch (face) {
			case WEST  -> minX = Math.min(box.minX() - delta, box.maxX() - 1);
			case EAST  -> maxX = Math.max(box.maxX() + delta, box.minX() + 1);
			case DOWN  -> minY = Math.min(box.minY() - delta, box.maxY() - 1);
			case UP    -> maxY = Math.max(box.maxY() + delta, box.minY() + 1);
			case NORTH -> minZ = Math.min(box.minZ() - delta, box.maxZ() - 1);
			case SOUTH -> maxZ = Math.max(box.maxZ() + delta, box.minZ() + 1);
			default -> { }
		}

		SubClaimWand.setBoxCoords(stack, new SubClaimWand.BoxCoords(minX, minY, minZ, maxX, maxY, maxZ));

		event.setCanceled(true);
	}

	/** which face of the box are we looking at? cast a ray from the eye through the box,
	 *  any spot on any visible face counts — not just faces of the original volume.
	 *  falls back to the dominant look axis if the ray misses (e.g. box behind you) */
	@Nullable
	private static Direction getLookedAtFace(ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return null;

		SubClaimWand.BoxCoords box = SubClaimWand.getBoxCoords(stack);
		AABB bounds = new AABB(
			box.minX(), box.minY(), box.minZ(),
			box.maxX() + 1.0D, box.maxY() + 1.0D, box.maxZ() + 1.0D
		);

		Vec3 origin = mc.player.getEyePosition();
		Vec3 dir = mc.player.getLookAngle();

		// inside the box? use the look direction instead, so it can still be pushed outward
		if (bounds.contains(origin)) {
			Direction dominant = null;
			double best = Double.NEGATIVE_INFINITY;
			for (Direction d : Direction.values()) {
				double dot = dir.dot(Vec3.atLowerCornerOf(d.getNormal()));
				if (dot > best) {
					best = dot;
					dominant = d;
				}
			}
			return dominant;
		}

		// standard slab ray/AABB test
		double entryDistance = 0.0D;
		double exitDistance = Double.POSITIVE_INFINITY;
		double entryX = origin.x, entryY = origin.y, entryZ = origin.z;

		double[] originCoords = { origin.x, origin.y, origin.z };
		double[] directionCoords = { dir.x, dir.y, dir.z };
		double[] boxMin = { bounds.minX, bounds.minY, bounds.minZ };
		double[] boxMax = { bounds.maxX, bounds.maxY, bounds.maxZ };

		for (int axis = 0; axis < 3; axis++) {
			double nearDistance;
			double farDistance;
			if (Math.abs(directionCoords[axis]) < 1.0E-6D) {
				if (originCoords[axis] < boxMin[axis] || originCoords[axis] > boxMax[axis]) return null;
				continue;
			}
			double inverseDirection = 1.0D / directionCoords[axis];
			nearDistance = (boxMin[axis] - originCoords[axis]) * inverseDirection;
			farDistance = (boxMax[axis] - originCoords[axis]) * inverseDirection;
			if (nearDistance > farDistance) {
				double swap = nearDistance;
				nearDistance = farDistance;
				farDistance = swap;
			}
			if (nearDistance > entryDistance) {
				entryDistance = nearDistance;
				entryX = originCoords[0] + directionCoords[0] * nearDistance;
				entryY = originCoords[1] + directionCoords[1] * nearDistance;
				entryZ = originCoords[2] + directionCoords[2] * nearDistance;
			}
			exitDistance = Math.min(exitDistance, farDistance);
			if (entryDistance > exitDistance) return null;
		}

		if (entryDistance <= 0.0D || !(exitDistance >= entryDistance)) return null;

		double tolerance = 1.0E-5D * Math.max(1.0D, Math.max(
			bounds.maxX - bounds.minX,
			Math.max(bounds.maxY - bounds.minY, bounds.maxZ - bounds.minZ)
		));

		if (Math.abs(entryX - bounds.minX) <= tolerance) return Direction.WEST;
		if (Math.abs(entryX - bounds.maxX) <= tolerance) return Direction.EAST;
		if (Math.abs(entryY - bounds.minY) <= tolerance) return Direction.DOWN;
		if (Math.abs(entryY - bounds.maxY) <= tolerance) return Direction.UP;
		if (Math.abs(entryZ - bounds.minZ) <= tolerance) return Direction.NORTH;
		if (Math.abs(entryZ - bounds.maxZ) <= tolerance) return Direction.SOUTH;

		// shouldn't happen for a valid hit; bail rather than guess
		return null;
	}

	@Nullable
	public static AABB getEffectiveBox(ItemStack stack, @Nullable BlockPos cursorPos) {
		int phase = SubClaimWand.getPhase(stack);
		if (phase == 0) return null;

		BlockPos first = SubClaimWand.getFirstPos(stack);
		if (first == null) return null;

		if (phase == 1) {
			if (cursorPos == null) return null;
			BlockPos second = cursorPos;
			double minX = Math.min(first.getX(), second.getX());
			double minY = Math.min(first.getY(), second.getY());
			double minZ = Math.min(first.getZ(), second.getZ());
			double maxX = Math.max(first.getX(), second.getX()) + 1.0D;
			double maxY = Math.max(first.getY(), second.getY()) + 1.0D;
			double maxZ = Math.max(first.getZ(), second.getZ()) + 1.0D;
			return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		}

		SubClaimWand.BoxCoords box = SubClaimWand.getBoxCoords(stack);
		return new AABB(
			box.minX(), box.minY(), box.minZ(),
			box.maxX() + 1.0D, box.maxY() + 1.0D, box.maxZ() + 1.0D
		);
	}

	static {
		SubClaimWand.screenOpener = SubClaimWandClientHandler::openNamingScreen;
	}

	public static void openNamingScreen(BlockPos first, BlockPos second, ItemStack stack) {
		Minecraft.getInstance().setScreen(new SubClaimNamingScreen(first, second, stack));
	}
}