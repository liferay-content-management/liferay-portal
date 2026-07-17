/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.upgrade.v4_0_0;

import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.model.FriendlyURLEntryLocalization;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Backfills the friendly URL of the raw {@link FileEntry} a CMS "Basic
 * Document" object entry wraps (via its "file" attachment object field),
 * for object entries created or updated before the model listeners that
 * keep the two in sync started existing.
 *
 * @author Jan Brychta
 */
public class CMSBasicDocumentFriendlyURLUpgradeProcess extends UpgradeProcess {

	public CMSBasicDocumentFriendlyURLUpgradeProcess(
		ClassNameLocalService classNameLocalService,
		CompanyLocalService companyLocalService,
		DLAppLocalService dlAppLocalService,
		FriendlyURLEntryLocalService friendlyURLEntryLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService) {

		_classNameLocalService = classNameLocalService;
		_companyLocalService = companyLocalService;
		_dlAppLocalService = dlAppLocalService;
		_friendlyURLEntryLocalService = friendlyURLEntryLocalService;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompanyId(this::_upgradeCompany);
	}

	private boolean _isInSync(
		long fileEntryClassNameId, FileEntry fileEntry,
		Map<String, String> urlTitleMap) {

		FriendlyURLEntry fileEntryFriendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				fileEntryClassNameId, fileEntry.getFileEntryId());

		if (fileEntryFriendlyURLEntry == null) {
			return false;
		}

		Map<String, String> fileEntryUrlTitleMap = new HashMap<>();

		for (FriendlyURLEntryLocalization friendlyURLEntryLocalization :
				_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
					fileEntryFriendlyURLEntry.getFriendlyURLEntryId())) {

			fileEntryUrlTitleMap.put(
				friendlyURLEntryLocalization.getLanguageId(),
				friendlyURLEntryLocalization.getUrlTitle());
		}

		return fileEntryUrlTitleMap.equals(urlTitleMap);
	}

	private void _upgradeCompany(long companyId) throws PortalException {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMS_BASIC_DOCUMENT", companyId);

		if (objectDefinition == null) {
			return;
		}

		long objectEntryClassNameId = _classNameLocalService.getClassNameId(
			objectDefinition.getClassName());

		AtomicInteger syncedCount = new AtomicInteger();

		ActionableDynamicQuery actionableDynamicQuery =
			_objectEntryLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> dynamicQuery.add(
				RestrictionsFactoryUtil.eq(
					"objectDefinitionId",
					objectDefinition.getObjectDefinitionId())));
		actionableDynamicQuery.setCompanyId(companyId);
		actionableDynamicQuery.setPerformActionMethod(
			(ObjectEntry objectEntry) -> {
				if (_upgradeObjectEntry(objectEntry, objectEntryClassNameId)) {
					syncedCount.incrementAndGet();
				}
			});

		actionableDynamicQuery.performActions();

		if (_log.isInfoEnabled() && (syncedCount.get() > 0)) {
			_log.info(
				StringBundler.concat(
					"Synced the friendly URL of ", syncedCount.get(),
					" file entries wrapped by CMS basic documents for company ",
					companyId));
		}
	}

	private boolean _upgradeObjectEntry(
			ObjectEntry objectEntry, long objectEntryClassNameId)
		throws PortalException {

		long dlFileEntryId = GetterUtil.getLong(
			objectEntry.getValues(
			).get(
				"file"
			));

		if (dlFileEntryId <= 0) {
			return false;
		}

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				objectEntryClassNameId, objectEntry.getObjectEntryId());

		if (friendlyURLEntry == null) {
			return false;
		}

		Map<String, String> urlTitleMap = new HashMap<>();

		for (FriendlyURLEntryLocalization friendlyURLEntryLocalization :
				_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
					friendlyURLEntry.getFriendlyURLEntryId())) {

			urlTitleMap.put(
				friendlyURLEntryLocalization.getLanguageId(),
				friendlyURLEntryLocalization.getUrlTitle());
		}

		FileEntry fileEntry;

		try {
			fileEntry = _dlAppLocalService.getFileEntry(dlFileEntryId);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			return false;
		}

		long fileEntryClassNameId = _classNameLocalService.getClassNameId(
			FileEntry.class);

		if (_isInSync(fileEntryClassNameId, fileEntry, urlTitleMap)) {
			return false;
		}

		_friendlyURLEntryLocalService.addFriendlyURLEntry(
			fileEntry.getGroupId(), fileEntryClassNameId,
			fileEntry.getFileEntryId(), friendlyURLEntry.getDefaultLanguageId(),
			urlTitleMap, new ServiceContext());

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CMSBasicDocumentFriendlyURLUpgradeProcess.class);

	private final ClassNameLocalService _classNameLocalService;
	private final CompanyLocalService _companyLocalService;
	private final DLAppLocalService _dlAppLocalService;
	private final FriendlyURLEntryLocalService _friendlyURLEntryLocalService;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;

}