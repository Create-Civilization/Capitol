package com.createcivilization.capitol.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CapitolConfig {

	public static final ModConfigSpec SPEC;

	// Claims
	public static final ModConfigSpec.IntValue MAX_TEAM_CLAIMS;
	public static final ModConfigSpec.IntValue MAX_CLAIM_DISTANCE;
	public static final ModConfigSpec.EnumValue<ListType> CLAIMABLE_DIMENSIONS_LIST_TYPE;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> CLAIMABLE_DIMENSIONS;

	// Forceloading
	public static final ModConfigSpec.BooleanValue FORCELOAD_ENABLED;
	public static final ModConfigSpec.IntValue FORCELOAD_MAX_PER_TEAM;

	// Server-wide protection toggles
	public static final ModConfigSpec.BooleanValue PROTECT_FIRE_SPREAD;
	public static final ModConfigSpec.BooleanValue PROTECT_PISTONS;
	public static final ModConfigSpec.BooleanValue PROTECT_FLUID_FLOW;
	public static final ModConfigSpec.BooleanValue PROTECT_DISPENSERS;
	public static final ModConfigSpec.BooleanValue PROTECT_CROP_TRAMPLING;
	public static final ModConfigSpec.BooleanValue PROTECT_SCULK;

	// Team-level protection defaults
	public static final ModConfigSpec.BooleanValue DEFAULT_EXPLOSION;
	public static final ModConfigSpec.BooleanValue DEFAULT_MOB_GRIEFING;
	public static final ModConfigSpec.BooleanValue DEFAULT_FIRE;
	public static final ModConfigSpec.BooleanValue DEFAULT_PISTON;
	public static final ModConfigSpec.BooleanValue DEFAULT_FLUID_FLOW;
	public static final ModConfigSpec.BooleanValue DEFAULT_DISPENSER;
	public static final ModConfigSpec.BooleanValue DEFAULT_CROP_TRAMPLING;
	public static final ModConfigSpec.BooleanValue DEFAULT_SCULK;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> TEAM_CONFIGURABLE_PROTECTIONS;

	// Entity lists
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITIES_ALLOWED_TO_GRIEF;
	public static final ModConfigSpec.EnumValue<ListType> ENTITY_PROTECTION_LIST_TYPE;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> PROTECTED_ENTITIES;

	// Exceptions
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_CLAIM_BARRIER;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_INTERACTION_EXCEPTIONS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_BREAK_EXCEPTIONS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_PLACE_EXCEPTIONS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_USE_EXCEPTIONS;

	public enum ListType {
		ONLY,
		ALL_BUT
	}

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		builder.comment("Claim settings").push("claims");

		MAX_TEAM_CLAIMS = builder
			.comment("Maximum chunks a team can claim.")
			.defineInRange("maxTeamClaims", 1000, 1, Integer.MAX_VALUE);

		MAX_CLAIM_DISTANCE = builder
			.comment("Max distance (in chunks) from existing claims to claim a new chunk.")
			.defineInRange("maxClaimDistance", 5, 1, Integer.MAX_VALUE);

		CLAIMABLE_DIMENSIONS_LIST_TYPE = builder
			.comment("ONLY = only listed dimensions are claimable. ALL_BUT = all except listed.")
			.defineEnum("claimableDimensionsListType", ListType.ALL_BUT);

		CLAIMABLE_DIMENSIONS = builder
			.comment("Dimensions to include/exclude depending on list type above.")
			.defineListAllowEmpty("claimableDimensions", List.of(), () -> "", o -> o instanceof String);

		builder.pop();

		builder.comment("Chunk forceloading settings.").push("forceloading");

		FORCELOAD_ENABLED = builder
			.comment("Whether chunk forceloading is enabled.")
			.define("enabled", true);

		FORCELOAD_MAX_PER_TEAM = builder
			.comment("Max forceloaded chunks per team. 0 = unlimited.")
			.defineInRange("maxPerTeam", 10, 0, Integer.MAX_VALUE);

		builder.pop();

		builder.comment("Server-wide protection toggles for claimed chunks.").push("protection");

		PROTECT_FIRE_SPREAD = builder.comment("Prevent fire spread in claims.").define("fireSpread", true);
		PROTECT_PISTONS = builder.comment("Prevent pistons pushing/pulling across claim boundaries.").define("pistons", true);
		PROTECT_FLUID_FLOW = builder.comment("Prevent fluid flowing across claim boundaries.").define("fluidFlow", true);
		PROTECT_DISPENSERS = builder.comment("Prevent dispensers acting across claim boundaries.").define("dispensers", true);
		PROTECT_CROP_TRAMPLING = builder.comment("Prevent non-player crop trampling in claims.").define("cropTrampling", true);
		PROTECT_SCULK = builder.comment("Prevent sculk spreading in claims.").define("sculk", false);

		builder.comment(
			"Default protection values for new teams. true = protection enabled (event blocked).",
			"Teams can toggle these if listed in teamConfigurableProtections."
		).push("teamDefaults");

		DEFAULT_EXPLOSION = builder.define("defaultExplosion", true);
		DEFAULT_MOB_GRIEFING = builder.define("defaultMobGriefing", true);
		DEFAULT_FIRE = builder.define("defaultFire", true);
		DEFAULT_PISTON = builder.define("defaultPiston", true);
		DEFAULT_FLUID_FLOW = builder.define("defaultFluidFlow", true);
		DEFAULT_DISPENSER = builder.define("defaultDispenser", true);
		DEFAULT_CROP_TRAMPLING = builder.define("defaultCropTrampling", true);
		DEFAULT_SCULK = builder.define("defaultSculk", false);

		TEAM_CONFIGURABLE_PROTECTIONS = builder
			.comment("Which protections teams can toggle. Keys: explosion, mob_griefing, fire, piston, fluid_flow, dispenser, crop_trampling, sculk")
			.defineListAllowEmpty("teamConfigurableProtections", List.of(
				"explosion", "mob_griefing", "fire"
			), () -> "", o -> o instanceof String);

		builder.pop();
		builder.pop();

		builder.comment(
			"Entity griefing lists. Supports resource locations, #tags, *wildcards, and (a|b) grouping."
		).push("entityLists");

		ENTITIES_ALLOWED_TO_GRIEF = builder
			.comment("Entities always allowed to grief blocks in claims.")
			.defineListAllowEmpty("entitiesAllowedToGrief", List.of("minecraft:sheep"), () -> "", o -> o instanceof String);

		ENTITY_PROTECTION_LIST_TYPE = builder
			.comment("ONLY = only listed entities are protected. ALL_BUT = all except listed.")
			.defineEnum("protectedEntitiesListType", ListType.ALL_BUT);

		PROTECTED_ENTITIES = builder
			.comment("Entities affected by kill/interact protection. Interpretation depends on list type above.")
			.defineListAllowEmpty("protectedEntities", List.of(), () -> "", o -> o instanceof String);

		builder.pop();

		builder.comment("Global exception lists that always apply regardless of team settings.").push("exceptions");

		ENTITY_CLAIM_BARRIER = builder
			.comment("Entities always prevented from entering claimed chunks.")
			.defineListAllowEmpty("entityClaimBarrier", List.of(), () -> "", o -> o instanceof String);

		BLOCK_INTERACTION_EXCEPTIONS = builder
			.comment("Blocks anyone can interact with in claims. Supports #tags and *wildcards.")
			.defineListAllowEmpty("blockInteractionExceptions", List.of(
				"minecraft:crafting_table", "minecraft:ender_chest"
			), () -> "", o -> o instanceof String);

		BLOCK_BREAK_EXCEPTIONS = builder
			.comment("Blocks anyone can break in claims.")
			.defineListAllowEmpty("blockBreakExceptions", List.of(), () -> "", o -> o instanceof String);

		BLOCK_PLACE_EXCEPTIONS = builder
			.comment("Blocks anyone can place in claims.")
			.defineListAllowEmpty("blockPlaceExceptions", List.of(), () -> "", o -> o instanceof String);

		ITEM_USE_EXCEPTIONS = builder
			.comment("Items anyone can use in claims.")
			.defineListAllowEmpty("itemUseExceptions", List.of(), () -> "", o -> o instanceof String);

		builder.pop();

		SPEC = builder.build();
	}
}
