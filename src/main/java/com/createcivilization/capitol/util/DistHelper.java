package com.createcivilization.capitol.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

import java.util.function.Supplier;

public class DistHelper {

	public static void runWhenOn(Dist dist, Supplier<Runnable> toRun) {
		if (FMLLoader.getDist() != dist) return;
		toRun.get().run();
	}

	public static void runWhenOnClient(Supplier<Runnable> toRun) {
		runWhenOn(Dist.CLIENT, toRun);
	}

	public static void runWhenOnServer(Supplier<Runnable> toRun) {
		runWhenOn(Dist.DEDICATED_SERVER, toRun);
	}
}