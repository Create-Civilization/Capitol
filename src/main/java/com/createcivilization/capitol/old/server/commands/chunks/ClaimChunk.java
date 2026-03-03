package com.createcivilization.capitol.old.server.commands.chunks;

import com.createcivilization.capitol.old.common.assets.Request;
import com.createcivilization.capitol.old.common.data.ClaimData;
import com.createcivilization.capitol.old.common.data.TeamData;
import com.createcivilization.capitol.old.server.ServerConstants;
import com.createcivilization.capitol.old.server.commands.abstracts.TeamCommand;
import com.createcivilization.capitol.old.server.utils.StatusHandlers;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;
import java.util.UUID;

/**
 * Claims a chunk
 * <p>
 *     If ran by a player it will claim the current chunk to the player's team
 * <p>
 *     If ran by the server it'll allow to set teamid, dimension, x and z of a chunkpos to claim it in name of a team
 */
public class ClaimChunk extends TeamCommand {
	public ClaimChunk() {
		super("claimChunk");
	}

	@Override
	public boolean requires(CommandSourceStack source) {
		return super.requires(source) && (source.hasPermission(0) || TeamData.BaseUtils.hasPermission(Objects.requireNonNull(source.getPlayer()).getUUID(), "claimChunks"));
	}

	@Override
	public int executes(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean isPlayer = source.isPlayer();

		UUID teamId = null;
		ResourceLocation dimension;
		ChunkPos chunkPos;

		if (context.getNodes().size() > 2) {
			if (!source.hasPermission(0)) {
				source.sendFailure(Component.literal("You don't have permission to use arguments."));
				return 0;
			}

			String teamString = StringArgumentType.getString(context, "teamId");
			teamId = teamString == null ? null : UUID.fromString(teamString);
			dimension = ResourceLocationArgument.getId(context, "dimension");
			chunkPos = new ChunkPos(IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "z"));
		} else if (isPlayer) {
			dimension = Objects.requireNonNull(source.getPlayer()).level().dimension().location();
			chunkPos = source.getPlayer().chunkPosition();
		} else {
			source.sendFailure(Component.literal("Only players or operators with full arguments can claim chunks."));
			return 0;
		}

		StatusHandlers.CommandHandler(
			ClaimData.SmartUtils.claimChunk(
				new Request(ServerConstants.resolveUUIDFromSource.apply(source)),
				teamId,
				dimension,
				chunkPos
			),
			context
		);
		return 1;
	}

	@Override
	protected LiteralArgumentBuilder<CommandSourceStack> setup() {
		return Commands.literal(this.name)
			.requires(this::requires)
			.executes(this::executes)
			.then(
				Commands.argument("teamId", StringArgumentType.word())
					.requires(source -> source.hasPermission(2))
					.executes(this::executes)
					.then(
						Commands.argument("dimension", ResourceLocationArgument.id())
							.executes(this::executes)
							.then(
								Commands.argument("x", IntegerArgumentType.integer())
									.executes(this::executes)
									.then(
										Commands.argument("z", IntegerArgumentType.integer())
											.executes(this::executes)
									)
							)
					)
			);
	}
}
