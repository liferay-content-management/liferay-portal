/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portlet.documentlibrary.util;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PermissionPropagationEntry;
import com.liferay.portal.kernel.service.PermissionPropagationEntryLocalServiceUtil;

import java.util.List;

/**
 * @author To Trinh
 */
public class DLPermissionPropagationUtil {

	public static long getInheritableParentFolderId(DLFolder dlFolder)
		throws PortalException {

		return getInheritableParentFolderId(
			dlFolder.getCompanyId(), dlFolder.getGroupId(),
			dlFolder.getAncestorFolderIds());
	}

	public static long getInheritableParentFolderId(
		long companyId, long groupId, List<Long> ancestorIds) {

		for (long ancestorId : ancestorIds) {
			PermissionPropagationEntry permissionPropagationEntry =
				PermissionPropagationEntryLocalServiceUtil.
					fetchPermissionPropagationEntry(
						companyId, groupId, DLFolderConstants.getClassName(),
						ancestorId);

			if ((permissionPropagationEntry != null) &&
				permissionPropagationEntry.isPropagation()) {

				return ancestorId;
			}
		}

		if (isHomeFolderPermissionPropagationEnabled(companyId, groupId)) {
			return DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
		}

		return -1;
	}

	public static boolean isHomeFolderPermissionPropagationEnabled(
		long companyId, long groupId) {

		PermissionPropagationEntry permissionPropagationEntry =
			PermissionPropagationEntryLocalServiceUtil.
				fetchPermissionPropagationEntry(
					companyId, groupId, DLFolderConstants.getClassName(),
					groupId);

		if (permissionPropagationEntry != null) {
			return permissionPropagationEntry.isPropagation();
		}

		return DLFolderConstants.DEFAULT_FOLDER_PERMISSION_PROPAGATION_ENABLED;
	}

}