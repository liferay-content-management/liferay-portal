/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.internal.service;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceWrapper;
import com.liferay.permission.template.service.PermissionTemplateLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.util.ParamUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Loc Pham
 */
@Component(service = ServiceWrapper.class)
public class PermissionTemplateDLFolderLocalServiceWrapper
	extends DLFolderLocalServiceWrapper {

	@Override
	public DLFolder addFolder(
			String externalReferenceCode, long userId, long groupId,
			long repositoryId, boolean mountPoint, long parentFolderId,
			String name, String description, boolean hidden,
			ServiceContext serviceContext)
		throws PortalException {

		DLFolder folder = super.addFolder(
			externalReferenceCode, userId, groupId, repositoryId, mountPoint,
			parentFolderId, name, description, hidden, serviceContext);

		if (FeatureFlagManagerUtil.isEnabled("LPS-87806")) {
			_permissionTemplateLocalService.addPermissionTemplate(
				folder.getCompanyId(), groupId, DLFolder.class.getName(),
				folder.getFolderId(),
				ParamUtil.getBoolean(
					serviceContext, "permissionTemplateEnabled"));
		}

		return folder;
	}

	@Reference
	private PermissionTemplateLocalService _permissionTemplateLocalService;

}