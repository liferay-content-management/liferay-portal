/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a wrapper for {@link TokenEntry}.
 * </p>
 *
 * @author Jürgen Kappler
 * @see TokenEntry
 * @generated
 */
public class TokenEntryWrapper
	extends BaseModelWrapper<TokenEntry>
	implements ModelWrapper<TokenEntry>, TokenEntry {

	public TokenEntryWrapper(TokenEntry tokenEntry) {
		super(tokenEntry);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put("tokenEntryId", getTokenEntryId());
		attributes.put("groupId", getGroupId());
		attributes.put("companyId", getCompanyId());
		attributes.put("userId", getUserId());
		attributes.put("userName", getUserName());
		attributes.put("createDate", getCreateDate());
		attributes.put("accessToken", getAccessToken());
		attributes.put("expirationDate", getExpirationDate());
		attributes.put("refreshToken", getRefreshToken());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long mvccVersion = (Long)attributes.get("mvccVersion");

		if (mvccVersion != null) {
			setMvccVersion(mvccVersion);
		}

		Long tokenEntryId = (Long)attributes.get("tokenEntryId");

		if (tokenEntryId != null) {
			setTokenEntryId(tokenEntryId);
		}

		Long groupId = (Long)attributes.get("groupId");

		if (groupId != null) {
			setGroupId(groupId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long userId = (Long)attributes.get("userId");

		if (userId != null) {
			setUserId(userId);
		}

		String userName = (String)attributes.get("userName");

		if (userName != null) {
			setUserName(userName);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		String accessToken = (String)attributes.get("accessToken");

		if (accessToken != null) {
			setAccessToken(accessToken);
		}

		Date expirationDate = (Date)attributes.get("expirationDate");

		if (expirationDate != null) {
			setExpirationDate(expirationDate);
		}

		String refreshToken = (String)attributes.get("refreshToken");

		if (refreshToken != null) {
			setRefreshToken(refreshToken);
		}
	}

	@Override
	public TokenEntry cloneWithOriginalValues() {
		return wrap(model.cloneWithOriginalValues());
	}

	/**
	 * Returns the access token of this token entry.
	 *
	 * @return the access token of this token entry
	 */
	@Override
	public String getAccessToken() {
		return model.getAccessToken();
	}

	/**
	 * Returns the company ID of this token entry.
	 *
	 * @return the company ID of this token entry
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the create date of this token entry.
	 *
	 * @return the create date of this token entry
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the expiration date of this token entry.
	 *
	 * @return the expiration date of this token entry
	 */
	@Override
	public Date getExpirationDate() {
		return model.getExpirationDate();
	}

	/**
	 * Returns the group ID of this token entry.
	 *
	 * @return the group ID of this token entry
	 */
	@Override
	public long getGroupId() {
		return model.getGroupId();
	}

	/**
	 * Returns the mvcc version of this token entry.
	 *
	 * @return the mvcc version of this token entry
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the primary key of this token entry.
	 *
	 * @return the primary key of this token entry
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the refresh token of this token entry.
	 *
	 * @return the refresh token of this token entry
	 */
	@Override
	public String getRefreshToken() {
		return model.getRefreshToken();
	}

	/**
	 * Returns the token entry ID of this token entry.
	 *
	 * @return the token entry ID of this token entry
	 */
	@Override
	public long getTokenEntryId() {
		return model.getTokenEntryId();
	}

	/**
	 * Returns the user ID of this token entry.
	 *
	 * @return the user ID of this token entry
	 */
	@Override
	public long getUserId() {
		return model.getUserId();
	}

	/**
	 * Returns the user name of this token entry.
	 *
	 * @return the user name of this token entry
	 */
	@Override
	public String getUserName() {
		return model.getUserName();
	}

	/**
	 * Returns the user uuid of this token entry.
	 *
	 * @return the user uuid of this token entry
	 */
	@Override
	public String getUserUuid() {
		return model.getUserUuid();
	}

	@Override
	public void persist() {
		model.persist();
	}

	/**
	 * Sets the access token of this token entry.
	 *
	 * @param accessToken the access token of this token entry
	 */
	@Override
	public void setAccessToken(String accessToken) {
		model.setAccessToken(accessToken);
	}

	/**
	 * Sets the company ID of this token entry.
	 *
	 * @param companyId the company ID of this token entry
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the create date of this token entry.
	 *
	 * @param createDate the create date of this token entry
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the expiration date of this token entry.
	 *
	 * @param expirationDate the expiration date of this token entry
	 */
	@Override
	public void setExpirationDate(Date expirationDate) {
		model.setExpirationDate(expirationDate);
	}

	/**
	 * Sets the group ID of this token entry.
	 *
	 * @param groupId the group ID of this token entry
	 */
	@Override
	public void setGroupId(long groupId) {
		model.setGroupId(groupId);
	}

	/**
	 * Sets the mvcc version of this token entry.
	 *
	 * @param mvccVersion the mvcc version of this token entry
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the primary key of this token entry.
	 *
	 * @param primaryKey the primary key of this token entry
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the refresh token of this token entry.
	 *
	 * @param refreshToken the refresh token of this token entry
	 */
	@Override
	public void setRefreshToken(String refreshToken) {
		model.setRefreshToken(refreshToken);
	}

	/**
	 * Sets the token entry ID of this token entry.
	 *
	 * @param tokenEntryId the token entry ID of this token entry
	 */
	@Override
	public void setTokenEntryId(long tokenEntryId) {
		model.setTokenEntryId(tokenEntryId);
	}

	/**
	 * Sets the user ID of this token entry.
	 *
	 * @param userId the user ID of this token entry
	 */
	@Override
	public void setUserId(long userId) {
		model.setUserId(userId);
	}

	/**
	 * Sets the user name of this token entry.
	 *
	 * @param userName the user name of this token entry
	 */
	@Override
	public void setUserName(String userName) {
		model.setUserName(userName);
	}

	/**
	 * Sets the user uuid of this token entry.
	 *
	 * @param userUuid the user uuid of this token entry
	 */
	@Override
	public void setUserUuid(String userUuid) {
		model.setUserUuid(userUuid);
	}

	@Override
	public String toXmlString() {
		return model.toXmlString();
	}

	@Override
	protected TokenEntryWrapper wrap(TokenEntry tokenEntry) {
		return new TokenEntryWrapper(tokenEntry);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:-382933569