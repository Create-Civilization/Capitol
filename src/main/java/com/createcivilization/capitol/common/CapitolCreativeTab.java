package com.createcivilization.capitol.common;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.item.CapitolItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CapitolCreativeTab {

	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Capitol.MOD_ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CAPITOL = CREATIVE_MODE_TABS.register(
		"capitol",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.capitol"))
			.icon(() -> CapitolItems.CAPITOL_BLOCK.get().getDefaultInstance())
			.displayItems((parameters, output) -> output.acceptAll(
				CapitolItems.ITEMS.getEntries().stream()
					.map(item -> item.get().getDefaultInstance())
					.toList()
			))
			.build()
	);

	public static void register(IEventBus eventBus) {
		CREATIVE_MODE_TABS.register(eventBus);
	}
}
