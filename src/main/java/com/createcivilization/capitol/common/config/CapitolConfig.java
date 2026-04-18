package com.createcivilization.capitol.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CapitolConfig {

	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.IntValue MAX_TEAM_CLAIMS;
	public static final ModConfigSpec.IntValue MAX_CLAIM_DISTANCE;
	public static final ModConfigSpec.EnumValue<ListType> CLAIMABLE_DIMENSIONS_LIST_TYPE;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> CLAIMABLE_DIMENSIONS;

	public static final ModConfigSpec.BooleanValue FORCELOAD_ENABLED;
	public static final ModConfigSpec.IntValue FORCELOAD_MAX_PER_TEAM;

	public static final ModConfigSpec.BooleanValue PROTECT_FIRE_SPREAD;
	public static final ModConfigSpec.BooleanValue PROTECT_PISTONS;
	public static final ModConfigSpec.BooleanValue PROTECT_FLUID_FLOW;
	public static final ModConfigSpec.BooleanValue PROTECT_DISPENSERS;
	public static final ModConfigSpec.BooleanValue PROTECT_CROP_TRAMPLING;
	public static final ModConfigSpec.BooleanValue PROTECT_SCULK;

	public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITIES_ALLOWED_TO_GRIEF;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITIES_ALLOWED_TO_GRIEF_ENTITIES;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITIES_ALLOWED_TO_GRIEF_DROPPED_ITEMS;

	public static final ModConfigSpec.EnumValue<ListType> ENTITY_PROTECTION_LIST_TYPE;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> PROTECTED_ENTITIES;

	public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_CLAIM_BARRIER;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_INTERACTION_EXCEPTIONS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_BREAK_EXCEPTIONS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_PLACE_EXCEPTIONS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_USE_EXCEPTIONS;

	public static final ModConfigSpec.ConfigValue<List<? extends String>> OPTIONAL_ENTITY_EXCEPTION_GROUPS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> OPTIONAL_BLOCK_EXCEPTION_GROUPS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> OPTIONAL_BLOCK_ACCESS_ENTITY_GROUPS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> OPTIONAL_ENTITY_ACCESS_ENTITY_GROUPS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> OPTIONAL_DROPPED_ITEM_ACCESS_ENTITY_GROUPS;

	public enum ListType {
		ONLY,
		ALL_BUT
	}

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		builder.comment("Claim settings").push("claims");

		MAX_TEAM_CLAIMS = builder
			.comment("The maximum number of chunks a team can claim.")
			.defineInRange("maxTeamClaims", 1000, 1, Integer.MAX_VALUE);

		MAX_CLAIM_DISTANCE = builder
			.comment("The maximum distance (in chunks) from any existing team claim that a new chunk can be claimed.")
			.defineInRange("maxClaimDistance", 5, 1, Integer.MAX_VALUE);

		CLAIMABLE_DIMENSIONS_LIST_TYPE = builder
			.comment(
				"The type of the dimension list below.",
				"ONLY - only listed dimensions are claimable.",
				"ALL_BUT - all dimensions except those listed are claimable."
			)
			.defineEnum("claimableDimensionsListType", ListType.ALL_BUT);

		CLAIMABLE_DIMENSIONS = builder
			.comment(
				"Dimensions to include/exclude from being claimable, depending on the list type above.",
				"Example: [\"minecraft:overworld\", \"minecraft:the_nether\"]",
				"By default the list is empty and type is ALL_BUT, meaning all dimensions are claimable."
			)
			.defineListAllowEmpty("claimableDimensions", List.of(), () -> "", o -> o instanceof String);

		builder.pop();

		builder.comment(
			"Chunk forceloading settings.",
			"Allows teams to keep claimed chunks loaded even when no players are nearby."
		).push("forceloading");

		FORCELOAD_ENABLED = builder
			.comment("Whether chunk forceloading is enabled on this server.")
			.define("enabled", true);

		FORCELOAD_MAX_PER_TEAM = builder
			.comment("Maximum number of chunks a team can forceload. Set to 0 for unlimited.")
			.defineInRange("maxPerTeam", 10, 0, Integer.MAX_VALUE);

		builder.pop();

		builder.comment(
			"Server-wide protection toggles for non-entity events in claimed chunks.",
			"These apply to ALL claims and cannot be changed per-team."
		).push("protection");

		PROTECT_FIRE_SPREAD = builder
			.comment("Prevent fire spread and lightning fire in claims.")
			.define("fireSpread", true);

		PROTECT_PISTONS = builder
			.comment("Prevent pistons from pushing/pulling blocks into/out of claims.")
			.define("pistons", true);

		PROTECT_FLUID_FLOW = builder
			.comment("Prevent water/lava from flowing into claims from outside.")
			.define("fluidFlow", true);

		PROTECT_DISPENSERS = builder
			.comment("Prevent dispensers from acting into claims from outside.")
			.define("dispensers", true);

		PROTECT_CROP_TRAMPLING = builder
			.comment("Prevent farmland from being trampled by non-player entities in claims.")
			.define("cropTrampling", true);

		PROTECT_SCULK = builder
			.comment("Prevent sculk from spreading in claims.")
			.define("sculk", false);

		builder.pop();

		builder.comment(
			"Entity lists that control which entities can grief in claimed chunks.",
			"All lists support:",
			"  - Resource locations: minecraft:creeper, create:contraption",
			"  - Tags prefixed with #: #minecraft:raiders, #c:animals",
			"  - Wildcard patterns with *: *:*_golem, minecraft:*fish",
			"  - Grouping with (a|b): minecraft:*(painting|item_frame)",
			"Works with any mod's registry keys automatically."
		).push("entityLists");

		ENTITIES_ALLOWED_TO_GRIEF = builder
			.comment(
				"Entities that are ALWAYS allowed to grief blocks in claimed chunks.",
				"Any entity NOT on this list will be prevented from breaking/placing/modifying blocks in claims.",
				"This covers explosions, mob griefing, and any other block-modifying actions."
			)
			.defineListAllowEmpty("entitiesAllowedToGrief", List.of(
				"minecraft:sheep"
			), () -> "", o -> o instanceof String);

		ENTITIES_ALLOWED_TO_GRIEF_ENTITIES = builder
			.comment(
				"Entities that are ALWAYS allowed to grief other entities in claimed chunks.",
				"Any entity NOT on this list will be prevented from damaging/affecting entities in claims."
			)
			.defineListAllowEmpty("entitiesAllowedToGriefEntities", List.of(), () -> "", o -> o instanceof String);

		ENTITIES_ALLOWED_TO_GRIEF_DROPPED_ITEMS = builder
			.comment(
				"Entities that are ALWAYS allowed to pick up or destroy dropped items in claimed chunks.",
				"Any entity NOT on this list will be prevented from affecting dropped items in claims."
			)
			.defineListAllowEmpty("entitiesAllowedToGriefDroppedItems", List.of(), () -> "", o -> o instanceof String);

		ENTITY_PROTECTION_LIST_TYPE = builder
			.comment(
				"How to interpret the protectedEntities list below.",
				"ONLY - only the listed entities are protected from being killed/interacted by players in claims.",
				"ALL_BUT - all entities EXCEPT the listed ones are protected.",
				"This controls which entities are covered by KILL_ENTITIES / INTERACT_ENTITIES permissions."
			)
			.defineEnum("protectedEntitiesListType", ListType.ALL_BUT);

		PROTECTED_ENTITIES = builder
			.comment(
				"Entities affected by entity protection (kill/interact permissions) in claims.",
				"Interpretation depends on protectedEntitiesListType above.",
				"Default is ALL_BUT with empty list, meaning all entities are protected."
			)
			.defineListAllowEmpty("protectedEntities", List.of(), () -> "", o -> o instanceof String);

		builder.pop();

		builder.comment(
			"Global exception lists that ALWAYS apply regardless of team settings.",
			"These are server-wide overrides.",
			"All lists support resource locations, #tags, *(wildcard), and (a|b) grouping."
		).push("exceptions");

		ENTITY_CLAIM_BARRIER = builder
			.comment(
				"Entities that are ALWAYS prevented from entering claimed chunks.",
				"Example: [\"minecraft:falling_block\"] to stop sand/gravel from falling into claims."
			)
			.defineListAllowEmpty("entityClaimBarrier", List.of(), () -> "", o -> o instanceof String);

		BLOCK_INTERACTION_EXCEPTIONS = builder
			.comment(
				"Blocks ANYONE can interact with (right-click) even in protected claims.",
				"Bypasses interaction protection entirely for these blocks.",
				"Supports tags (#minecraft:buttons) and wildcards (*:*_door).",
				"Example: [\"minecraft:crafting_table\", \"#minecraft:buttons\", \"*:*_door\"]"
			)
			.defineListAllowEmpty("blockInteractionExceptions", List.of(
				"minecraft:crafting_table", "minecraft:ender_chest"
			), () -> "", o -> o instanceof String);

		BLOCK_BREAK_EXCEPTIONS = builder
			.comment(
				"Blocks ANYONE can break even in protected claims.",
				"Supports tags and wildcards, same format as blockInteractionExceptions."
			)
			.defineListAllowEmpty("blockBreakExceptions", List.of(), () -> "", o -> o instanceof String);

		BLOCK_PLACE_EXCEPTIONS = builder
			.comment(
				"Blocks ANYONE can place even in protected claims.",
				"Supports tags and wildcards, same format as blockInteractionExceptions.",
				"Example: [\"minecraft:torch\", \"minecraft:soul_torch\"]"
			)
			.defineListAllowEmpty("blockPlaceExceptions", List.of(), () -> "", o -> o instanceof String);

		ITEM_USE_EXCEPTIONS = builder
			.comment(
				"Items ANYONE can use (right-click with) even in protected claims.",
				"Bypasses USE_ITEMS permission for these items.",
				"Supports tags and wildcards.",
				"Example: [\"minecraft:compass\", \"minecraft:clock\", \"#c:maps\"]"
			)
			.defineListAllowEmpty("itemUseExceptions", List.of(), () -> "", o -> o instanceof String);

		builder.pop();

		builder.comment(
			"Optional exception groups that team owners can toggle on/off for their claims.",
			"Format: \"GroupName{entry1, entry2, entry3}\"",
			"Entries support resource locations, #tags, and wildcards.",
			"Teams toggle these groups via /capitol team manage protection group <name>"
		).push("optionalExceptionGroups");

		OPTIONAL_ENTITY_EXCEPTION_GROUPS = builder
			.comment(
				"Named groups of entities that team owners can allow/deny in their claims.",
				"When a team enables a group, those entities bypass entity interaction/kill protection for players."
			)
			.defineListAllowEmpty("entityGroups", List.of(
				"Traders{minecraft:villager, minecraft:wandering_trader}"
			), () -> "", o -> o instanceof String);

		OPTIONAL_BLOCK_EXCEPTION_GROUPS = builder
			.comment(
				"Named groups of blocks that team owners can allow/deny in their claims.",
				"When a team enables a group, those blocks bypass interaction protection for players."
			)
			.defineListAllowEmpty("blockGroups", List.of(
				"Doors{#minecraft:doors, #minecraft:trapdoors}",
				"Buttons{#minecraft:buttons}"
			), () -> "", o -> o instanceof String);

		OPTIONAL_BLOCK_ACCESS_ENTITY_GROUPS = builder
			.comment(
				"Named groups of entities that team owners can allow/deny block griefing for.",
				"When a team enables a group, those entities can grief blocks in their claims.",
				"Example: \"Creepers{minecraft:creeper}\" lets teams choose if creepers can blow up their claims."
			)
			.defineListAllowEmpty("blockAccessEntityGroups", List.of(
				"Creepers{minecraft:creeper}",
				"Withers{minecraft:wither, minecraft:wither_skull}",
				"TNT{minecraft:tnt, minecraft:tnt_minecart}",
				"Endermen{minecraft:enderman}"
			), () -> "", o -> o instanceof String);

		OPTIONAL_ENTITY_ACCESS_ENTITY_GROUPS = builder
			.comment(
				"Named groups of entities that team owners can allow/deny entity griefing for.",
				"When a team enables a group, those entities can damage other entities in their claims."
			)
			.defineListAllowEmpty("entityAccessEntityGroups", List.of(), () -> "", o -> o instanceof String);

		OPTIONAL_DROPPED_ITEM_ACCESS_ENTITY_GROUPS = builder
			.comment(
				"Named groups of entities that team owners can allow/deny dropped item access for.",
				"When a team enables a group, those entities can pick up/destroy dropped items in their claims."
			)
			.defineListAllowEmpty("droppedItemAccessEntityGroups", List.of(), () -> "", o -> o instanceof String);

		builder.pop();

		SPEC = builder.build();
	}
}
