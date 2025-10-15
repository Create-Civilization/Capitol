package com.createcivilization.capitol.common.assets;

import javax.annotation.Nullable;

/**
 * Statuses are a way of expressing the status of a process, includes a success, summary and description.
 * <p>
 * {@code new Status(true, "Successfully added two numbers", "Utilizing maths we have calculated two numbers")}
 * @param success Whether the relevant process succeeded.
 * @param summary A summary on what happened.
 * @param description A description with more data on what happened.
 */
public record Status(
	boolean success,
	String summary,
	@Nullable String description
) {
}
