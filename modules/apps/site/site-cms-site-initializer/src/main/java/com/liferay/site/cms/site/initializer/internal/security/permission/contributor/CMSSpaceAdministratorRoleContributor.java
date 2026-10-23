/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.security.permission.contributor;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.contributor.RoleCollection;
import com.liferay.portal.kernel.security.permission.contributor.RoleContributor;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Veronica Gonzalez
 */
@Component(service = RoleContributor.class)
public class CMSSpaceAdministratorRoleContributor implements RoleContributor {

	@Override
	public void contribute(RoleCollection roleCollection) {
		try {
			_contribute(roleCollection);
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}
	}

	private void _contribute(RoleCollection roleCollection)
		throws PortalException {

		if (!roleCollection.isSignedIn() ||
			(roleCollection.getGroupId() <= 0)) {

			return;
		}

		Group group = _groupLocalService.fetchGroup(
			roleCollection.getGroupId());

		if ((group == null) ||
			!Objects.equals(group.getGroupKey(), GroupConstants.CMS)) {

			return;
		}

		Role role = _roleLocalService.fetchRole(
			roleCollection.getCompanyId(),
			DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR);

		if ((role == null) || roleCollection.hasRoleId(role.getRoleId()) ||
			!_isSpaceAdministrator(roleCollection)) {

			return;
		}

		roleCollection.addRoleId(role.getRoleId());
	}

	private boolean _isSpaceAdministrator(RoleCollection roleCollection)
		throws PortalException {

		User user = roleCollection.getUser();

		long userId = user.getUserId();

		for (long spaceGroupId :
				_depotEntryLocalService.getDepotEntryGroupIds(
					roleCollection.getCompanyId(), userId,
					DepotConstants.TYPE_SPACE)) {

			if (_userGroupRoleLocalService.hasUserGroupRole(
					userId, spaceGroupId,
					DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR, true) ||
				_userGroupRoleLocalService.hasUserGroupRole(
					userId, spaceGroupId,
					DepotRolesConstants.ASSET_LIBRARY_OWNER, true)) {

				return true;
			}
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CMSSpaceAdministratorRoleContributor.class);

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private UserGroupRoleLocalService _userGroupRoleLocalService;

}