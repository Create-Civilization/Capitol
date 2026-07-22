package com.createcivilization.capitol.client.item;

import com.createcivilization.capitol.Capitol;
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

// bus = EventBusSubscriber.Bus.GAME removed — deprecated for removal in NeoForge 21.1.x; GAME is the default
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

		Vec3 look = mc.player.getLookAngle();
		Direction bestDir = null;
		double bestDot = Double.NEGATIVE_INFINITY;
		for (Direction dir : Direction.values()) {
			Vec3 normal = Vec3.atLowerCornerOf(dir.getNormal());
			double dot = look.dot(normal);
			if (dot > bestDot) {
				bestDot = dot;
				bestDir = dir;
			}
		}

		if (bestDir == null) return;

		Direction faceLookedAt = bestDir.getOpposite();

		String key = switch (faceLookedAt) {
			case EAST  -> SubClaimWand.OFF_POS_X;
			case WEST  -> SubClaimWand.OFF_NEG_X;
			case UP    -> SubClaimWand.OFF_POS_Y;
			case DOWN  -> SubClaimWand.OFF_NEG_Y;
			case SOUTH -> SubClaimWand.OFF_POS_Z;
			case NORTH -> SubClaimWand.OFF_NEG_Z;
		};

		// Read → mutate → write back (required by the DataComponents API)
		CompoundTag tag = SubClaimWand.readTag(stack);
		int value = tag.getInt(key);
		value = Math.clamp(value + delta, -32, 32);
		tag.putInt(key, value);
		SubClaimWand.writeTag(stack, tag);

		event.setCanceled(true);
	}

	@Nullable
	public static AABB getEffectiveBox(ItemStack stack, @Nullable BlockPos cursorPos) {
		int phase = SubClaimWand.getPhase(stack);
		if (phase == 0) return null;

		BlockPos first = SubClaimWand.getFirstPos(stack);
		if (first == null) return null;

		BlockPos second;
		if (phase == 1) {
			if (cursorPos == null) return null;
			second = cursorPos;
		} else {
			second = SubClaimWand.getSecondPos(stack);
			if (second == null) return null;
		}

		double minX = Math.min(first.getX(), second.getX());
		double minY = Math.min(first.getY(), second.getY());
		double minZ = Math.min(first.getZ(), second.getZ());
		double maxX = Math.max(first.getX(), second.getX()) + 1.0D;
		double maxY = Math.max(first.getY(), second.getY()) + 1.0D;
		double maxZ = Math.max(first.getZ(), second.getZ()) + 1.0D;

		// readTag never returns null — empty CompoundTag if no data present
		CompoundTag tag = SubClaimWand.readTag(stack);
		maxX += tag.getInt(SubClaimWand.OFF_POS_X);
		minX -= tag.getInt(SubClaimWand.OFF_NEG_X);
		maxY += tag.getInt(SubClaimWand.OFF_POS_Y);
		minY -= tag.getInt(SubClaimWand.OFF_NEG_Y);
		maxZ += tag.getInt(SubClaimWand.OFF_POS_Z);
		minZ -= tag.getInt(SubClaimWand.OFF_NEG_Z);

		if (maxX - minX < 1.0D) {
			double center = (minX + maxX) * 0.5D;
			minX = center - 0.5D;
			maxX = center + 0.5D;
		}
		if (maxY - minY < 1.0D) {
			double center = (minY + maxY) * 0.5D;
			minY = center - 0.5D;
			maxY = center + 0.5D;
		}
		if (maxZ - minZ < 1.0D) {
			double center = (minZ + maxZ) * 0.5D;
			minZ = center - 0.5D;
			maxZ = center + 0.5D;
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}
}