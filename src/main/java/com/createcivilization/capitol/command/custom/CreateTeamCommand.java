package com.createcivilization.capitol.command.custom;

import net.minecraft.world.entity.player.Player;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam");
    }

    @Override
    public int execute(Player player) {
        return 1;
    }

    @Override
    public void canExecute(Player player) {
        if (player.getPersistentData().getString("team").isEmpty()) {

        }
    }
}