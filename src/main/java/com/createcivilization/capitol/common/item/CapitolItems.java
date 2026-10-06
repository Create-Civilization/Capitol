package com.createcivilization.capitol.common.item;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.block.CapitolBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CapitolItems {

	public static final DeferredRegister<Item> ITEMS =
		DeferredRegister.create(Registries.ITEM, Capitol.MOD_ID);

	public static final DeferredHolder<Item, Item> SUB_CLAIM_WAND = ITEMS.register(
		"sub_claim_wand",
		() -> new SubClaimWand(new Item.Properties()
    	.durability(20)
    	.stacksTo(1)
		)
	);

	public static final DeferredHolder<Item, BlockItem> CAPITOL_BLOCK = ITEMS.register(
		"capitol_block", 
		() -> new BlockItem(CapitolBlocks.CAPITOL_BLOCK.get(), new Item.Properties()
		.stacksTo(1)
		)
	);

	public static final DeferredHolder<Item, BlockItem> OUTPOST_BLOCK = ITEMS.register(
		"outpost_block",
		() -> new BlockItem(CapitolBlocks.OUTPOST_BLOCK.get(), new Item.Properties()
		.stacksTo(1)
		)
	);

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}
