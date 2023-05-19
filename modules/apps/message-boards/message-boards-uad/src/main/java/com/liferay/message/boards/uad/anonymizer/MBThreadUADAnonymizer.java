/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.message.boards.uad.anonymizer;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.message.boards.exception.RequiredMessageException;
import com.liferay.message.boards.exception.RequiredThreadException;
import com.liferay.message.boards.model.MBMessage;
import com.liferay.message.boards.model.MBThread;
import com.liferay.message.boards.service.MBMessageLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.user.associated.data.anonymizer.UADAnonymizer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Locale;
import java.util.Map;

/**
 * @author Brian Wing Shun Chan
 */
@Component(service = UADAnonymizer.class)
public class MBThreadUADAnonymizer extends BaseMBThreadUADAnonymizer {

	public void delete(MBThread mbThread, long userId) throws PortalException {
		MBMessage message =
			_mbMessageLocalService.getMessage(mbThread.getRootMessageId());

		if (message.isDiscussion()) {
			AssetEntry assetEntry =
				assetEntryLocalService.fetchEntry(
					message.getClassName(), message.getClassPK());

			if (assetEntry.getUserId() != mbThread.getUserId()) {
				throw new RequiredThreadException();
			}
		}

		super.delete(mbThread, userId);
	}

	@Override
	public Map<Class<?>, String> getExceptionMessageMap(Locale locale) {
		return HashMapBuilder.<Class<?>, String>put(
			RequiredThreadException.class,
			_language.get(
				locale, "thread-cannot-be-deleted.-anonymimze-instead")
		).build();
	}

	@Reference
	private Language _language;

	@Reference
	private MBMessageLocalService _mbMessageLocalService;
}