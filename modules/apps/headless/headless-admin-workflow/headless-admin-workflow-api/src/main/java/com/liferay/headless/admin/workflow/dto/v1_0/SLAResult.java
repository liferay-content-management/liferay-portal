/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.workflow.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
@GraphQLName(
	description = "The result of a Workflow Metrics SLA for a workflow task.",
	value = "SLAResult"
)
@io.swagger.v3.oas.annotations.media.Schema(
	description = "The result of a Workflow Metrics SLA for a workflow task."
)
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "SLAResult")
public class SLAResult implements Serializable {

	public static SLAResult toDTO(String json) {
		return ObjectMapperUtil.readValue(SLAResult.class, json);
	}

	public static SLAResult unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(SLAResult.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "Whether the task breached the SLA, which happens when the SLA becomes overdue while the task is open."
	)
	public Boolean getBreached() {
		if (_breachedSupplier != null) {
			breached = _breachedSupplier.get();

			_breachedSupplier = null;
		}

		return breached;
	}

	public void setBreached(Boolean breached) {
		this.breached = breached;

		_breachedSupplier = null;
	}

	@JsonIgnore
	public void setBreached(
		UnsafeSupplier<Boolean, Exception> breachedUnsafeSupplier) {

		_breachedSupplier = () -> {
			try {
				return breachedUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(
		description = "Whether the task breached the SLA, which happens when the SLA becomes overdue while the task is open."
	)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected Boolean breached;

	@JsonIgnore
	private Supplier<Boolean> _breachedSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "The SLA's overdue date for the workflow instance of the task."
	)
	public Date getDateOverdue() {
		if (_dateOverdueSupplier != null) {
			dateOverdue = _dateOverdueSupplier.get();

			_dateOverdueSupplier = null;
		}

		return dateOverdue;
	}

	public void setDateOverdue(Date dateOverdue) {
		this.dateOverdue = dateOverdue;

		_dateOverdueSupplier = null;
	}

	@JsonIgnore
	public void setDateOverdue(
		UnsafeSupplier<Date, Exception> dateOverdueUnsafeSupplier) {

		_dateOverdueSupplier = () -> {
			try {
				return dateOverdueUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(
		description = "The SLA's overdue date for the workflow instance of the task."
	)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected Date dateOverdue;

	@JsonIgnore
	private Supplier<Date> _dateOverdueSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(description = "The SLA's ID.")
	public Long getId() {
		if (_idSupplier != null) {
			id = _idSupplier.get();

			_idSupplier = null;
		}

		return id;
	}

	public void setId(Long id) {
		this.id = id;

		_idSupplier = null;
	}

	@JsonIgnore
	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		_idSupplier = () -> {
			try {
				return idUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "The SLA's ID.")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected Long id;

	@JsonIgnore
	private Supplier<Long> _idSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof SLAResult)) {
			return false;
		}

		SLAResult slaResult = (SLAResult)object;

		return Objects.equals(toString(), slaResult.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		Boolean breached = getBreached();

		if (breached != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"breached\": ");

			sb.append(breached);
		}

		Date dateOverdue = getDateOverdue();

		if (dateOverdue != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateOverdue\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(dateOverdue));

			sb.append("\"");
		}

		Long id = getId();

		if (id != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(id);
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.liferay.headless.admin.workflow.dto.v1_0.SLAResult",
		name = "x-class-name"
	)
	public String xClassName;

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
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
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof Map) {
						sb.append(_toJSON((Map<String, ?>)valueArray[i]));
					}
					else if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>)value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value instanceof Collection) {
			return String.valueOf(
				JSONFactoryUtil.createJSONArray((Collection<?>)value));
		}
		else if (value instanceof Map) {
			return String.valueOf(
				JSONFactoryUtil.createJSONObject((Map<?, ?>)value));
		}
		else if (value instanceof Object[]) {
			return String.valueOf(
				JSONFactoryUtil.createJSONArray(
					Arrays.asList((Object[])value)));
		}
		else if (value instanceof String) {
			return StringBundler.concat("\"", _escape(value), "\"");
		}

		return String.valueOf(value);
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

	private Map<String, Serializable> _extendedProperties;

}
// LIFERAY-REST-BUILDER-HASH:1977610323