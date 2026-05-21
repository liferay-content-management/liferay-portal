/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.AssetListEntry;
import com.liferay.headless.delivery.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
public class AssetListEntrySerDes {

	public static AssetListEntry toDTO(String json) {
		AssetListEntryJSONParser assetListEntryJSONParser =
			new AssetListEntryJSONParser();

		return assetListEntryJSONParser.parseToDTO(json);
	}

	public static AssetListEntry[] toDTOs(String json) {
		AssetListEntryJSONParser assetListEntryJSONParser =
			new AssetListEntryJSONParser();

		return assetListEntryJSONParser.parseToDTOs(json);
	}

	public static String toJSON(AssetListEntry assetListEntry) {
		if (assetListEntry == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (assetListEntry.getAssetListEntryId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"assetListEntryId\": ");

			sb.append(assetListEntry.getAssetListEntryId());
		}

		if (assetListEntry.getClassNameId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"classNameId\": ");

			sb.append(assetListEntry.getClassNameId());
		}

		if (assetListEntry.getClassPK() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"classPK\": ");

			sb.append(assetListEntry.getClassPK());
		}

		if (assetListEntry.getDateCreated() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateCreated\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					assetListEntry.getDateCreated()));

			sb.append("\"");
		}

		if (assetListEntry.getDateModified() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateModified\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					assetListEntry.getDateModified()));

			sb.append("\"");
		}

		if (assetListEntry.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(assetListEntry.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (assetListEntry.getItemSubtype() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemSubtype\": ");

			sb.append("\"");

			sb.append(_escape(assetListEntry.getItemSubtype()));

			sb.append("\"");
		}

		if (assetListEntry.getItemType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemType\": ");

			sb.append("\"");

			sb.append(_escape(assetListEntry.getItemType()));

			sb.append("\"");
		}

		if (assetListEntry.getTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(assetListEntry.getTitle()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		AssetListEntryJSONParser assetListEntryJSONParser =
			new AssetListEntryJSONParser();

		return assetListEntryJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(AssetListEntry assetListEntry) {
		if (assetListEntry == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (assetListEntry.getAssetListEntryId() == null) {
			map.put("assetListEntryId", null);
		}
		else {
			map.put(
				"assetListEntryId",
				String.valueOf(assetListEntry.getAssetListEntryId()));
		}

		if (assetListEntry.getClassNameId() == null) {
			map.put("classNameId", null);
		}
		else {
			map.put(
				"classNameId", String.valueOf(assetListEntry.getClassNameId()));
		}

		if (assetListEntry.getClassPK() == null) {
			map.put("classPK", null);
		}
		else {
			map.put("classPK", String.valueOf(assetListEntry.getClassPK()));
		}

		if (assetListEntry.getDateCreated() == null) {
			map.put("dateCreated", null);
		}
		else {
			map.put(
				"dateCreated",
				liferayToJSONDateFormat.format(
					assetListEntry.getDateCreated()));
		}

		if (assetListEntry.getDateModified() == null) {
			map.put("dateModified", null);
		}
		else {
			map.put(
				"dateModified",
				liferayToJSONDateFormat.format(
					assetListEntry.getDateModified()));
		}

		if (assetListEntry.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(assetListEntry.getExternalReferenceCode()));
		}

		if (assetListEntry.getItemSubtype() == null) {
			map.put("itemSubtype", null);
		}
		else {
			map.put(
				"itemSubtype", String.valueOf(assetListEntry.getItemSubtype()));
		}

		if (assetListEntry.getItemType() == null) {
			map.put("itemType", null);
		}
		else {
			map.put("itemType", String.valueOf(assetListEntry.getItemType()));
		}

		if (assetListEntry.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put("title", String.valueOf(assetListEntry.getTitle()));
		}

		return map;
	}

	public static class AssetListEntryJSONParser
		extends BaseJSONParser<AssetListEntry> {

		@Override
		protected AssetListEntry createDTO() {
			return new AssetListEntry();
		}

		@Override
		protected AssetListEntry[] createDTOArray(int size) {
			return new AssetListEntry[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "assetListEntryId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "classNameId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "classPK")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "dateCreated")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "dateModified")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			AssetListEntry assetListEntry, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "assetListEntryId")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setAssetListEntryId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "classNameId")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setClassNameId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "classPK")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setClassPK(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "dateCreated")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setDateCreated(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "dateModified")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setDateModified(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					assetListEntry.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setItemSubtype((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setItemType((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					assetListEntry.setTitle((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:-1916371748