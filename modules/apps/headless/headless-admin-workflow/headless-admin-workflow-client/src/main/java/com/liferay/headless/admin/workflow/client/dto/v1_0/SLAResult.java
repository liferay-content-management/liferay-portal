/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.workflow.client.dto.v1_0;

import com.liferay.headless.admin.workflow.client.function.UnsafeSupplier;
import com.liferay.headless.admin.workflow.client.serdes.v1_0.SLAResultSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Date;
import java.util.Objects;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class SLAResult implements Cloneable, Serializable {

	public static SLAResult toDTO(String json) {
		return SLAResultSerDes.toDTO(json);
	}

	public Boolean getBreached() {
		return breached;
	}

	public void setBreached(Boolean breached) {
		this.breached = breached;
	}

	public void setBreached(
		UnsafeSupplier<Boolean, Exception> breachedUnsafeSupplier) {

		try {
			breached = breachedUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean breached;

	public Date getDateOverdue() {
		return dateOverdue;
	}

	public void setDateOverdue(Date dateOverdue) {
		this.dateOverdue = dateOverdue;
	}

	public void setDateOverdue(
		UnsafeSupplier<Date, Exception> dateOverdueUnsafeSupplier) {

		try {
			dateOverdue = dateOverdueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date dateOverdue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		try {
			id = idUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long id;

	@Override
	public SLAResult clone() throws CloneNotSupportedException {
		return (SLAResult)super.clone();
	}

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
		return SLAResultSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:1962553264