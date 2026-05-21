/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.dto.v1_0;

import com.liferay.headless.delivery.client.function.UnsafeSupplier;
import com.liferay.headless.delivery.client.serdes.v1_0.AssetListEntrySerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Date;
import java.util.Objects;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class AssetListEntry implements Cloneable, Serializable {

	public static AssetListEntry toDTO(String json) {
		return AssetListEntrySerDes.toDTO(json);
	}

	public Long getAssetListEntryId() {
		return assetListEntryId;
	}

	public void setAssetListEntryId(Long assetListEntryId) {
		this.assetListEntryId = assetListEntryId;
	}

	public void setAssetListEntryId(
		UnsafeSupplier<Long, Exception> assetListEntryIdUnsafeSupplier) {

		try {
			assetListEntryId = assetListEntryIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long assetListEntryId;

	public Long getClassNameId() {
		return classNameId;
	}

	public void setClassNameId(Long classNameId) {
		this.classNameId = classNameId;
	}

	public void setClassNameId(
		UnsafeSupplier<Long, Exception> classNameIdUnsafeSupplier) {

		try {
			classNameId = classNameIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long classNameId;

	public Long getClassPK() {
		return classPK;
	}

	public void setClassPK(Long classPK) {
		this.classPK = classPK;
	}

	public void setClassPK(
		UnsafeSupplier<Long, Exception> classPKUnsafeSupplier) {

		try {
			classPK = classPKUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long classPK;

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setDateCreated(
		UnsafeSupplier<Date, Exception> dateCreatedUnsafeSupplier) {

		try {
			dateCreated = dateCreatedUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date dateCreated;

	public Date getDateModified() {
		return dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public void setDateModified(
		UnsafeSupplier<Date, Exception> dateModifiedUnsafeSupplier) {

		try {
			dateModified = dateModifiedUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date dateModified;

	public String getExternalReferenceCode() {
		return externalReferenceCode;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		this.externalReferenceCode = externalReferenceCode;
	}

	public void setExternalReferenceCode(
		UnsafeSupplier<String, Exception> externalReferenceCodeUnsafeSupplier) {

		try {
			externalReferenceCode = externalReferenceCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String externalReferenceCode;

	public String getItemSubtype() {
		return itemSubtype;
	}

	public void setItemSubtype(String itemSubtype) {
		this.itemSubtype = itemSubtype;
	}

	public void setItemSubtype(
		UnsafeSupplier<String, Exception> itemSubtypeUnsafeSupplier) {

		try {
			itemSubtype = itemSubtypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String itemSubtype;

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public void setItemType(
		UnsafeSupplier<String, Exception> itemTypeUnsafeSupplier) {

		try {
			itemType = itemTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String itemType;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitle(
		UnsafeSupplier<String, Exception> titleUnsafeSupplier) {

		try {
			title = titleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String title;

	@Override
	public AssetListEntry clone() throws CloneNotSupportedException {
		return (AssetListEntry)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof AssetListEntry)) {
			return false;
		}

		AssetListEntry assetListEntry = (AssetListEntry)object;

		return Objects.equals(toString(), assetListEntry.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return AssetListEntrySerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:46647418