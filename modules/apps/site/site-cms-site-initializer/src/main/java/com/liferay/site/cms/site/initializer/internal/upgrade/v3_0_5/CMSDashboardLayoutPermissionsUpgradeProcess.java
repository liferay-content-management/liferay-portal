/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.upgrade.v3_0_5;

import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Veronica Gonzalez
 */
public class CMSDashboardLayoutPermissionsUpgradeProcess
	extends UpgradeProcess {

	public CMSDashboardLayoutPermissionsUpgradeProcess(
		CompanyLocalService companyLocalService,
		GroupLocalService groupLocalService,
		LayoutLocalService layoutLocalService,
		ResourcePermissionLocalService resourcePermissionLocalService,
		RoleLocalService roleLocalService) {

		_companyLocalService = companyLocalService;
		_groupLocalService = groupLocalService;
		_layoutLocalService = layoutLocalService;
		_resourcePermissionLocalService = resourcePermissionLocalService;
		_roleLocalService = roleLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompanyId(this::_upgradeCompany);
	}

	private void _upgradeCompany(long companyId) throws PortalException {
		Group group = _groupLocalService.fetchGroup(
			companyId, GroupConstants.CMS);

		if (group == null) {
			return;
		}

		Layout layout = _layoutLocalService.fetchLayoutByFriendlyURL(
			group.getGroupId(), false, "/dashboard");
		Role role = _roleLocalService.fetchRole(
			companyId, DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR);

		if ((layout == null) || (role == null)) {
			return;
		}

		_resourcePermissionLocalService.setResourcePermissions(
			companyId, Layout.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(layout.getPlid()), role.getRoleId(),
			new String[] {ActionKeys.VIEW});
	}

	private final CompanyLocalService _companyLocalService;
	private final GroupLocalService _groupLocalService;
	private final LayoutLocalService _layoutLocalService;
	private final ResourcePermissionLocalService
		_resourcePermissionLocalService;
	private final RoleLocalService _roleLocalService;

}