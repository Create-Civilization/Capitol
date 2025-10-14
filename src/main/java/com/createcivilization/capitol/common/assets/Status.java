package com.createcivilization.capitol.common.assets;

import javax.annotation.Nullable;

public record Status(
	boolean success,
	String summary,
	@Nullable String description
) {
}
