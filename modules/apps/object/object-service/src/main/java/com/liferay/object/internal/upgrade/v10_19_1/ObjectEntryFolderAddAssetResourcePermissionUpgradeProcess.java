/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.upgrade.v10_19_1;

import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Mikel Lorza
 */
public class ObjectEntryFolderAddAssetResourcePermissionUpgradeProcess
	extends UpgradeProcess {

	public ObjectEntryFolderAddAssetResourcePermissionUpgradeProcess(
		ResourcePermissionLocalService resourcePermissionLocalService,
		RoleLocalService roleLocalService) {

		_resourcePermissionLocalService = resourcePermissionLocalService;
		_roleLocalService = roleLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		long bitwiseValue = _getBitwiseValue();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select companyId,objectEntryFolderId from " +
					"ObjectEntryFolder")) {

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long companyId = resultSet.getLong(1);

				Role role = _roleLocalService.getRole(
					companyId, RoleConstants.USER);

				String objectEntryFolderId = resultSet.getString(2);

				ResourcePermission resourcePermission =
					_resourcePermissionLocalService.fetchResourcePermission(
						companyId, ObjectEntryFolder.class.getName(),
						ResourceConstants.SCOPE_INDIVIDUAL, objectEntryFolderId,
						role.getRoleId());

				if (resourcePermission != null) {
					resourcePermission.setActionIds(
						resourcePermission.getActionIds() | bitwiseValue);

					_resourcePermissionLocalService.updateResourcePermission(
						resourcePermission);
				}
				else {
					_resourcePermissionLocalService.setResourcePermissions(
						companyId, ObjectEntryFolder.class.getName(),
						ResourceConstants.SCOPE_INDIVIDUAL, objectEntryFolderId,
						role.getRoleId(), new String[] {ActionKeys.ADD_ASSET});
				}
			}
		}
	}

	private long _getBitwiseValue() throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select bitwiseValue from ResourceAction where name = ? and " +
					"actionId = ?")) {

			preparedStatement.setString(1, ObjectEntryFolder.class.getName());
			preparedStatement.setString(2, ActionKeys.ADD_ASSET);

			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return resultSet.getLong("bitwiseValue");
			}

			return 0;
		}
	}

	private final ResourcePermissionLocalService
		_resourcePermissionLocalService;
	private final RoleLocalService _roleLocalService;

}