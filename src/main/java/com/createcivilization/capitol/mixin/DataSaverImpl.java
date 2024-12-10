package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.util.*;

import com.google.gson.stream.JsonReader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;

@SuppressWarnings("all")
@Mixin(MinecraftServer.class)
public abstract class DataSaverImpl implements IDataSaver {

    private CompoundTag data;

    @Override
    public CompoundTag getData() {
        if (data == null) data = new CompoundTag();
        return data;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;saveAll()V", shift = At.Shift.BEFORE), method = "saveEverything")
    private void saveData(boolean p_195515_, boolean p_195516_, boolean p_195517_, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("Lmao no");
        System.exit(0);
    }

    @Mixin(DedicatedServer.class)
    public abstract static class DataSaverImplButWeSaveTheData {

        @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;get()Ljava/lang/Object;", shift = At.Shift.BEFORE), method = "initServer")
        public void initServer(CallbackInfoReturnable<Boolean> cir) {
            try {
                JsonReader reader = new JsonReader(new FileReader(TeamUtils.getTeamDataFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}