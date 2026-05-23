/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.model.impl;

import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing TokenEntry in entity cache.
 *
 * @author Jürgen Kappler
 * @generated
 */
public class TokenEntryCacheModel
	implements CacheModel<TokenEntry>, Externalizable, MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof TokenEntryCacheModel)) {
			return false;
		}

		TokenEntryCacheModel tokenEntryCacheModel =
			(TokenEntryCacheModel)object;

		if ((tokenEntryId == tokenEntryCacheModel.tokenEntryId) &&
			(mvccVersion == tokenEntryCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, tokenEntryId);

		return HashUtil.hash(hashCode, mvccVersion);
	}

	@Override
	public long getMvccVersion() {
		return mvccVersion;
	}

	@Override
	public void setMvccVersion(long mvccVersion) {
		this.mvccVersion = mvccVersion;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(21);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", tokenEntryId=");
		sb.append(tokenEntryId);
		sb.append(", groupId=");
		sb.append(groupId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", userName=");
		sb.append(userName);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", accessToken=");
		sb.append(accessToken);
		sb.append(", expirationDate=");
		sb.append(expirationDate);
		sb.append(", refreshToken=");
		sb.append(refreshToken);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public TokenEntry toEntityModel() {
		TokenEntryImpl tokenEntryImpl = new TokenEntryImpl();

		tokenEntryImpl.setMvccVersion(mvccVersion);
		tokenEntryImpl.setTokenEntryId(tokenEntryId);
		tokenEntryImpl.setGroupId(groupId);
		tokenEntryImpl.setCompanyId(companyId);
		tokenEntryImpl.setUserId(userId);

		if (userName == null) {
			tokenEntryImpl.setUserName("");
		}
		else {
			tokenEntryImpl.setUserName(userName);
		}

		if (createDate == Long.MIN_VALUE) {
			tokenEntryImpl.setCreateDate(null);
		}
		else {
			tokenEntryImpl.setCreateDate(new Date(createDate));
		}

		if (accessToken == null) {
			tokenEntryImpl.setAccessToken("");
		}
		else {
			tokenEntryImpl.setAccessToken(accessToken);
		}

		if (expirationDate == Long.MIN_VALUE) {
			tokenEntryImpl.setExpirationDate(null);
		}
		else {
			tokenEntryImpl.setExpirationDate(new Date(expirationDate));
		}

		if (refreshToken == null) {
			tokenEntryImpl.setRefreshToken("");
		}
		else {
			tokenEntryImpl.setRefreshToken(refreshToken);
		}

		tokenEntryImpl.resetOriginalValues();

		return tokenEntryImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();

		tokenEntryId = objectInput.readLong();

		groupId = objectInput.readLong();

		companyId = objectInput.readLong();

		userId = objectInput.readLong();
		userName = objectInput.readUTF();
		createDate = objectInput.readLong();
		accessToken = objectInput.readUTF();
		expirationDate = objectInput.readLong();
		refreshToken = objectInput.readUTF();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		objectOutput.writeLong(tokenEntryId);

		objectOutput.writeLong(groupId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(userId);

		if (userName == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(userName);
		}

		objectOutput.writeLong(createDate);

		if (accessToken == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(accessToken);
		}

		objectOutput.writeLong(expirationDate);

		if (refreshToken == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(refreshToken);
		}
	}

	public long mvccVersion;
	public long tokenEntryId;
	public long groupId;
	public long companyId;
	public long userId;
	public String userName;
	public long createDate;
	public String accessToken;
	public long expirationDate;
	public String refreshToken;

}
// LIFERAY-SERVICE-BUILDER-HASH:506494944