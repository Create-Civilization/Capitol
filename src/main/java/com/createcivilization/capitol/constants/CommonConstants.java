package com.createcivilization.capitol.constants;

import java.awt.Color;

import java.util.*;
import java.util.stream.Stream;

// Try to avoid setting variables that are stored here from outside of here.
public class CommonConstants {

	public static class Colors {

		public static String getHex(Color color) {
			return String.format("#%02x%02x%02x", color.getRed(), color.getBlue(), color.getGreen());
		}

		public static Color get(String color) {
			return colors.get(color.toUpperCase());
		}

		public static Color get0(String hex) {
			return getColorsStream()
				.filter(color -> getHex(color).equals(hex))
				.findFirst().orElse(Color.decode(hex));
		}

		public static Color get(int rgb) {
			return getColorsStream()
				.filter(color -> color.getRGB() == rgb)
				.findFirst().orElse(new Color(rgb));
		}

		public static Color get(int[] rgb) {
			int
				red = rgb[0],
				green = rgb[1],
				blue = rgb[2];
			return getColorsStream()
				.filter(color -> color.getRed() == red && color.getGreen() == green && color.getBlue() == blue)
				.findFirst().orElse(new Color(red, green, blue));
		}

		public static Color get(Object o) {
			if (o == null) return null;
			if (o instanceof int[]) return get((int[]) o);
			if (o instanceof Integer) return get((int) o);
			if (o instanceof String) return get((String) o);
			return null;
		}

		public static List<Color> getColors() {
			return new ArrayList<>(colors.values());
		}

		public static Stream<Color> getColorsStream() {
			return getColors().stream();
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