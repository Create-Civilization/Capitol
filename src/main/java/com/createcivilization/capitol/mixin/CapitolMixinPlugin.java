package com.createcivilization.capitol.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Conditionally loads Create-targeting mixins only when Create is on the classpath.
 */
public class CapitolMixinPlugin implements IMixinConfigPlugin {

	private static final String CREATE_PACKAGE = "com.createcivilization.capitol.mixin.create.";
	private boolean createPresent;

	@Override
	public void onLoad(String mixinPackage) {
		createPresent = LoadingModList.get().getModFileById("create") != null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith(CREATE_PACKAGE)) return createPresent;
		return true;
	}

	@Override public String getRefMapperConfig() { return null; }
	@Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override public List<String> getMixins() {
		return List.of(
			"create.ContraptionMixin",
			"create.BlockBreakingMovementBehaviourMixin",
			"create.HarvesterMovementBehaviourMixin",
			"create.PloughMovementBehaviourMixin"
		);
	}
	@Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	@Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
