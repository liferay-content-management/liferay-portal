/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.model.listener;

import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.model.FriendlyURLEntryLocalization;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Keeps a CMS "Basic Document" object entry's friendly URL in sync with the
 * friendly URL of the raw {@link FileEntry} it wraps (via its "file"
 * attachment object field), so the public document URL reflects whatever
 * friendly URL the CMS UI configured, including per-locale translations.
 *
 * <p>
 * This listens on {@link FriendlyURLEntryLocalization} rather than
 * {@link FriendlyURLEntry} itself, because
 * {@link com.liferay.friendly.url.service.impl.FriendlyURLEntryLocalServiceImpl}
 * persists the {@link FriendlyURLEntry} row (firing its model listeners)
 * before it persists the per-locale {@link FriendlyURLEntryLocalization}
 * rows that hold the actual url title being set, so a
 * {@link FriendlyURLEntry} listener would only ever see the url titles from
 * before this update. Listening on {@link FriendlyURLEntryLocalization}
 * fires once the specific locale being changed is already committed.
 * </p>
 *
 * <p>
 * This handles the case where a friendly URL is edited on an object entry
 * that already exists. The companion
 * {@link CMSBasicDocumentObjectEntryFriendlyURLModelListener} handles the
 * case where the friendly URL is set as part of creating the object entry,
 * when the object entry is not yet visible through
 * {@code ObjectEntryLocalService} at the moment this listener would fire.
 * </p>
 *
 * @author Jan Brychta
 */
@Component(service = ModelListener.class)
public class CMSBasicDocumentFriendlyURLEntryModelListener
	extends BaseModelListener<FriendlyURLEntryLocalization> {

	@Override
	public void onAfterCreate(
			FriendlyURLEntryLocalization friendlyURLEntryLocalization)
		throws ModelListenerException {

		try {
			_syncFileEntryFriendlyURL(friendlyURLEntryLocalization);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Override
	public void onAfterUpdate(
			FriendlyURLEntryLocalization originalFriendlyURLEntryLocalization,
			FriendlyURLEntryLocalization friendlyURLEntryLocalization)
		throws ModelListenerException {

		try {
			_syncFileEntryFriendlyURL(friendlyURLEntryLocalization);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private void _syncFileEntryFriendlyURL(
			FriendlyURLEntryLocalization friendlyURLEntryLocalization)
		throws Exception {

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchFriendlyURLEntry(
				friendlyURLEntryLocalization.getFriendlyURLEntryId());

		if (friendlyURLEntry == null) {
			return;
		}

		ObjectDefinition cmsBasicDocumentObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMS_BASIC_DOCUMENT", friendlyURLEntry.getCompanyId());

		if (cmsBasicDocumentObjectDefinition == null) {
			return;
		}

		long objectEntryClassNameId = _classNameLocalService.getClassNameId(
			cmsBasicDocumentObjectDefinition.getClassName());

		if (friendlyURLEntry.getClassNameId() != objectEntryClassNameId) {
			return;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			friendlyURLEntry.getClassPK());

		if (objectEntry == null) {

			// The object entry is not visible yet through

			// ObjectEntryLocalService, meaning this friendly URL entry is
			// being created as part of the object entry's own creation.
			// CMSBasicDocumentObjectEntryFriendlyURLModelListener handles
			// that case once the object entry is fully persisted.

			return;
		}

		long dlFileEntryId = GetterUtil.getLong(
			objectEntry.getValues(
			).get(
				"file"
			));

		if (dlFileEntryId <= 0) {
			return;
		}

		Map<String, String> urlTitleMap = new HashMap<>();

		for (FriendlyURLEntryLocalization currentFriendlyURLEntryLocalization :
				_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
					friendlyURLEntry.getFriendlyURLEntryId())) {

			urlTitleMap.put(
				currentFriendlyURLEntryLocalization.getLanguageId(),
				currentFriendlyURLEntryLocalization.getUrlTitle());
		}

		FileEntry fileEntry = _dlAppLocalService.getFileEntry(dlFileEntryId);

		_friendlyURLEntryLocalService.addFriendlyURLEntry(
			fileEntry.getGroupId(),
			_classNameLocalService.getClassNameId(FileEntry.class),
			fileEntry.getFileEntryId(), friendlyURLEntry.getDefaultLanguageId(),
			urlTitleMap, ServiceContextThreadLocal.getServiceContext());
	}

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}