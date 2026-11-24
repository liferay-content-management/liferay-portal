/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.service;

import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectEntryFolderLocalServiceWrapper;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.site.cms.site.initializer.util.CMSDefaultValueUtil;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Stefano Motta
 */
@Component(service = ServiceWrapper.class)
public class CMSDefaultValueObjectEntryFolderLocalServiceWrapper
	extends ObjectEntryFolderLocalServiceWrapper {

	@Override
	public ObjectEntryFolder copyObjectEntryFolder(
			long userId, long objectEntryFolderId,
			long parentObjectEntryFolderId, boolean replace,
			ServiceContext serviceContext)
		throws PortalException {

		ObjectEntryFolder objectEntryFolder = super.copyObjectEntryFolder(
			userId, objectEntryFolderId, parentObjectEntryFolderId, replace,
			serviceContext);

		if ((objectEntryFolder.getObjectEntryFolderId() ==
				objectEntryFolderId) ||
			!FeatureFlagManagerUtil.isEnabled(
				objectEntryFolder.getCompanyId(), "LPD-112761")) {

			return objectEntryFolder;
		}

		ObjectEntryFolder sourceObjectEntryFolder = getObjectEntryFolder(
			objectEntryFolderId);

		if ((objectEntryFolder.getGroupId() == 0) ||
			(sourceObjectEntryFolder.getGroupId() == 0)) {

			return objectEntryFolder;
		}

		_copyObjectEntries(sourceObjectEntryFolder, objectEntryFolder, userId);

		return objectEntryFolder;
	}

	private void _copyObjectEntries(
			ObjectEntryFolder sourceObjectEntryFolder,
			ObjectEntryFolder targetObjectEntryFolder, long userId)
		throws PortalException {

		CMSDefaultValueUtil.copyObjectEntry(
			_filterFactory, sourceObjectEntryFolder, targetObjectEntryFolder,
			userId);

		Map<String, ObjectEntryFolder> sourceObjectEntryFolders =
			new HashMap<>();

		for (ObjectEntryFolder objectEntryFolder :
				getObjectEntryFolders(
					sourceObjectEntryFolder.getGroupId(),
					sourceObjectEntryFolder.getCompanyId(),
					sourceObjectEntryFolder.getObjectEntryFolderId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			sourceObjectEntryFolders.put(
				objectEntryFolder.getName(), objectEntryFolder);
		}

		for (ObjectEntryFolder objectEntryFolder :
				getObjectEntryFolders(
					targetObjectEntryFolder.getGroupId(),
					targetObjectEntryFolder.getCompanyId(),
					targetObjectEntryFolder.getObjectEntryFolderId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			ObjectEntryFolder childSourceObjectEntryFolder =
				sourceObjectEntryFolders.get(objectEntryFolder.getName());

			if (childSourceObjectEntryFolder == null) {
				continue;
			}

			_copyObjectEntries(
				childSourceObjectEntryFolder, objectEntryFolder, userId);
		}
	}

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

}