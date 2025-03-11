/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.depot.web.internal.info.item.provider;

import com.liferay.depot.model.DepotEntry;
import com.liferay.friendly.url.info.item.provider.InfoItemFriendlyURLProvider;
import com.liferay.friendly.url.model.FriendlyURLEntryLocalization;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.friendly.url.util.comparator.FriendlyURLEntryLocalizationComparator;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.util.Portal;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "item.class.name=com.liferay.depot.model.DepotEntry",
	service = InfoItemFriendlyURLProvider.class
)
public class DepotEntryInfoItemFriendlyURLProvider
	implements InfoItemFriendlyURLProvider<DepotEntry> {

	@Override
	public String getFriendlyURL(DepotEntry depotEntry, String languageId) {
		return String.valueOf(depotEntry.getDepotEntryId());
	}

	@Override
	public List<FriendlyURLEntryLocalization> getFriendlyURLEntryLocalizations(
		DepotEntry depotEntry, String languageId) {

		return _friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
			depotEntry.getGroupId(),
			_portal.getClassNameId(DepotEntry.class.getName()),
			depotEntry.getDepotEntryId(), languageId, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS,
			FriendlyURLEntryLocalizationComparator.getInstance(false));
	}

	@Reference
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@Reference
	private Portal _portal;

}