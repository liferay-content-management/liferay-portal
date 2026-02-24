/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.util;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.service.DepotEntryLocalServiceUtil;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectEntryFolderLocalServiceUtil;
import com.liferay.petra.function.UnsafePredicate;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Marco Galluzzi
 */
public class DepotEntryUtil {

	public static List<Long> getDepotEntryGroupIds(
		long companyId, long userId) {

		boolean hasCMSAdministratorRole = false;

		try {
			hasCMSAdministratorRole = RoleLocalServiceUtil.hasUserRole(
				userId, companyId, RoleConstants.CMS_ADMINISTRATOR, true);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		if (hasCMSAdministratorRole) {
			return DepotEntryLocalServiceUtil.getDepotEntryGroupIds(
				companyId, DepotConstants.TYPE_SPACE);
		}

		return DepotEntryLocalServiceUtil.getDepotEntryGroupIds(
			companyId, userId, DepotConstants.TYPE_SPACE);
	}

	public static List<Long> getDepotEntryGroupIds(
			long companyId, long userId,
			UnsafePredicate<Long, PortalException> unsafeFunction)
		throws PortalException {

		List<Long> depotEntryGroupIds = getDepotEntryGroupIds(
			companyId, userId);

		if (ListUtil.isEmpty(depotEntryGroupIds)) {
			return null;
		}

		ArrayList<Long> selectedDepotEntryGroupIds = new ArrayList<>();

		for (long depotEntryGroupId : depotEntryGroupIds) {
			if (unsafeFunction.test(depotEntryGroupId)) {
				selectedDepotEntryGroupIds.add(depotEntryGroupId);
			}
		}

		return selectedDepotEntryGroupIds;
	}

	public static List<Long> getDepotEntryGroupIds(
		ObjectEntryFolder objectEntryFolder,
		String rootObjectEntryFolderExternalReferenceCode,
		ThemeDisplay themeDisplay) {

		try {
			boolean hasCMSAdministratorRole = RoleLocalServiceUtil.hasUserRole(
				themeDisplay.getUserId(), themeDisplay.getCompanyId(),
				RoleConstants.CMS_ADMINISTRATOR, true);

			ModelResourcePermission<ObjectEntryFolder>
				objectEntryFolderModelResourcePermission =
					_objectEntryFolderModelResourcePermissionSnapshot.get();

			if (objectEntryFolder != null) {
				if (hasCMSAdministratorRole ||
					objectEntryFolderModelResourcePermission.contains(
						themeDisplay.getPermissionChecker(),
						objectEntryFolder.getObjectEntryFolderId(),
						ActionKeys.ADD_ENTRY)) {

					return Collections.singletonList(
						objectEntryFolder.getGroupId());
				}

				return null;
			}

			if (hasCMSAdministratorRole) {
				return DepotEntryLocalServiceUtil.getDepotEntryGroupIds(
					themeDisplay.getCompanyId(), DepotConstants.TYPE_SPACE);
			}

			List<Long> depotEntryGroupIds =
				DepotEntryLocalServiceUtil.getDepotEntryGroupIds(
					themeDisplay.getCompanyId(), themeDisplay.getUserId(),
					DepotConstants.TYPE_SPACE);

			if (ListUtil.isEmpty(depotEntryGroupIds)) {
				return null;
			}

			String[] objectEntryFolderExternalReferenceCodes =
				_getRootObjectEntryFolderExternalReferenceCodes(
					rootObjectEntryFolderExternalReferenceCode);

			return TransformUtil.transform(
				depotEntryGroupIds,
				depotEntryGroupId -> {
					for (String objectEntryFolderExternalReferenceCode :
							objectEntryFolderExternalReferenceCodes) {

						ObjectEntryFolder objectEntryFolder1 =
							ObjectEntryFolderLocalServiceUtil.
								fetchObjectEntryFolderByExternalReferenceCode(
									objectEntryFolderExternalReferenceCode,
									depotEntryGroupId,
									themeDisplay.getCompanyId());

						if ((objectEntryFolder1 != null) &&
							objectEntryFolderModelResourcePermission.contains(
								themeDisplay.getPermissionChecker(),
								objectEntryFolder1.getObjectEntryFolderId(),
								ActionKeys.ADD_ENTRY)) {

							return depotEntryGroupId;
						}
					}

					return null;
				});
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		return null;
	}

	private static String[] _getRootObjectEntryFolderExternalReferenceCodes(
		String rootObjectEntryFolderExternalReferenceCode) {

		if (rootObjectEntryFolderExternalReferenceCode == null) {
			return new String[] {
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES
			};
		}

		return new String[] {rootObjectEntryFolderExternalReferenceCode};
	}

	private static final Log _log = LogFactoryUtil.getLog(DepotEntryUtil.class);

	private static final Snapshot<ModelResourcePermission<ObjectEntryFolder>>
		_objectEntryFolderModelResourcePermissionSnapshot = new Snapshot<>(
			DepotEntryUtil.class, Snapshot.cast(ModelResourcePermission.class),
			"(model.class.name=com.liferay.object.model.ObjectEntryFolder)");

}