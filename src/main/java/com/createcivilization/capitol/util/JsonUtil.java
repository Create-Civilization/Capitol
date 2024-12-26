package com.createcivilization.capitol.util;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class JsonUtil {

	public static <K, V> void saveJsonMap(JsonWriter writer, String jsonObjectName, Map<K, V> map, boolean close) throws IOException {
		writer.name(jsonObjectName).beginObject();
		for (Map.Entry<K, V> entrySet : map.entrySet()) {
			writer.name(String.valueOf(entrySet.getKey()));
			if (entrySet.getValue() instanceof List<?> valuesList) {
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
	}

	public static <K, V> void advancedSaveJsonMapHoldingList(
		JsonWriter writer,
		String jsonObjectName,
		Map<K, V> map,
		Function<K, String> keyToString,
		Function<V, String[]> valueToString,
		boolean close
	) throws IOException {
		writer.name(jsonObjectName).beginObject();
		for (Map.Entry<K, V> entrySet : map.entrySet()) {
			writer.name(keyToString.apply(entrySet.getKey()));
			for (String value : valueToString.apply(entrySet.getValue())) writer.value(value);
		}
		writer.endObject();
		if (close) writer.close();
	}

	public static <K, V> void advancedSaveJsonMap(
		JsonWriter writer,
		String jsonObjectName,
		Map<K, V> map,
		Function<K, String> keyToString,
		Function<V, String> valueToString,
		boolean close
	) throws IOException {
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
	}
}