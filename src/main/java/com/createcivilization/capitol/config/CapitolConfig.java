package com.createcivilization.capitol.config;

import com.createcivilization.capitol.Capitol;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Consumer;

public class CapitolConfig {

	public static final ForgeConfigSpec SERVER_SPEC;
	public static final CapitolConfig SERVER;

	static {
		var specPair = new ForgeConfigSpec.Builder().configure(CapitolConfig::new);
		SERVER = specPair.getLeft();
		SERVER_SPEC = specPair.getRight();
	}

	public final ForgeConfigSpec.IntValue claimRadius;
	public final ForgeConfigSpec.IntValue inviteTimeout;
	public final ForgeConfigSpec.BooleanValue nonMemberUseItems;
	public final ForgeConfigSpec.BooleanValue nonMemberInteractEntities;
	public final ForgeConfigSpec.BooleanValue nonMemberInteractBlocks;
	public final ForgeConfigSpec.IntValue maxChunks;
	public final ForgeConfigSpec.IntValue maxMembers;

	public final ForgeConfigSpec.BooleanValue debugLogs;
	public final ForgeConfigSpec.BooleanValue offlineMode;
	public final ForgeConfigSpec.BooleanValue logCapitolActions;
	public final ForgeConfigSpec.ConfigValue<String> logUrl;

	public final ForgeConfigSpec.IntValue warTakeoverIncrement;
	public final ForgeConfigSpec.IntValue warTakeoverDecrement;
	public final ForgeConfigSpec.IntValue maxWarTakeoverAmount;

	private final ForgeConfigSpec.Builder builder;

	private CapitolConfig(final ForgeConfigSpec.Builder pBuilder) {
		this.builder = pBuilder;

		this.builder.push("Teams");
		this.claimRadius = this.positiveInteger(
			"The radius around the middle chunk which will also be claimed when placing a Capitol Block.",
			"claim_radius",
			1
		);
		this.inviteTimeout = this.positiveInteger(
			"The number of seconds before an invite expires.",
			"invite_timeout",
			120
		);
		this.nonMemberUseItems = this.boolean0(
			"Whether non-members can use items (can be overridden by team permissions).",
			"can_non_members_use_items",
			true
		);
		this.nonMemberInteractEntities = this.boolean0(
			"Whether non-members can interact with entities (can be overridden by team permissions).",
			"can_non_members_interact_with_entities",
			true
		);
		this.nonMemberInteractBlocks = this.boolean0(
			"Whether non-members can interact with blocks (can be overridden by team permissions).",
			"can_non_members_interact_with_blocks",
			true
		);
		this.maxChunks = this.positiveInteger(
			"The maximum number of chunks a team can own.",
			"max_chunks",
			1000
		);
		this.maxMembers = this.positiveInteger(
			"The maximum number of members a team can have.",
			"max_members",
			50
		);
		this.builder.pop();

		this.builder.push("Debug");
		this.debugLogs = this.boolean0(
			"Prints debug logs to chat and console.",
			"debug_logs",
			false
		);
		this.offlineMode = this.boolean0(
			"Removes calls to Mojang API (use in development environment to avoid crashing game).",
			"offline_mode",
			false
		);
		this.logCapitolActions = this.boolean0(
			"Sends actions like creating teams, adding members to teams, and claiming chunks to the specified url.",
			"log_capitol_actions",
			false
		);
		this.logUrl = this.string(
			"The url to send actions to (formatted for discord webhooks).",
			"log_url",
			""
		);
		this.builder.pop();

		this.builder.push("War");
		this.warTakeoverIncrement = this.positiveInteger(
			"The int increase per tick for war takeover progress.",
			"war_takeover_increment",
			1
		);
		this.warTakeoverDecrement = this.positiveInteger(
			"The int decrease per tick for war takeover progress.",
			"war_takeover_decrement",
			1
		);
		this.maxWarTakeoverAmount = this.positiveInteger(
			"The maximum amount of war takeover progress (divide value by 20 to get time in seconds).",
			"max_war_takeover_amount",
			600
		);
		this.builder.pop();
	}

	@SafeVarargs
	public final ForgeConfigSpec.IntValue positiveInteger(String comment, String name, int defaultValue, Consumer<ForgeConfigSpec.Builder>... additions) {
		return this.integer(comment, name, defaultValue, 0, Integer.MAX_VALUE, additions);
	}

	@SafeVarargs
	public final ForgeConfigSpec.IntValue limitlessInteger(String comment, String name, int defaultValue, Consumer<ForgeConfigSpec.Builder>... additions) {
		return this.integer(comment, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, additions);
	}

	@SafeVarargs
	public final ForgeConfigSpec.IntValue integer(String comment, String name, int defaultValue, int min, int max, Consumer<ForgeConfigSpec.Builder>... additions) {
		try {
			return this.builder.comment(comment).translation(this.translate(name)).defineInRange(name, defaultValue, min, max);
		} finally {
			for (Consumer<ForgeConfigSpec.Builder> addition : additions) addition.accept(this.builder);
		}
	}

	@SafeVarargs
	public final ForgeConfigSpec.BooleanValue boolean0(String comment, String name, boolean defaultValue, Consumer<ForgeConfigSpec.Builder>... additions) {
		try {
			return this.builder.comment(comment).translation(this.translate(name)).define(name, defaultValue);
		} finally {
			for (Consumer<ForgeConfigSpec.Builder> addition : additions) addition.accept(this.builder);
		}
	}

	@SafeVarargs
	public final ForgeConfigSpec.ConfigValue<String> string(String comment, String name, String defaultValue, Consumer<ForgeConfigSpec.Builder>... additions) {
		try {
			return this.builder.comment(comment).translation(this.translate(name)).define(name, defaultValue);
		} finally {
			for (Consumer<ForgeConfigSpec.Builder> addition : additions) addition.accept(this.builder);
		}
	}

	public String translate(String key) {
		return Capitol.MOD_ID + "." + key;
	}
}