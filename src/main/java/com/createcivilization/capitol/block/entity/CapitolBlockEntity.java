package com.createcivilization.capitol.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.*;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("all")
public class CapitolBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    public CapitolBlockEntity(BlockPos pos, BlockState state) {
        super(CapitolBlockEntities.CAPITOL_BE.get(), pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Capitol");
    }

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return null;
	}
}