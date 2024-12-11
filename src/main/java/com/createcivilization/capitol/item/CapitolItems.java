package com.createcivilization.capitol.item;

import com.createcivilization.capitol.Capitol;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CapitolItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Capitol.MOD_ID);




    public static void  register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
