package com.createcivilization.capitol.common.item;

import com.createcivilization.capitol.Capitol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nullable;

public class SubClaimWand extends Item {

	private static final String TAG_PHASE = "Phase";

	private static final String TAG_FIRST_X = "FirstX";
	private static final String TAG_FIRST_Y = "FirstY";
	private static final String TAG_FIRST_Z = "FirstZ";

	private static final String TAG_SECOND_X = "SecondX";
	private static final String TAG_SECOND_Y = "SecondY";
	private static final String TAG_SECOND_Z = "SecondZ";

	public static final String OFF_POS_X = "OffPosX";
	public static final String OFF_NEG_X = "OffNegX";
	public static final String OFF_POS_Y = "OffPosY";
	public static final String OFF_NEG_Y = "OffNegY";
	public static final String OFF_POS_Z = "OffPosZ";
	public static final String OFF_NEG_Z = "OffNegZ";

	public SubClaimWand(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (!ctx.getLevel().isClientSide()) {
			return InteractionResult.PASS;
		}

		ItemStack stack = ctx.getItemInHand();
		CompoundTag tag = readTag(stack);
		int phase = tag.getInt(TAG_PHASE);
		BlockPos clickedPos = ctx.getClickedPos();

		switch (phase) {
			case 0 -> {
				tag.putInt(TAG_FIRST_X, clickedPos.getX());
				tag.putInt(TAG_FIRST_Y, clickedPos.getY());
				tag.putInt(TAG_FIRST_Z, clickedPos.getZ());
				tag.putInt(TAG_PHASE, 1);
				writeTag(stack, tag);
				return InteractionResult.SUCCESS;
			}
			case 1 -> {
				tag.putInt(TAG_SECOND_X, clickedPos.getX());
				tag.putInt(TAG_SECOND_Y, clickedPos.getY());
				tag.putInt(TAG_SECOND_Z, clickedPos.getZ());
				tag.putInt(TAG_PHASE, 2);
				writeTag(stack, tag);
				return InteractionResult.SUCCESS;
			}
			case 2 -> {
				BlockPos first = getFirstPos(stack);
				BlockPos second = getSecondPos(stack);
				Capitol.LOGGER.info("Sub-claim selection complete: {} -> {}", first, second);
				stack.remove(DataComponents.CUSTOM_DATA);
				return InteractionResult.SUCCESS;
			}
			default -> {
				return InteractionResult.PASS;
			}
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return getPhase(stack) > 0;
	}

	// ── DataComponents tag helpers ─────────────────────────────────────────────

	/**
	 * Reads the CUSTOM_DATA CompoundTag from the stack, returning an empty tag if absent.
	 * Always call writeTag() after mutating the result.
	 */
	public static CompoundTag readTag(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null ? data.copyTag() : new CompoundTag();
	}

	/**
	 * Writes a CompoundTag back into the stack's CUSTOM_DATA component.
	 */
	public static void writeTag(ItemStack stack, CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	// ── Field accessors ────────────────────────────────────────────────────────

	public static int getPhase(ItemStack stack) {
		return readTag(stack).getInt(TAG_PHASE);
	}

	public static void setPhase(ItemStack stack, int phase) {
		CompoundTag tag = readTag(stack);
		tag.putInt(TAG_PHASE, phase);
		writeTag(stack, tag);
	}

	@Nullable
	public static BlockPos getFirstPos(ItemStack stack) {
		CompoundTag tag = readTag(stack);
		if (!tag.contains(TAG_FIRST_X)) return null;
		return new BlockPos(tag.getInt(TAG_FIRST_X), tag.getInt(TAG_FIRST_Y), tag.getInt(TAG_FIRST_Z));
	}

	public static void setFirstPos(ItemStack stack, BlockPos pos) {
		CompoundTag tag = readTag(stack);
		tag.putInt(TAG_FIRST_X, pos.getX());
		tag.putInt(TAG_FIRST_Y, pos.getY());
		tag.putInt(TAG_FIRST_Z, pos.getZ());
		writeTag(stack, tag);
	}

	@Nullable
	public static BlockPos getSecondPos(ItemStack stack) {
		CompoundTag tag = readTag(stack);
		if (!tag.contains(TAG_SECOND_X)) return null;
		return new BlockPos(tag.getInt(TAG_SECOND_X), tag.getInt(TAG_SECOND_Y), tag.getInt(TAG_SECOND_Z));
	}

	public static void setSecondPos(ItemStack stack, BlockPos pos) {
		CompoundTag tag = readTag(stack);
		tag.putInt(TAG_SECOND_X, pos.getX());
		tag.putInt(TAG_SECOND_Y, pos.getY());
		tag.putInt(TAG_SECOND_Z, pos.getZ());
		writeTag(stack, tag);
	}
}
