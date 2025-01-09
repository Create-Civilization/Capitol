package com.createcivilization.capitol.journeymap;

import journeymap.client.api.*;
import journeymap.client.api.event.ClientEvent;

// See MapPolygon.class
@ClientPlugin
@SuppressWarnings("NullableProblems")
public class JourneyMapIntegration implements IClientPlugin {

	@SuppressWarnings("FieldCanBeLocal")
	private IClientAPI api;

	@Override
	public void initialize(IClientAPI iClientAPI) {
		System.out.println("Capitol initializing JourneyMap integration...");
		this.api = iClientAPI;
	}

	@Override
	public String getModId() {
		return "capitol";
	}

	@Override
	public void onEvent(ClientEvent clientEvent) {}
}