/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.workflow.client.serdes.v1_0;

import com.liferay.headless.admin.workflow.client.dto.v1_0.SLAResult;
import com.liferay.headless.admin.workflow.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class SLAResultSerDes {

	public static SLAResult toDTO(String json) {
		SLAResultJSONParser slaResultJSONParser = new SLAResultJSONParser();

		return slaResultJSONParser.parseToDTO(json);
	}

	public static SLAResult[] toDTOs(String json) {
		SLAResultJSONParser slaResultJSONParser = new SLAResultJSONParser();

		return slaResultJSONParser.parseToDTOs(json);
	}

	public static String toJSON(SLAResult slaResult) {
		if (slaResult == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (slaResult.getBreached() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"breached\": ");

			sb.append(slaResult.getBreached());
		}

		if (slaResult.getDateOverdue() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateOverdue\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(slaResult.getDateOverdue()));

			sb.append("\"");
		}

		if (slaResult.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(slaResult.getId());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		SLAResultJSONParser slaResultJSONParser = new SLAResultJSONParser();

		return slaResultJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(SLAResult slaResult) {
		if (slaResult == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (slaResult.getBreached() == null) {
			map.put("breached", null);
		}
		else {
			map.put("breached", String.valueOf(slaResult.getBreached()));
		}

		if (slaResult.getDateOverdue() == null) {
			map.put("dateOverdue", null);
		}
		else {
			map.put(
				"dateOverdue",
				liferayToJSONDateFormat.format(slaResult.getDateOverdue()));
		}

		if (slaResult.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(slaResult.getId()));
		}

		return map;
	}

	public static class SLAResultJSONParser extends BaseJSONParser<SLAResult> {

		@Override
		protected SLAResult createDTO() {
			return new SLAResult();
		}

		@Override
		protected SLAResult[] createDTOArray(int size) {
			return new SLAResult[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "breached")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "dateOverdue")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			SLAResult slaResult, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "breached")) {
				if (jsonParserFieldValue != null) {
					slaResult.setBreached((Boolean)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "dateOverdue")) {
				if (jsonParserFieldValue != null) {
					slaResult.setDateOverdue(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					slaResult.setId(Long.valueOf((String)jsonParserFieldValue));
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>)value;

			return _toJSON(collection.toArray());
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:1150391665