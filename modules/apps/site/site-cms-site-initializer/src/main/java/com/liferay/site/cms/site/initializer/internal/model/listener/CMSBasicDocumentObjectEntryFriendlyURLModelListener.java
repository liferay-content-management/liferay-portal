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
 * This listens on {@link ObjectEntry} rather than {@link FriendlyURLEntry}.
 * {@link com.liferay.object.service.impl.ObjectEntryLocalServiceImpl} adds or
 * updates the object entry's own friendly URL entry before it persists the
 * object entry itself for a versioned, non-draft object entry (as
 * "CMSBasicDocument" is configured), so a {@link FriendlyURLEntry} listener
 * fires while the object entry is not yet visible through
 * {@code ObjectEntryLocalService}. Listening on {@link ObjectEntry} avoids
 * that ordering: the object entry parameter is already fully populated when
 * the listener fires, and by then its friendly URL entry has already been
 * created or updated.
 * </p>
 *
 * @author Jan Brychta
 */
@Component(service = ModelListener.class)
public class CMSBasicDocumentObjectEntryFriendlyURLModelListener
	extends BaseModelListener<ObjectEntry> {

	@Override
	public void onAfterCreate(ObjectEntry objectEntry)
		throws ModelListenerException {

		try {
			_syncFileEntryFriendlyURL(objectEntry);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Override
	public void onAfterUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		try {
			_syncFileEntryFriendlyURL(objectEntry);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private void _syncFileEntryFriendlyURL(ObjectEntry objectEntry)
		throws Exception {

		ObjectDefinition cmsBasicDocumentObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMS_BASIC_DOCUMENT", objectEntry.getCompanyId());

		if ((cmsBasicDocumentObjectDefinition == null) ||
			(objectEntry.getObjectDefinitionId() !=
				cmsBasicDocumentObjectDefinition.getObjectDefinitionId())) {

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

		long objectEntryClassNameId = _classNameLocalService.getClassNameId(
			cmsBasicDocumentObjectDefinition.getClassName());

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				objectEntryClassNameId, objectEntry.getObjectEntryId());

		if (friendlyURLEntry == null) {
			return;
		}

		Map<String, String> urlTitleMap = new HashMap<>();

		for (FriendlyURLEntryLocalization friendlyURLEntryLocalization :
				_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
					friendlyURLEntry.getFriendlyURLEntryId())) {

			urlTitleMap.put(
				friendlyURLEntryLocalization.getLanguageId(),
				friendlyURLEntryLocalization.getUrlTitle());
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

}