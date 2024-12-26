package com.createcivilization.capitol.util;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;

public class JsonUtil {

	public static <K, V> void saveJsonMap(JsonWriter writer, String jsonObjectName, Map<K, V> map, boolean close) throws IOException {
		writer.name(jsonObjectName).beginObject();
		for (Map.Entry<K, V> entrySet : map.entrySet()) {
			writer.name(entrySet.getKey().toString());
			if (entrySet.getValue() instanceof List<?> valuesList) {
				writer.beginArray();
				for (Object value : valuesList) {
					writer.value(value.toString());
				}
				writer.endArray();
			} else {
				writer.beginObject();
				writer.value(entrySet.getValue().toString());
				writer.endObject();
			}
		}
		writer.endObject();
		if (close) writer.close();
	}
}