package com.createcivilization.capitol.journeymap;

import journeymap.client.api.*;
import journeymap.client.api.event.ClientEvent;

@ClientPlugin
@SuppressWarnings("NullableProblems")
public class JourneyMapIntegration implements IClientPlugin {

	private IClientAPI api;

	@Override
	public void initialize(IClientAPI iClientAPI) {
		System.out.println("Find me in the logs.");
		this.api = iClientAPI;
	}

	@Override
	public String getModId() {
		return "capitol";
	}

	@Override
	public void onEvent(ClientEvent clientEvent) {
		api.getAllWaypoints();
	}
}