package com.createcivilization.capitol.constants;

import java.awt.Color;

import java.util.*;

// Try to avoid setting variables that are stored here.
public class CommonConstants {

	public static class Colors {

		public static Color get(String color) {
			return colors.get(color.toUpperCase());
		}

		public static Color get(int rgb) {
			return colors.values().stream().filter(color -> color.getRGB() == rgb).findFirst().orElse(new Color(rgb));
		}

		public static Color get(Object o) {
			if (o instanceof Integer) return get((int) o);
			if (o instanceof String) return get((String) o);
			return null;
		}

		public static final Map<String, Color> colors = new HashMap<>();

		static {
			// Default colors from Color.java
			colors.put("WHITE", Color.WHITE);
			colors.put("LIGHT_GREY", Color.LIGHT_GRAY);
			colors.put("LIGHT_GRAY", Color.LIGHT_GRAY);
			colors.put("GREY", Color.GRAY);
			colors.put("GRAY", Color.GRAY);
			colors.put("DARK_GREY", Color.DARK_GRAY);
			colors.put("DARK_GRAY", Color.DARK_GRAY);
			colors.put("BLACK", Color.BLACK);
			colors.put("RED", Color.RED);
			colors.put("PINK", Color.PINK);
			colors.put("ORANGE", Color.ORANGE);
			colors.put("YELLOW", Color.YELLOW);
			colors.put("GREEN", Color.GREEN);
			colors.put("MAGENTA", Color.MAGENTA);
			colors.put("CYAN", Color.CYAN);
			colors.put("BLUE", Color.BLUE);

			// Custom colors
			colors.put("PURPLE", new Color(128,0,128));
		}
	}
}