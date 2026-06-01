/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.categories.internal.feature.flag;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryConstants;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.model.FriendlyURLEntryLocalization;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagListener;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Roberto Díaz
 */
@Component(
	property = "feature.flag.key=LPD-70396", service = FeatureFlagListener.class
)
public class AssetCategoryFriendlyURLEntryFeatureFlagListener
	implements FeatureFlagListener {

	@Override
	public void onValue(
		long companyId, String featureFlagKey, boolean enabled) {

		long classNameId = _classNameLocalService.getClassNameId(
			AssetCategory.class);

		List<FriendlyURLEntry> orphanedFriendlyURLEntries = new ArrayList<>();

		ActionableDynamicQuery actionableDynamicQuery =
			_friendlyURLEntryLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				dynamicQuery.add(
					RestrictionsFactoryUtil.eq("classNameId", classNameId));

				if (enabled) {
					dynamicQuery.add(
						RestrictionsFactoryUtil.eq(
							"parentClassPK",
							AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID));
				}
				else {
					dynamicQuery.add(
						RestrictionsFactoryUtil.ne(
							"parentClassPK",
							AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID));
				}
			});
		actionableDynamicQuery.setCompanyId(companyId);
		actionableDynamicQuery.setPerformActionMethod(
			(FriendlyURLEntry friendlyURLEntry) -> _updateParentClassPK(
				enabled, friendlyURLEntry, orphanedFriendlyURLEntries));

		try {
			actionableDynamicQuery.performActions();
		}
		catch (PortalException portalException) {
			ReflectionUtil.throwException(portalException);
		}

		for (FriendlyURLEntry orphanedFriendlyURLEntry :
				orphanedFriendlyURLEntries) {

			_friendlyURLEntryLocalService.deleteFriendlyURLEntry(
				orphanedFriendlyURLEntry);
		}
	}

	private void _updateParentClassPK(
		boolean enabled, FriendlyURLEntry friendlyURLEntry,
		List<FriendlyURLEntry> orphanedFriendlyURLEntries) {

		long parentClassPK = AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID;

		if (enabled) {
			AssetCategory assetCategory =
				_assetCategoryLocalService.fetchAssetCategory(
					friendlyURLEntry.getClassPK());

			if (assetCategory == null) {
				orphanedFriendlyURLEntries.add(friendlyURLEntry);

				return;
			}

			parentClassPK = assetCategory.getParentCategoryId();

			if (parentClassPK ==
					AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID) {

				parentClassPK = assetCategory.getVocabularyId();
			}

			if (parentClassPK ==
					AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID) {

				return;
			}
		}

		friendlyURLEntry.setParentClassPK(parentClassPK);

		friendlyURLEntry = _friendlyURLEntryLocalService.updateFriendlyURLEntry(
			friendlyURLEntry);

		for (FriendlyURLEntryLocalization friendlyURLEntryLocalization :
				_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
					friendlyURLEntry.getFriendlyURLEntryId())) {

			friendlyURLEntryLocalization.setParentClassPK(parentClassPK);

			_friendlyURLEntryLocalService.updateFriendlyURLLocalization(
				friendlyURLEntryLocalization);
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Updated parentClassPK to ", parentClassPK,
					" for friendly URL entry ",
					friendlyURLEntry.getFriendlyURLEntryId()));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetCategoryFriendlyURLEntryFeatureFlagListener.class);

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

}