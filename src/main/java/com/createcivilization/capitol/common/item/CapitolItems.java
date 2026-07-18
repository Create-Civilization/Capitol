package com.createcivilization.capitol.common.item;

import com.createcivilization.capitol.Capitol;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CapitolItems {

	public static final DeferredRegister<Item> ITEMS =
		DeferredRegister.create(Registries.ITEM, Capitol.MOD_ID);

	public static final DeferredHolder<Item, Item> SUB_CLAIM_WAND = ITEMS.register(
		"sub_claim_wand",
		() -> new Item(new Item.Properties())
	);

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}
