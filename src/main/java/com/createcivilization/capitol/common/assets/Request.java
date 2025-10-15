package com.createcivilization.capitol.common.assets;

import java.util.UUID;

/**
 * Represents the requesting of an action, used to store data for checking whether an action can be done,
 * any method utilizing this should return a {@link Status}.
 * @param origin The UUID origin of the subject, usually a Player's UUID or the SERVER_UUID.
 */
public record Request(
	UUID origin
) {
}
