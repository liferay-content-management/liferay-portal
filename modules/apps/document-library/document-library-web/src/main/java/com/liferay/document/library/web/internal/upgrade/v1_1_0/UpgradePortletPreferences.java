/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.upgrade.v1_1_0;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RepositoryLocalService;
import com.liferay.portal.kernel.upgrade.BasePortletPreferencesUpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;

import javax.portlet.PortletPreferences;

/**
 * @author Attila Bakay
 */
public class UpgradePortletPreferences
	extends BasePortletPreferencesUpgradeProcess {

	public UpgradePortletPreferences(
		DLAppLocalService dlAppLocalService,
		GroupLocalService groupLocalService,
		RepositoryLocalService repositoryLocalService) {

		_dlAppLocalService = dlAppLocalService;
		_groupLocalService = groupLocalService;
		_repositoryLocalService = repositoryLocalService;
	}

	@Override
	protected String[] getPortletIds() {
		return new String[] {DLPortletKeys.DOCUMENT_LIBRARY + "_INSTANCE_%"};
	}

	@Override
	protected String upgradePreferences(
			long companyId, long ownerId, int ownerType, long plid,
			String portletId, String xml)
		throws Exception {

		PortletPreferences portletPreferences =
			PortletPreferencesFactoryUtil.fromXML(
				companyId, ownerId, ownerType, plid, portletId, xml);

		long rootFolderId = GetterUtil.getLong(
			portletPreferences.getValue("rootFolderId", "-1"));

		if (rootFolderId == -1) {
			return PortletPreferencesFactoryUtil.toXML(portletPreferences);
		}

		String rootFolderExternalReferenceCode = "";

		if (rootFolderId != 0) {
			Folder folder = _dlAppLocalService.getFolder(rootFolderId);

			rootFolderExternalReferenceCode = folder.getExternalReferenceCode();
		}

		portletPreferences.setValue(
			"rootFolderExternalReferenceCode", rootFolderExternalReferenceCode);

		long selectedRepositoryId = GetterUtil.getLong(
			portletPreferences.getValue("selectedRepositoryId", null));

		Repository selectedRepository = _repositoryLocalService.fetchRepository(
			selectedRepositoryId);

		String selectedRepositoryExternalReferenceCode = "";
		long repositoryGroupId = 0;

		if (selectedRepository != null) {
			selectedRepositoryExternalReferenceCode =
				selectedRepository.getExternalReferenceCode();
			repositoryGroupId = selectedRepository.getGroupId();
		}
		else {
			Group group = _groupLocalService.getGroup(selectedRepositoryId);

			selectedRepositoryExternalReferenceCode =
				group.getExternalReferenceCode();

			repositoryGroupId = selectedRepositoryId;
		}

		portletPreferences.setValue(
			"selectedRepositoryExternalReferenceCode",
			selectedRepositoryExternalReferenceCode);

		portletPreferences.setValue(
			"repositoryGroupId", String.valueOf(repositoryGroupId));

		return PortletPreferencesFactoryUtil.toXML(portletPreferences);
	}

	private final DLAppLocalService _dlAppLocalService;
	private final GroupLocalService _groupLocalService;
	private final RepositoryLocalService _repositoryLocalService;

}