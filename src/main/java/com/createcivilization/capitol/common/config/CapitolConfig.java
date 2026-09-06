package com.createcivilization.capitol.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CapitolConfig {

	public static final ModConfigSpec SPEC;

	// Claims
	public static final ModConfigSpec.IntValue MAX_TEAM_CLAIMS;
	public static final ModConfigSpec.IntValue CLAIM_RADIUS;
	public static final ModConfigSpec.EnumValue<ListType> CLAIMABLE_DIMENSIONS_LIST_TYPE;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> CLAIMABLE_DIMENSIONS;
	public static final ModConfigSpec.BooleanValue SUBLEVEL_CLAIM_OVERLAP;
	public static final ModConfigSpec.DoubleValue AUTO_CLAIM_COOLDOWN;

	// Capitol block tiers
	public static final ModConfigSpec.IntValue CAPITOL_VILLAGE_MAX_CLAIMS;
	public static final ModConfigSpec.IntValue CAPITOL_TOWN_MAX_CLAIMS;
	public static final ModConfigSpec.IntValue CAPITOL_CITY_MAX_CLAIMS;
	public static final ModConfigSpec.IntValue CAPITOL_VILLAGE_UPKEEP;
	public static final ModConfigSpec.IntValue CAPITOL_TOWN_UPKEEP;
	public static final ModConfigSpec.IntValue CAPITOL_CITY_UPKEEP;

	// Forceloading
	public static final ModConfigSpec.BooleanValue FORCELOAD_ENABLED;
	public static final ModConfigSpec.IntValue FORCELOAD_MAX_PER_TEAM;

	// Server-wide protection toggles
	public static final ModConfigSpec.BooleanValue PROTECT_FIRE_SPREAD;
	public static final ModConfigSpec.BooleanValue PROTECT_PISTONS;
	public static final ModConfigSpec.BooleanValue PROTECT_FLUID_FLOW;
	public static final ModConfigSpec.BooleanValue PROTECT_CROP_TRAMPLING;

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

		CLAIM_RADIUS = builder
			.comment("Max chunk radius from the player for claim/unclaim actions.")
			.defineInRange("claimRadius", 8, 0, Integer.MAX_VALUE);

		CLAIMABLE_DIMENSIONS_LIST_TYPE = builder
			.comment("ONLY = only listed dimensions are claimable. ALL_BUT = all except listed.")
			.defineEnum("claimableDimensionsListType", ListType.ALL_BUT);

		CLAIMABLE_DIMENSIONS = builder
			.comment("Dimensions to include/exclude depending on list type above.")
			.defineListAllowEmpty("claimableDimensions", List.of(), () -> "", o -> o instanceof String);

		SUBLEVEL_CLAIM_OVERLAP = builder
			.comment("When true makes it so when a sub-level enters someone's claim they gain permission to it as well.",
				"This is good to use if you are worried about players being able to grief with sub-levels,",
				"or don't want a looming airship over your base you can't do anything about")
				.define("sublevelClaimOverlap", true);

		AUTO_CLAIM_COOLDOWN = builder
			.comment("How long a player must wait before being able to claim the next chunk while autoclaiming.",
				"Given in seconds.")
				.defineInRange("autoClaimCooldown", 1.0, 0, Integer.MAX_VALUE);

		builder.pop();

		builder.comment(
			"Capitol block tiers (Village -> Town -> City).",
			"Max claims + upkeep per tier. Not enforced yet - per-block claims/upkeep payment are future work."
		).push("capitolBlockTiers");

		CAPITOL_VILLAGE_MAX_CLAIMS = builder
			.comment("Max claims a Village capitol block controls.")
			.defineInRange("villageMaxClaims", 100, 1, Integer.MAX_VALUE);

		CAPITOL_TOWN_MAX_CLAIMS = builder
			.comment("Max claims a Town capitol block controls.")
			.defineInRange("townMaxClaims", 300, 1, Integer.MAX_VALUE);

		CAPITOL_CITY_MAX_CLAIMS = builder
			.comment("Max claims a City capitol block controls.")
			.defineInRange("cityMaxClaims", 600, 1, Integer.MAX_VALUE);

		CAPITOL_VILLAGE_UPKEEP = builder
			.comment("Upkeep for a Village capitol block (not charged yet).")
			.defineInRange("villageUpkeep", 5, 0, Integer.MAX_VALUE);

		CAPITOL_TOWN_UPKEEP = builder
			.comment("Upkeep for a Town capitol block (not charged yet).")
			.defineInRange("townUpkeep", 15, 0, Integer.MAX_VALUE);

		CAPITOL_CITY_UPKEEP = builder
			.comment("Upkeep for a City capitol block (not charged yet).")
			.defineInRange("cityUpkeep", 40, 0, Integer.MAX_VALUE);

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
		PROTECT_CROP_TRAMPLING = builder.comment("Prevent non-player crop trampling in claims.").define("cropTrampling", true);

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
