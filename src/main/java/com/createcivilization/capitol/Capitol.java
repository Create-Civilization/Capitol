package com.createcivilization.capitol;

import com.createcivilization.capitol.block.CapitolBlocks;
import com.createcivilization.capitol.block.entity.CapitolBlockEntities;
import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.item.CapitolItems;
import com.createcivilization.capitol.util.PacketHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// git push origin main
// git pull
@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";

    @SuppressWarnings("removal") // Forge docs are gaslighting when it says to place FMLJavaModLoadingContext in the constructor
    public Capitol() {
		FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus ew = context.getModEventBus();
		context.registerConfig(ModConfig.Type.SERVER, CapitolConfig.SERVER_SPEC);

        ew.addListener(this::commonSetup);

        CapitolItems.register(ew);
        CapitolBlocks.register(ew);
        CapitolBlockEntities.register(ew);

        MinecraftForge.EVENT_BUS.register(this);

		// Don't ask.
		boolean cake = true & false;
		System.out.println(cake);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(PacketHandler::register);
	}
}