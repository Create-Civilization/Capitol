package com.createcivilization.capitol.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Supplier;

public class DistHelper {

	public static void runWhenOn(Dist dist, Supplier<Runnable> toRun) {
		DistExecutor.unsafeRunWhenOn(dist, toRun);
	}

	public static void runWhenOnClient(Supplier<Runnable> toRun) {
		runWhenOn(Dist.CLIENT, toRun);
	}

	public static void runWhenOnServer(Supplier<Runnable> toRun) {
		runWhenOn(Dist.DEDICATED_SERVER, toRun);
	}
}