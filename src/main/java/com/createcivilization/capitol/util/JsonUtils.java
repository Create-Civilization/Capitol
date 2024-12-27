package com.createcivilization.capitol.util;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class JsonUtils {

	public static <K, V> void saveJsonMap(JsonWriter writer, String jsonObjectName, Map<K, V> map, boolean close) {
		try {
			writer.name(jsonObjectName).beginObject();
			for (Map.Entry<K, V> entrySet : map.entrySet()) {
				writer.name(String.valueOf(entrySet.getKey()));
				if (entrySet.getValue() instanceof Collection<?> valuesList) {
					writer.beginArray();
					for (Object value : valuesList) writer.value(String.valueOf(value));
					writer.endArray();
				} else {
					writer.beginObject();
					writer.value(String.valueOf(entrySet.getValue()));
					writer.endObject();
				}
			}
			writer.endObject();
			if (close) writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveJsonList(JsonWriter writer, String jsonObjectName, Iterable<?> list, boolean close) {
		try {
			writer.name(jsonObjectName).beginArray();
			for (Object value : list) writer.value(String.valueOf(value));
			writer.endArray();
			if (close) writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <K, V extends Iterable<?>> void advancedSaveJsonMapHoldingList(
		JsonWriter writer,
		String jsonObjectName,
		Map<K, V> map,
		Function<K, String> keyToString,
		Function<V, String[]> valueToString,
		boolean close
	) {
		try {
			writer.name(jsonObjectName).beginObject();
			for (Map.Entry<K, V> entrySet : map.entrySet()) {
				writer.name(keyToString.apply(entrySet.getKey()));
				writer.beginArray();
				for (String value : valueToString.apply(entrySet.getValue())) writer.value(value);
				writer.endArray();
			}
			writer.endObject();
			if (close) writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <K, V> void advancedSaveJsonMap(
		JsonWriter writer,
		String jsonObjectName,
		Map<K, V> map,
		Function<K, String> keyToString,
		Function<V, String> valueToString,
		boolean close
	) {
		try {
			writer.name(jsonObjectName).beginObject();
			for (Map.Entry<K, V> entrySet : map.entrySet()) {
				writer.name(keyToString.apply(entrySet.getKey()));
				if (entrySet.getValue() instanceof Collection<?>) throw new RuntimeException("Called advanceSaveJsonMap expecting a map holding object values but was provided a map holding a Collection (abstraction of List)!");
				else {
					writer.beginObject();
					writer.value(valueToString.apply(entrySet.getValue()));
					writer.endObject();
				}
			}
			writer.endObject();
			if (close) writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}