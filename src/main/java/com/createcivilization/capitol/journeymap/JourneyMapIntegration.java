package com.createcivilization.capitol.journeymap;

import journeymap.client.api.*;
import journeymap.client.api.event.ClientEvent;

// See MapPolygon.class
// TODO: Take chunk data from client and use PolygonHelper to create the list for it, and then wrap it in a PolygonOverlay, and do stuff with IClientAPI.show() and IClientAPI.remove()
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