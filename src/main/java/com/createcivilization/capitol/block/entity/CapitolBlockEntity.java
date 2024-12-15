package com.createcivilization.capitol.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("all")
public class CapitolBlockEntity extends BlockEntity implements MenuProvider {

    public CapitolBlockEntity(BlockPos pos, BlockState state) {
        super(CapitolBlockEntities.CAPITOL_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Capitol");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }
}