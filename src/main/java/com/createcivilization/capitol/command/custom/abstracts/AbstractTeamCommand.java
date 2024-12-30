package com.createcivilization.capitol.command.custom.abstracts;

public abstract class AbstractTeamCommand extends AbstractPlayerCommand {

    protected AbstractTeamCommand(String commandName) {
		super("capitolTeams", commandName);
    }
}