/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.service.impl;

import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.object.storage.sharepoint.service.base.TokenEntryLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = "model.class.name=com.liferay.object.storage.sharepoint.model.TokenEntry",
	service = AopService.class
)
public class TokenEntryLocalServiceImpl extends TokenEntryLocalServiceBaseImpl {

	@Override
	public TokenEntry addTokenEntry(
			String accessToken, Date expirationDate, long groupId,
			String refreshToken, long userId)
		throws PortalException {

		TokenEntry tokenEntry = tokenEntryPersistence.fetchByG_U(
			groupId, userId);

		if (tokenEntry != null) {
			tokenEntry.setAccessToken(accessToken);
			tokenEntry.setExpirationDate(expirationDate);
			tokenEntry.setRefreshToken(refreshToken);

			return tokenEntryPersistence.update(tokenEntry);
		}

		long tokenEntryId = counterLocalService.increment();

		tokenEntry = tokenEntryPersistence.create(tokenEntryId);

		tokenEntry.setUserId(userId);

		User user = _userLocalService.getUser(userId);

		tokenEntry.setUserName(user.getFullName());

		tokenEntry.setCreateDate(new Date());
		tokenEntry.setAccessToken(accessToken);
		tokenEntry.setExpirationDate(expirationDate);
		tokenEntry.setGroupId(groupId);
		tokenEntry.setRefreshToken(refreshToken);

		return tokenEntryPersistence.update(tokenEntry);
	}

	@Override
	public TokenEntry fetchTokenEntry(long groupId, long userId) {
		return tokenEntryPersistence.fetchByG_U(groupId, userId);
	}

	@Reference
	private UserLocalService _userLocalService;

}