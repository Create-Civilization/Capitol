package com.createcivilization.capitol.common.item;

import com.createcivilization.capitol.Capitol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nullable;

// the corner-flipping in BoxCoords is deliberate; silence javac's whine about the record's compact constructor
@SuppressWarnings("deprecation")
public class SubClaimWand extends Item {

	private static final String TAG_PHASE = "Phase";

	private static final String TAG_FIRST_X = "FirstX";
	private static final String TAG_FIRST_Y = "FirstY";
	private static final String TAG_FIRST_Z = "FirstZ";

	private static final String TAG_SECOND_X = "SecondX";
	private static final String TAG_SECOND_Y = "SecondY";
	private static final String TAG_SECOND_Z = "SecondZ";

	// old keys, back when the box was stored as offsets; kept so old wands still migrate
	private static final String OFF_POS_X = "OffPosX";
	private static final String OFF_NEG_X = "OffNegX";
	private static final String OFF_POS_Y = "OffPosY";
	private static final String OFF_NEG_Y = "OffNegY";
	private static final String OFF_POS_Z = "OffPosZ";
	private static final String OFF_NEG_Z = "OffNegZ";

	// box is stored as absolute corner coords, so any face can be pushed around freely
	private static final String TAG_MIN_X = "BoxMinX";
	private static final String TAG_MIN_Y = "BoxMinY";
	private static final String TAG_MIN_Z = "BoxMinZ";
	private static final String TAG_MAX_X = "BoxMaxX";
	private static final String TAG_MAX_Y = "BoxMaxY";
	private static final String TAG_MAX_Z = "BoxMaxZ";

	public record BoxCoords(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		public BoxCoords {
			minX = Math.min(minX, maxX);
			minY = Math.min(minY, maxY);
			minZ = Math.min(minZ, maxZ);
			maxX = Math.max(minX, maxX);
			maxY = Math.max(minY, maxY);
			maxZ = Math.max(minZ, maxZ);
		}
	}

	// current selection box: new wands store absolute corners, old ones get converted from their offsets here
	public static BoxCoords getBoxCoords(ItemStack stack) {
		CompoundTag tag = readTag(stack);

		if (tag.contains(TAG_MIN_X) && tag.contains(TAG_MAX_X)
			&& tag.contains(TAG_MIN_Y) && tag.contains(TAG_MAX_Y)
			&& tag.contains(TAG_MIN_Z) && tag.contains(TAG_MAX_Z)) {
			return new BoxCoords(
				tag.getInt(TAG_MIN_X), tag.getInt(TAG_MIN_Y), tag.getInt(TAG_MIN_Z),
				tag.getInt(TAG_MAX_X), tag.getInt(TAG_MAX_Y), tag.getInt(TAG_MAX_Z)
			);
		}

		// old format: start from the two clicked corners, then apply the stored offsets
		BlockPos first = getFirstPos(stack);
		BlockPos second = getSecondPos(stack);
		if (first == null || second == null) {
			// no corners saved yet — bail with a 1x1 box; scroll fixes it once both corners exist
			return new BoxCoords(0, 0, 0, 1, 1, 1);
		}
		int minX = Math.min(first.getX(), second.getX());
		int minY = Math.min(first.getY(), second.getY());
		int minZ = Math.min(first.getZ(), second.getZ());
		int maxX = Math.max(first.getX(), second.getX());
		int maxY = Math.max(first.getY(), second.getY());
		int maxZ = Math.max(first.getZ(), second.getZ());

		minX -= tag.getInt(OFF_NEG_X);
		maxX += tag.getInt(OFF_POS_X);
		minY -= tag.getInt(OFF_NEG_Y);
		maxY += tag.getInt(OFF_POS_Y);
		minZ -= tag.getInt(OFF_NEG_Z);
		maxZ += tag.getInt(OFF_POS_Z);

		return new BoxCoords(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static void setBoxCoords(ItemStack stack, BoxCoords box) {
		CompoundTag tag = readTag(stack);
		tag.putInt(TAG_MIN_X, box.minX());
		tag.putInt(TAG_MIN_Y, box.minY());
		tag.putInt(TAG_MIN_Z, box.minZ());
		tag.putInt(TAG_MAX_X, box.maxX());
		tag.putInt(TAG_MAX_Y, box.maxY());
		tag.putInt(TAG_MAX_Z, box.maxZ());
		writeTag(stack, tag);
	}

	@FunctionalInterface
	public interface ScreenOpener {
		void open(BlockPos first, BlockPos second, ItemStack stack);
	}

	public static ScreenOpener screenOpener = null;

	public SubClaimWand(Properties properties) {
		super(properties);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		return ItemAttributeModifiers.builder()
			.add(
				Attributes.BLOCK_INTERACTION_RANGE,
				new AttributeModifier(
					ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "sub_claim_wand_range"),
					35.5,
					AttributeModifier.Operation.ADD_VALUE
				),
				EquipmentSlotGroup.MAINHAND
			)
			.build();
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {

		if (!ctx.getLevel().isClientSide()) {
			return InteractionResult.PASS;
		}

		// client side: walk the selection phases
		ItemStack stack = ctx.getItemInHand();
		CompoundTag tag = readTag(stack);
		int phase = tag.getInt(TAG_PHASE);
		BlockPos clickedPos = ctx.getClickedPos();

		// shift right-click while picking = cancel everything
		if (ctx.isSecondaryUseActive() && phase > 0) {
			clearSelection(stack);
			return InteractionResult.SUCCESS;
		}

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
				if (first != null && second != null && screenOpener != null) {
					screenOpener.open(first, second, stack);
				}
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

	// reads the stack's CUSTOM_DATA tag (empty one if there's none); if you change it, writeTag() it back
	public static CompoundTag readTag(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null ? data.copyTag() : new CompoundTag();
	}

	// writes a tag back into the stack's CUSTOM_DATA
	public static void writeTag(ItemStack stack, CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void clearSelection(ItemStack stack) {
		stack.remove(DataComponents.CUSTOM_DATA);
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
