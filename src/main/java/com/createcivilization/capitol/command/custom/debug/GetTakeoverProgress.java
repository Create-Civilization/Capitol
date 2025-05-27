package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.util.data.IChunkData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GetTakeoverProgress extends AbstractDebugCommand {

	public GetTakeoverProgress() {
		super();
		subSubCommand.set(
			Commands.literal("getTakeoverProgress")
				.requires(this::canExecuteAllParams)
				.executes(this::executeAllParams)
		);
	}

	@Override
	public int execute(Player player) {
		player.sendSystemMessage(Component.literal("Take over progress: " + ((IChunkData)player.level().getChunk(player.getOnPos())).getTakeOverProgress()));
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and an operator");
		return player.hasPermissions(4);
	}
}