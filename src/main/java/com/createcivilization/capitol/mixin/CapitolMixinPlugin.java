package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.Capitol;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conditionally loads Create-targeting mixins only when Create is on the classpath.
 */
public class CapitolMixinPlugin implements IMixinConfigPlugin {

	private static final Map<String, String> PACKAGE_TO_MOD = Map.of(
		"com.createcivilization.capitol.mixin.create.", "create",
		"com.createcivilization.capitol.mixin.simulated.", "simulated"
	);

	private final Set<String> presentMods = new HashSet<>();

	private boolean createPresent;

	@Override
	public void onLoad(String mixinPackage) {
		for (String modId : PACKAGE_TO_MOD.values()) {
			if (LoadingModList.get().getModFileById(modId) != null)
				Capitol.LOGGER.info("ADDING MOD {}", modId);
				presentMods.add(modId);
		}
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		for (var entry : PACKAGE_TO_MOD.entrySet()) {
			if (mixinClassName.startsWith(entry.getKey()))
				return presentMods.contains(entry.getValue());
		}
		return true;
	}


	@Override public String getRefMapperConfig() { return null; }
	@Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override public List<String> getMixins() {
		return List.of(
			"create.ContraptionMixin",
			"create.BlockBreakingMovementBehaviourMixin",
			"create.BlockBreakingKineticBlockEntityMixin",
			"create.DeployerBlockEntityMixin",
			"simulated.SimAssemblyContraptionMixin",
			"simulated.AssemblePacketMixin"
		);
	}
	@Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	@Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
