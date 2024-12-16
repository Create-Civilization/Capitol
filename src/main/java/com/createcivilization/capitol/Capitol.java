package com.createcivilization.capitol;

import com.createcivilization.capitol.block.CapitolBlocks;
import com.createcivilization.capitol.block.entity.CapitolBlockEntities;
import com.createcivilization.capitol.item.CapitolItems;

import net.minecraft.server.MinecraftServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.*;

import wiiu.mavity.util.ObjectHolder;

// git push origin main
// git pull
@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ObjectHolder<MinecraftServer> server = new ObjectHolder<>();

    @SuppressWarnings("removal") // Forge docs are gaslighting when it says to place FMLJavaModLoadingContext in the constructor
    public Capitol() {
        IEventBus dumbassFeatureThatIsNotNeededAndProvenToBePointlessByFabric = FMLJavaModLoadingContext.get().getModEventBus();

        dumbassFeatureThatIsNotNeededAndProvenToBePointlessByFabric.addListener(this::commonSetup);

        CapitolItems.register(dumbassFeatureThatIsNotNeededAndProvenToBePointlessByFabric);
        CapitolBlocks.register(dumbassFeatureThatIsNotNeededAndProvenToBePointlessByFabric);
        CapitolBlockEntities.register(dumbassFeatureThatIsNotNeededAndProvenToBePointlessByFabric);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}
}