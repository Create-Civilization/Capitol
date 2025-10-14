package com.createcivilization.capitol.old.command;

import com.createcivilization.capitol.Capitol;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID)
public class CapitolCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
		var dispatcher = event.getDispatcher();
        new com.createcivilization.capitol.old.command.custom.teamcommands.team.CreateTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.debug.ReloadTeamsFromFileCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.debug.ReloadTeamsCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.chunks.ClaimCurrentChunkCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.debug.GetTeamsDebugCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.debug.RemoveTeamDebugCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.debug.ActivateWarTeamsDebugCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.team.DisbandTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.chunks.UnclaimCurrentChunkCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.team.InviteTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.team.InviteAcceptTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.team.LeaveTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.roles.AddRoleTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.roles.ReassignRoleTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.teamcommands.roles.EditRoleTeamCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.debug.AdminModeCommand().register(dispatcher);
		new com.createcivilization.capitol.old.command.custom.debug.GetTakeoverProgress().register(dispatcher);
    }
}