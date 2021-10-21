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

package com.liferay.layout.internal.service;

import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.layout.friendly.url.LayoutFriendlyURLEntryHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.LayoutFriendlyURL;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutFriendlyURLLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Noor Najjar
 */
@Component(immediate = true, service = ServiceWrapper.class)
public class LayoutFriendlyURLLocalServiceWrapper
	extends com.liferay.portal.kernel.service.
				LayoutFriendlyURLLocalServiceWrapper {

	public LayoutFriendlyURLLocalServiceWrapper() {
		super(null);
	}

	public LayoutFriendlyURLLocalServiceWrapper(
		LayoutFriendlyURLLocalService layoutFriendlyURLLocalService) {

		super(layoutFriendlyURLLocalService);
	}

	@Override
	public LayoutFriendlyURL addLayoutFriendlyURL(
			long userId, long companyId, long groupId, long plid,
			boolean privateLayout, String friendlyURL, String languageId,
			ServiceContext serviceContext)
		throws PortalException {

		User user = _userLocalService.fetchUser(userId);

		long layoutFriendlyURLId = counterLocalService.increment();

		LayoutFriendlyURL layoutFriendlyURL =
			_layoutFriendlyURLLocalService.createLayoutFriendlyURL(
				layoutFriendlyURLId);

		layoutFriendlyURL.setUuid(serviceContext.getUuid());
		layoutFriendlyURL.setGroupId(groupId);
		layoutFriendlyURL.setCompanyId(companyId);
		layoutFriendlyURL.setUserId(user.getUserId());
		layoutFriendlyURL.setUserName(user.getFullName());
		layoutFriendlyURL.setPlid(plid);
		layoutFriendlyURL.setPrivateLayout(privateLayout);
		layoutFriendlyURL.setLanguageId(languageId);

		layoutFriendlyURL.setFriendlyURL(
			_getFriendlyURL(groupId, privateLayout, friendlyURL));

		return _layoutFriendlyURLLocalService.updateLayoutFriendlyURL(
			layoutFriendlyURL);
	}

	@Override
	public List<LayoutFriendlyURL> updateLayoutFriendlyURLs(
			long userId, long companyId, long groupId, long plid,
			boolean privateLayout, Map<Locale, String> friendlyURLMap,
			ServiceContext serviceContext)
		throws PortalException {

		Map<String, LayoutFriendlyURL> layoutFriendlyURLMap = new HashMap<>();

		for (LayoutFriendlyURL layoutFriendlyURL :
				_layoutFriendlyURLLocalService.getLayoutFriendlyURLs(plid)) {

			layoutFriendlyURLMap.put(
				layoutFriendlyURL.getLanguageId(), layoutFriendlyURL);
		}

		List<LayoutFriendlyURL> layoutFriendlyURLs = new ArrayList<>(
			friendlyURLMap.size());

		for (Locale locale : LanguageUtil.getAvailableLocales(groupId)) {
			String friendlyURL = friendlyURLMap.get(locale);

			String languageId = LocaleUtil.toLanguageId(locale);

			LayoutFriendlyURL layoutFriendlyURL = layoutFriendlyURLMap.get(
				languageId);

			if (Validator.isNull(friendlyURL)) {
				if (layoutFriendlyURL != null) {
					deleteLayoutFriendlyURL(layoutFriendlyURL);
				}
			}
			else {
				if (layoutFriendlyURL == null) {
					layoutFriendlyURL = addLayoutFriendlyURL(
						userId, companyId, groupId, plid, privateLayout,
						friendlyURL, languageId, serviceContext);
				}
				else {
					layoutFriendlyURL.setFriendlyURL(friendlyURL);

					layoutFriendlyURL =
						_layoutFriendlyURLLocalService.updateLayoutFriendlyURL(
							layoutFriendlyURL);
				}

				layoutFriendlyURLs.add(layoutFriendlyURL);
			}
		}

		return layoutFriendlyURLs;
	}

	@Reference
	protected CounterLocalService counterLocalService;

	private String _getFriendlyURL(
		long groupId, boolean privateLayout, String friendlyURL) {

		String newFriendlyURL = friendlyURL;

		for (int i = 1;; i++) {
			FriendlyURLEntry friendlyURLEntry =
				_friendlyURLEntryLocalService.fetchFriendlyURLEntry(
					groupId,
					_layoutFriendlyURLEntryHelper.getClassNameId(privateLayout),
					newFriendlyURL);

			if (friendlyURLEntry != null) {
				newFriendlyURL = friendlyURL + i;
			}
			else {
				return newFriendlyURL;
			}
		}
	}

	@Reference
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@Reference
	private LayoutFriendlyURLEntryHelper _layoutFriendlyURLEntryHelper;

	@Reference
	private LayoutFriendlyURLLocalService _layoutFriendlyURLLocalService;

	@Reference
	private UserLocalService _userLocalService;

}